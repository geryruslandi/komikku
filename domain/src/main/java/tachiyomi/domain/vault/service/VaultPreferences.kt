package tachiyomi.domain.vault.service

import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

/**
 * Persisted state of the private vault.
 *
 * The active profile is app state (not a user setting), so it is excluded from backups and is
 * only changed when the app restarts into the other profile.
 */
class VaultPreferences(
    private val preferenceStore: PreferenceStore,
) {

    fun isActive() = preferenceStore.getBoolean(Preference.appStateKey(KEY_ACTIVE), false)

    companion object {
        const val KEY_ACTIVE = "vault_active"
    }
}
