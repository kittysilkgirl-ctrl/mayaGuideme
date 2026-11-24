package maya.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import maya.core.storage.SecurePreferencesStore

/**
 * Immutable UI state representing the first-run telemetry consent gate
 * and optional Bring-Your-Own-Key (BYOK) API key setup flow.
 */
data class OnboardingUiState(
    val isLoading: Boolean = true,
    val consentGranted: Boolean = false,
    val showConsentDialog: Boolean = false,
    val showByokSetup: Boolean = false,
    val geminiApiKey: String? = null,
    val what3wordsApiKey: String? = null,
    val shouldExitApp: Boolean = false
)

/**
 * ViewModel orchestrating the mandatory first-run consent gate and optional BYOK key setup.
 *
 * Depends strictly on the [SecurePreferencesStore] interface so it remains completely
 * decoupled from Android Keystore / DataStore in tests and previews.
 */
class OnboardingViewModel(
    private val preferencesStore: SecurePreferencesStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                preferencesStore.consentGrantedFlow,
                preferencesStore.geminiApiKeyFlow,
                preferencesStore.what3wordsApiKeyFlow
            ) { consentGranted, geminiKey, what3wordsKey ->
                Triple(consentGranted, geminiKey, what3wordsKey)
            }.collect { (consentGranted, geminiKey, what3wordsKey) ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        consentGranted = consentGranted,
                        showConsentDialog = !consentGranted && !current.shouldExitApp,
                        geminiApiKey = geminiKey,
                        what3wordsApiKey = what3wordsKey
                    )
                }
            }
        }
    }

    /**
     * User agreed to mandatory first-run telemetry consent.
     * Persists consentGranted = true, closes the consent dialog, and opens the optional BYOK setup.
     */
    fun acceptConsent() {
        viewModelScope.launch {
            preferencesStore.setConsentGranted(true)
            _uiState.update { current ->
                current.copy(
                    consentGranted = true,
                    showConsentDialog = false,
                    showByokSetup = true,
                    shouldExitApp = false
                )
            }
        }
    }

    /**
     * User declined mandatory telemetry consent.
     * Blocks application usage and signals the host Activity to exit cleanly.
     */
    fun declineConsent() {
        viewModelScope.launch {
            preferencesStore.setConsentGranted(false)
            _uiState.update { current ->
                current.copy(
                    consentGranted = false,
                    showConsentDialog = false,
                    showByokSetup = false,
                    shouldExitApp = true
                )
            }
        }
    }

    /**
     * User saves optional Gemini and/or what3words API keys and proceeds into the application.
     */
    fun saveByokKeys(geminiApiKey: String?, what3wordsApiKey: String?) {
        viewModelScope.launch {
            val normalizedGemini = geminiApiKey?.trim()?.takeIf { it.isNotEmpty() }
            val normalizedWhat3words = what3wordsApiKey?.trim()?.takeIf { it.isNotEmpty() }

            preferencesStore.setGeminiApiKey(normalizedGemini)
            preferencesStore.setWhat3wordsApiKey(normalizedWhat3words)

            _uiState.update { current ->
                current.copy(
                    geminiApiKey = normalizedGemini,
                    what3wordsApiKey = normalizedWhat3words,
                    showByokSetup = false
                )
            }
        }
    }

    /**
     * User skips optional BYOK API key configuration (or closes the BYOK dialog)
     * to proceed into the application using default settings.
     */
    fun skipByokSetup() {
        _uiState.update { current ->
            current.copy(showByokSetup = false)
        }
    }
}
