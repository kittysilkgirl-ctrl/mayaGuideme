package maya.core.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Contract for securely persisting user consent and optional Bring-Your-Own-Key (BYOK)
 * credentials (Gemini API key, what3words API key).
 *
 * Exposes observable reactive streams via [Flow] and suspending update functions to allow
 * asynchronous persistence (e.g. DataStore + Tink AEAD) without blocking the main/UI thread.
 */
interface SecurePreferencesStore {

    /**
     * Emits whether mandatory first-run telemetry consent has been granted.
     * Defaults to false until explicitly accepted.
     */
    val consentGrantedFlow: Flow<Boolean>

    /**
     * Emits the optional user-supplied Gemini API key, or null if using the free-tier default.
     */
    val geminiApiKeyFlow: Flow<String?>

    /**
     * Emits the optional user-supplied what3words API key, or null if disabled.
     */
    val what3wordsApiKeyFlow: Flow<String?>

    /**
     * Persists the user's telemetry consent decision.
     */
    suspend fun setConsentGranted(granted: Boolean)

    /**
     * Persists or clears (if blank/null) the custom Gemini API key.
     */
    suspend fun setGeminiApiKey(apiKey: String?)

    /**
     * Persists or clears (if blank/null) the custom what3words API key.
     */
    suspend fun setWhat3wordsApiKey(apiKey: String?)

    /**
     * Clears all stored keys and consent (useful for reset, corruption recovery, or logout).
     */
    suspend fun clear()
}

/**
 * Pure in-memory implementation of [SecurePreferencesStore].
 *
 * Backed by thread-safe [MutableStateFlow] instances. Can be used in unit tests,
 * Compose previews, and early prototype wiring without touching Android Keystore or disk.
 */
class InMemorySecurePreferencesStore(
    initialConsentGranted: Boolean = false,
    initialGeminiApiKey: String? = null,
    initialWhat3wordsApiKey: String? = null
) : SecurePreferencesStore {

    private val _consentGranted = MutableStateFlow(initialConsentGranted)
    override val consentGrantedFlow: Flow<Boolean> = _consentGranted.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(initialGeminiApiKey)
    override val geminiApiKeyFlow: Flow<String?> = _geminiApiKey.asStateFlow()

    private val _what3wordsApiKey = MutableStateFlow(initialWhat3wordsApiKey)
    override val what3wordsApiKeyFlow: Flow<String?> = _what3wordsApiKey.asStateFlow()

    override suspend fun setConsentGranted(granted: Boolean) {
        _consentGranted.value = granted
    }

    override suspend fun setGeminiApiKey(apiKey: String?) {
        _geminiApiKey.value = apiKey?.takeIf { it.isNotBlank() }
    }

    override suspend fun setWhat3wordsApiKey(apiKey: String?) {
        _what3wordsApiKey.value = apiKey?.takeIf { it.isNotBlank() }
    }

    override suspend fun clear() {
        _consentGranted.value = false
        _geminiApiKey.value = null
        _what3wordsApiKey.value = null
    }
}
