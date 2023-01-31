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
package com.benoitletondor.pixelminimalwatchfacecompanion

import android.util.Log
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig.WATCH_CAPABILITY
import com.benoitletondor.pixelminimalwatchfacecompanion.device.Device
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class WatchMessageReceiver : WearableListenerService(), CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {
    @Inject lateinit var sync: Sync
    @Inject lateinit var storage: Storage
    @Inject lateinit var device: Device

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        when(messageEvent.path) {
            QUERY_BATTERY_SYNC_STATUS_PATH -> sendBatterySyncStatus(messageEvent.data)
            QUERY_BATTERY_ACTIVATED_SYNC_PATH -> activateBatterySync()
            QUERY_BATTERY_DEACTIVATED_SYNC_PATH -> deactivateBatterySync()
            QUERY_NOTIFICATIONS_SYNC_STATUS_PATH -> sendNotificationsSyncStatus(messageEvent.data)
            QUERY_NOTIFICATIONS_ACTIVATED_SYNC_PATH -> activateNotificationsSync()
            QUERY_NOTIFICATIONS_DEACTIVATED_SYNC_PATH -> deactivateNotificationsSync()
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)

        if (capabilityInfo.name != WATCH_CAPABILITY) {
            return
        }

        val watchNode = capabilityInfo.nodes.firstOrNull { it.isNearby } ?: capabilityInfo.nodes.firstOrNull()
        if (watchNode != null) {
            if (storage.isBatterySyncActivated()) {
                activateBatterySync()
            }

            if (storage.isNotificationsSyncActivated()) {
                activateNotificationsSync()
            }
        }
    }

    private fun sendBatterySyncStatus(watchData: ByteArray) {
        try {
            val syncActivatedOnWatch = watchData.first().toInt() == 1
            if (syncActivatedOnWatch) {
                activateBatterySync()
            } else {
                deactivateBatterySync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while sending battery sync status", e)
        }
    }

    private fun deactivateBatterySync() {
        storage.setBatterySyncActivated(false)
        BatteryStatusBroadcastReceiver.unsubscribeFromUpdates(this)

        storage.setForegroundServiceEnabled(false)
        device.finishForegroundService()

        launch {
            try {
                sync.sendBatterySyncStatus(false)
            } catch (e: Exception) {
                if (e is CancellationException) throw e

                Log.e(TAG, "Error while deactivating battery sync", e)
            }
        }
    }

    private fun activateBatterySync() {
        storage.setBatterySyncActivated(true)
        BatteryStatusBroadcastReceiver.subscribeToUpdates(this)

        launch {
            try {
                sync.sendBatterySyncStatus(true)
                sync.sendBatteryStatus(BatteryStatusBroadcastReceiver.getCurrentBatteryLevel(this@WatchMessageReceiver))
            } catch (e: Exception) {
                if (e is CancellationException) throw e

                Log.e(TAG, "Error while activating battery sync", e)
            }
        }
    }

    private fun sendNotificationsSyncStatus(watchData: ByteArray) {
        try {
            val syncActivatedOnWatch = watchData.first().toInt() == 1
            if (syncActivatedOnWatch) {
                activateNotificationsSync()
            } else {
                deactivateNotificationsSync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while sending notifications sync status", e)
        }
    }

    private fun deactivateNotificationsSync() {
        storage.setNotificationsSyncActivated(false)

        launch {
            try {
                sync.sendNotificationsSyncStatus(Sync.NotificationsSyncStatus.DEACTIVATED)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t

                Log.e(TAG, "Error while deactivating notifications sync", t)
            }
        }
    }

    private fun activateNotificationsSync() {
        storage.setNotificationsSyncActivated(true)

        if (device.hasNotificationsListenerPermission()) {
            launch {
                try {
                    sync.sendNotificationsSyncStatus(Sync.NotificationsSyncStatus.ACTIVATED)
                    NotificationsListener.onSyncActivated()
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t

                    Log.e(TAG, "Error while activating notifications sync", t)
                }
            }
        } else {
            launch {
                try {
                    sync.sendNotificationsSyncStatus(Sync.NotificationsSyncStatus.ACTIVATED_MISSING_PERMISSION)
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t

                    Log.e(TAG, "Error while activating notifications sync", t)
                }
            }
        }
    }

    companion object {
        private const val TAG = "WatchMessageReceiver"

        private const val QUERY_BATTERY_SYNC_STATUS_PATH = "/batterySync/queryStatus"
        private const val QUERY_BATTERY_ACTIVATED_SYNC_PATH = "/batterySync/activate"
        private const val QUERY_BATTERY_DEACTIVATED_SYNC_PATH = "/batterySync/deactivate"
        private const val QUERY_NOTIFICATIONS_SYNC_STATUS_PATH = "/notificationsSync/queryStatus"
        private const val QUERY_NOTIFICATIONS_ACTIVATED_SYNC_PATH = "/notificationsSync/activate"
        private const val QUERY_NOTIFICATIONS_DEACTIVATED_SYNC_PATH = "/notificationsSync/deactivate"
    }
}