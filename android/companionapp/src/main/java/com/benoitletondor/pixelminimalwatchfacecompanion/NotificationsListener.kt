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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject lateinit var sync: Sync
    @Inject lateinit var storage: Storage

    private var currentSyncActivatedWatchingJob: Job? = null
    private var currentAppsDisabledWatchingJob: Job? = null
    private var currentSyncJob: Job? = null
    private var latestSentNotificationsData: Sync.NotificationsData? = null
    private var filteredApps: Set<String> = emptySet()

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (DEBUG_LOGS) Log.d(TAG, "onListenerConnected")

        currentInstance = this

        currentSyncActivatedWatchingJob?.cancel()
        currentSyncActivatedWatchingJob = scope.launch {
            storage
                .isNotificationsSyncActivatedFlow()
                .distinctUntilChanged()
                .collect { syncActivated ->
                    latestSentNotificationsData = null

                    if (syncActivated) {
                        onChange()
                    }
                }
        }

        currentAppsDisabledWatchingJob?.cancel()
        currentAppsDisabledWatchingJob = scope.launch {
            storage.watchNotificationSyncDisabledPackages()
                .collect {
                    filteredApps = it
                    onChange()
                }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (DEBUG_LOGS) Log.d(TAG, "onNotificationPosted: $sbn")
        onChange()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (DEBUG_LOGS) Log.d(TAG, "onNotificationRemoved: $sbn")
        onChange()
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap?) {
        super.onNotificationRankingUpdate(rankingMap)
        if (DEBUG_LOGS) Log.d(TAG, "onNotificationRankingUpdate: $rankingMap")
        onChange()
    }

    override fun onDestroy() {
        if (DEBUG_LOGS) Log.d(TAG, "onDestroy")

        scope.cancel()
        currentSyncJob = null
        currentSyncActivatedWatchingJob = null
        currentInstance = null
        currentAppsDisabledWatchingJob = null
        super.onDestroy()
    }

    private fun onChange() {
        if (!storage.isNotificationsSyncActivated()) {
            if (DEBUG_LOGS) Log.d(TAG, "onChange: isNotificationsSyncActivated is false, skipping")
            return
        }

        currentSyncJob?.cancel()
        currentSyncJob = scope.launch {
            if (DEBUG_LOGS) Log.d(TAG, "onChange: currentSyncJob start")

            try {
                val groupIds = mutableSetOf<String>()
                val iconIds = mutableListOf<Int>()
                val iconIdsToIcons = mutableMapOf<Int, Icon>()
                activeNotifications.forEach { notification ->
                    if (notification.isOngoing) {
                        if (DEBUG_LOGS) Log.d(TAG, "onChange: Ignoring because ongoing $notification")
                        return@forEach
                    }

                    if (notification.packageName in filteredApps) {
                        if (DEBUG_LOGS) Log.d(TAG, "onChange: Ignoring because filtered $notification")
                        return@forEach
                    }

                    if (notification.groupKey.isNullOrBlank() || notification.groupKey !in groupIds) {
                        iconIds.add(notification.notification.smallIcon.id())
                        iconIdsToIcons[notification.notification.smallIcon.id()] = notification.notification.smallIcon

                        groupIds.add(notification.groupKey)
                    }
                }

                if (DEBUG_LOGS) Log.d(TAG, "onChange: iconIds: $iconIds")

                if (iconIds != latestSentNotificationsData?.iconIds) {
                    val data = Sync.NotificationsData(iconIds, iconIdsToIcons)
                    if (DEBUG_LOGS) Log.d(TAG, "onChange: sendActiveNotifications: $data")
                    sync.sendActiveNotifications(data)

                    if (DEBUG_LOGS) Log.d(TAG, "onChange: done, storing: $data")
                    latestSentNotificationsData = data
                } else {
                    if (DEBUG_LOGS) Log.d(TAG, "onChange: ignoring as same as before")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e

                Log.e(TAG, "Failed to send active notifications", e)
            }
        }
    }

    private fun Icon.id(): Int = toString().hashCode()

    object NotificationsListenerPermissionResultContract : ActivityResultContract<Unit, Unit>() {
        override fun createIntent(context: Context, input: Unit): Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            androidRPermissionIntent(context)
        } else {
            askPermissionDefaultIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?) = Unit
    }

    object FallbackNotificationsListenerPermissionResultContract : ActivityResultContract<Unit, Unit>() {
        override fun createIntent(context: Context, input: Unit): Intent = askPermissionDefaultIntent(context)

        override fun parseResult(resultCode: Int, intent: Intent?) = Unit
    }

    companion object {
        private var currentInstance: NotificationsListener? = null

        private const val TAG = "NotificationsListener"
        private val DEBUG_LOGS = BuildConfig.DEBUG

        @RequiresApi(Build.VERSION_CODES.R)
        private fun androidRPermissionIntent(context: Context): Intent {
            return Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    ComponentName(context, NotificationsListener::class.java).flattenToString(),
                )
            }
        }

        private fun askPermissionDefaultIntent(context: Context): Intent {
            return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                val value = "${context.packageName}/${NotificationsListener::class.java.name}"
                val key = ":settings:fragment_args_key"
                putExtra(key, value)
                putExtra(":settings:show_fragment_args", Bundle().also { it.putString(key, value) })
            }
        }

        fun onSyncActivated() {
            if (currentInstance == null) {
                Log.e(TAG, "onSyncActivated called without a NotificationsListener instance")
            }

            currentInstance?.latestSentNotificationsData = null
            currentInstance?.onChange()
        }
    }
}