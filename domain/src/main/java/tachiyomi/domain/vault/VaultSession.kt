package tachiyomi.domain.vault

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-process state of the private vault.
 *
 * The active profile is persisted in [tachiyomi.domain.vault.service.VaultPreferences] and only
 * changes across an app restart (see the vault manager in the app module), so [isActive] is
 * immutable for the lifetime of the process.
 */
object VaultSession {

    const val KEY_SUFFIX = "__vault"

    @Volatile
    private var _isActive: Boolean = false

    val isActive: Boolean
        get() = _isActive

    private val _requiresAuth = MutableStateFlow(true)

    /**
     * Whether the private vault is waiting for the device credential.
     *
     * Starts as `true` on every process start and becomes `true` again every time the app is
     * backgrounded.
     */
    val requiresAuth: StateFlow<Boolean> = _requiresAuth.asStateFlow()

    /**
     * Set while the device credential prompt is in flight so backgrounding the app for the prompt
     * itself does not immediately lock it again.
     */
    @Volatile
    var isAuthenticating: Boolean = false
        private set

    fun init(active: Boolean) {
        _isActive = active
        _requiresAuth.value = true
        isAuthenticating = false
    }

    fun onAuthenticating() {
        isAuthenticating = true
    }

    fun onAuthenticated() {
        isAuthenticating = false
        _requiresAuth.value = false
    }

    fun onAuthenticationDismissed() {
        isAuthenticating = false
        _requiresAuth.value = true
    }

    fun onAppBackgrounded() {
        if (!isAuthenticating) {
            _requiresAuth.value = true
        }
    }

    /**
     * Keys that hold IDs belonging to the active database (categories, manga ids for cover
     * metadata, ...) must not be shared between the standard library and the vault.
     */
    fun profileKey(key: String): String = if (isActive) "$key$KEY_SUFFIX" else key

    fun isVaultKey(key: String): Boolean = key.endsWith(KEY_SUFFIX)
}
