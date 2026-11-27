package eu.kanade.tachiyomi.data.vault

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.data.backup.create.BackupCreateJob
import eu.kanade.tachiyomi.data.backup.restore.BackupRestoreJob
import eu.kanade.tachiyomi.data.sync.SyncDataJob
import tachiyomi.core.common.Constants
import tachiyomi.core.common.preference.Preference
import tachiyomi.domain.vault.service.VaultPreferences
import kotlin.system.exitProcess

/**
 * Switches between the standard library and the private vault.
 *
 * The switch itself is always done through an app restart: the database driver, cover cache and
 * download directory are selected once at startup from the persisted profile, so no query or
 * transaction can ever run against the wrong database.
 */
object VaultManager {

    fun isDeviceSecure(context: Context): Boolean {
        if (context.getSystemService<KeyguardManager>()?.isDeviceSecure != true) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return true

        return BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Critical writes must never be interrupted by the process exit that follows a profile switch.
     */
    fun canSwitch(context: Context): Boolean {
        return !BackupRestoreJob.isRunning(context) &&
            !BackupCreateJob.isManualJobRunning(context) &&
            !SyncDataJob.isRunning(context)
    }

    fun enter(context: Context) {
        setActive(context, active = true)
    }

    fun lock(context: Context) {
        setActive(context, active = false)
    }

    private fun setActive(context: Context, active: Boolean) {
        // commit() instead of apply(): the process is killed right after this call.
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit(commit = true) {
                putBoolean(Preference.appStateKey(VaultPreferences.KEY_ACTIVE), active)
            }

        restart(context)
    }

    private fun restart(context: Context) {
        val intent = Intent()
            .setClassName(context, Constants.MAIN_ACTIVITY)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        exitProcess(0)
    }
}
