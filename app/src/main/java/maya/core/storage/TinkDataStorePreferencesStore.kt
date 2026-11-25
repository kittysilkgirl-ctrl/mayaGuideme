package maya.core.storage
import android.content.Context
import android.util.Base64
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.io.IOException
import java.security.GeneralSecurityException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val DATASTORE_NAME = "maya_secure_prefs"
private const val TINK_KEYSET_NAME = "maya_aead_keyset"
private const val TINK_PREF_FILE = "maya_tink_keyset_prefs"
private const val MASTER_KEY_URI = "android-keystore://maya_master_key"

// Process-wide singleton DataStore delegate; creates files/datastore/maya_secure_prefs.preferences_pb
private val Context.mayaSecureDataStore by preferencesDataStore(
    name = DATASTORE_NAME,
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

/**
 * Concrete [SecurePreferencesStore] backed by Jetpack Preferences DataStore
 * and Google Tink AEAD (AES256-GCM) wrapped by an Android Keystore master key.
 *
 * File names match `res/xml/backup_rules.xml` and `res/xml/data_extraction_rules.xml`
 * so encrypted state is never restored onto a device with a different Keystore key.
 */
class TinkDataStorePreferencesStore(
    context: Context
) : SecurePreferencesStore {

    private val appContext = context.applicationContext
    private val dataStore = appContext.mayaSecureDataStore

    // Lazy (synchronized by default) so Keystore binder IPC never runs on the UI thread
    private val aead: Aead by lazy { buildOrRecoverAead() }

    override val consentGrantedFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            decryptOrReset(prefs[KEY_CONSENT_GRANTED], KEY_CONSENT_GRANTED.name) == "true"
        }
        .flowOn(Dispatchers.IO)

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            dataStore.edit { it.clear() }
        }
    }
    override val geminiApiKeyFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            decryptOrReset(prefs[KEY_GEMINI_API_KEY], KEY_GEMINI_API_KEY.name)
        }
        .flowOn(Dispatchers.IO)

    override val what3wordsApiKeyFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            decryptOrReset(prefs[KEY_WHAT3WORDS_API_KEY], KEY_WHAT3WORDS_API_KEY.name)
        }
        .flowOn(Dispatchers.IO)

    override suspend fun setConsentGranted(granted: Boolean) {
        withContext(Dispatchers.IO) {
            val encrypted = encryptValue(granted.toString(), KEY_CONSENT_GRANTED.name)
            dataStore.edit { prefs ->
                prefs[KEY_CONSENT_GRANTED] = encrypted
            }
        }
    }

    override suspend fun setGeminiApiKey(apiKey: String?) {
        writeOptionalSecret(KEY_GEMINI_API_KEY, apiKey)
    }

    override suspend fun setWhat3wordsApiKey(apiKey: String?) {
        writeOptionalSecret(KEY_WHAT3WORDS_API_KEY, apiKey)
    }

    private suspend fun writeOptionalSecret(
        key: Preferences.Key<String>,
        value: String?
    ) {
        withContext(Dispatchers.IO) {
            val normalized = value?.trim()?.takeIf { it.isNotEmpty() }
            if (normalized == null) {
                dataStore.edit { prefs -> prefs.remove(key) }
            } else {
                val encrypted = encryptValue(normalized, key.name)
                dataStore.edit { prefs -> prefs[key] = encrypted }
            }
        }
    }

    private fun buildOrRecoverAead(): Aead {
        AeadConfig.register()
        return try {
            createKeysetHandle().getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        } catch (_: GeneralSecurityException) {
            resetCorruptedKeyset()
            createKeysetHandle().getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        } catch (_: IOException) {
            resetCorruptedKeyset()
            createKeysetHandle().getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        }
    }

    private fun createKeysetHandle() = AndroidKeysetManager.Builder()
        .withSharedPref(appContext, TINK_KEYSET_NAME, TINK_PREF_FILE)
        .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
        .withMasterKeyUri(MASTER_KEY_URI)
        .build()
        .keysetHandle

    private fun resetCorruptedKeyset() {
        appContext
            .getSharedPreferences(TINK_PREF_FILE, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }


    private fun encryptValue(plaintext: String, associatedData: String): String {
        val ciphertext = aead.encrypt(
            plaintext.toByteArray(Charsets.UTF_8),
            associatedData.toByteArray(Charsets.UTF_8)
        )
        return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    private suspend fun decryptOrReset(
        encodedCiphertext: String?,
        associatedData: String
    ): String? {
        if (encodedCiphertext.isNullOrEmpty()) return null
        return try {
            val ciphertext = Base64.decode(encodedCiphertext, Base64.NO_WRAP)
            val plaintextBytes = aead.decrypt(
                ciphertext,
                associatedData.toByteArray(Charsets.UTF_8)
            )
            String(plaintextBytes, Charsets.UTF_8)
        } catch (_: GeneralSecurityException) {
            // Covers AEADBadTagException after OS reset or Keystore drift: wipe cleanly so consent re-prompts
            dataStore.edit { it.clear() }
            null
        } catch (_: IllegalArgumentException) {
            dataStore.edit { it.clear() }
            null
        }
    }

    private companion object {
        val KEY_CONSENT_GRANTED = stringPreferencesKey("consent_granted_enc")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key_enc")
        val KEY_WHAT3WORDS_API_KEY = stringPreferencesKey("what3words_api_key_enc")
    }
}
