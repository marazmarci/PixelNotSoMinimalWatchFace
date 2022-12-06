/*
 *   Copyright 2022 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.benoitletondor.pixelminimalwatchfacecompanion.device

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.benoitletondor.pixelminimalwatchfacecompanion.ForegroundService
import com.benoitletondor.pixelminimalwatchfacecompanion.NotificationsListener
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

// Large parts of this code are from
// https://gitlab.inria.fr/stopcovid19/stopcovid-android/-/blob/master/stopcovid/src/main/java/com/lunabeestudio/stopcovid/manager/ProximityManager.kt
class DeviceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : Device {

    private val powerManagerIntents = arrayOf(
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
        Intent().setComponent(ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        ),
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
        ),
        Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
        Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
        Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
        Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
        Intent().setComponent(ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
        Intent().setComponent(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))
    )

    override fun isBatteryOptimizationOff(): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            (pm?.isIgnoringBatteryOptimizations(context.packageName) == true
                    || !hasActivityToResolveIgnoreBatteryOptimization(context))
        } catch (e: Exception) {
            Log.e("DeviceImpl", "Unable to fetch battery optimization state, defaulting to true", e)
            true
        }
    }

    override fun getBatteryOptimizationOptOutIntents(): List<Intent> = getIgnoreBatteryOptimizationIntents(context)

    override fun startForegroundService() = ForegroundService.start(context)

    override fun isForegroundServiceStarted(): Boolean = ForegroundService.isActivated()

    override fun finishForegroundService() = ForegroundService.stop(context)

    override fun hasNotificationsListenerPermission(): Boolean {
        return try {
            val enabledNotificationListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
            enabledNotificationListeners.contains(context.packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check notification listener permission", e)
            false
        }
    }

    override suspend fun listAllApps(): List<Device.AppInfo> {
        return withContext(Dispatchers.IO) {
            val appInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstalledApplications(0)
            }

            return@withContext appInfos
                .filter { it.enabled }
                .map {
                    Device.AppInfo(
                        packageName = it.packageName,
                        appName = context.packageManager.getApplicationLabel(it).toString(),
                        icon = context.packageManager.getApplicationIcon(it),
                    )
                }
                .sortedBy { it.appName }
        }
    }

    private fun hasActivityToResolveIgnoreBatteryOptimization(context: Context): Boolean {
        val powerIntents = getIgnoreBatteryOptimizationIntents(context)
        for (intent in powerIntents) {
            intent.apply {
                putExtra("package_name", context.packageName)
                putExtra("package_label", context.getString(R.string.app_name))
            }
            val resolveInfo = intent.resolveActivityInfo(context.packageManager, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolveInfo?.exported == true) {
                return true
            }
        }

        return false
    }

    @SuppressLint("BatteryLife")
    private fun getIgnoreBatteryOptimizationIntents(context: Context): List<Intent> {
        val miuiIntent = Intent("miui.intent.action.HIDDEN_APPS_CONFIG_ACTIVITY")
        miuiIntent.putExtra("package_name", context.packageName)
        miuiIntent.putExtra("package_label", context.getString(R.string.app_name))
        val systemIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        val powerIntents = arrayListOf(miuiIntent, systemIntent)
        powerIntents.addAll(powerManagerIntents)
        return powerIntents
    }

    companion object {
        private const val TAG = "Device"
    }
}