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
package com.benoitletondor.pixelminimalwatchfacecompanion.storage

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

private const val SHARED_PREFERENCES_FILE_NAME = "sharedPref"

private const val ONBOARDING_FINISHED_KEY = "onboarding_finished"
private const val BATTERY_SYNC_ACTIVATED = "onboarding_finished"
private const val FOREGROUND_SERVICE_ENABLED_KEY = "foreground_service_enabled"
private const val NOTIFICATIONS_SYNC_ENABLED_KEY = "notifications_sync_enabled"
private const val NOTIFICATIONS_SYNC_FILTERED_APPS_KEY = "notifications_sync_filtered_apps"

class StorageImpl @Inject constructor(@ApplicationContext context: Context) : Storage {
    private val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    private val batterySyncActivatedMutableFlow = MutableStateFlow(isBatterySyncActivated())
    private val notificationsSyncActivatedMutableFlow = MutableStateFlow(isNotificationsSyncActivated())

    override fun setOnboardingFinished(finished: Boolean) {
        sharedPreferences.edit {
            putBoolean(ONBOARDING_FINISHED_KEY, finished)
        }
    }

    override fun isOnboardingFinished(): Boolean
        = sharedPreferences.getBoolean(ONBOARDING_FINISHED_KEY, false)

    override fun isBatterySyncActivatedFlow(): Flow<Boolean> = batterySyncActivatedMutableFlow

    override fun isBatterySyncActivated(): Boolean
        = sharedPreferences.getBoolean(BATTERY_SYNC_ACTIVATED, false)

    override fun setBatterySyncActivated(activated: Boolean) {
        sharedPreferences.edit {
            putBoolean(BATTERY_SYNC_ACTIVATED, activated)
        }

        batterySyncActivatedMutableFlow.value = activated
    }

    override fun isNotificationsSyncActivated(): Boolean
        = sharedPreferences.getBoolean(NOTIFICATIONS_SYNC_ENABLED_KEY, false)

    override fun setNotificationsSyncActivated(activated: Boolean) {
        sharedPreferences.edit {
            putBoolean(NOTIFICATIONS_SYNC_ENABLED_KEY, activated)
        }

        notificationsSyncActivatedMutableFlow.value = activated
    }

    override fun isNotificationsSyncActivatedFlow(): Flow<Boolean> = notificationsSyncActivatedMutableFlow

    override fun isForegroundServiceEnabled(): Boolean = sharedPreferences.getBoolean(FOREGROUND_SERVICE_ENABLED_KEY, false)

    override fun setForegroundServiceEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(FOREGROUND_SERVICE_ENABLED_KEY, enabled)
        }
    }

    override fun watchNotificationSyncDisabledPackages(): Flow<Set<String>> {
        return getOrCreateNotificationsSyncDisbaledPackageCache()
    }

    override fun setNotificationsSyncAppDisabled(packageName: String) {
        val cache = getOrCreateNotificationsSyncDisbaledPackageCache()
        cache.value = cache.value.plus(packageName)

        sharedPreferences.edit {
            putStringSet(NOTIFICATIONS_SYNC_FILTERED_APPS_KEY, cache.value)
        }
    }

    override fun removeNotificationsSyncAppDisabled(packageName: String) {
        val cache = getOrCreateNotificationsSyncDisbaledPackageCache()
        cache.value = cache.value.minus(packageName)

        sharedPreferences.edit {
            putStringSet(NOTIFICATIONS_SYNC_FILTERED_APPS_KEY, cache.value)
        }
    }

    private var notificationSyncDisabledPackagesCache: MutableStateFlow<Set<String>>? = null
    private fun getOrCreateNotificationsSyncDisbaledPackageCache(): MutableStateFlow<Set<String>> {
        val notificationSyncDisabledPackagesCache = notificationSyncDisabledPackagesCache
        if (notificationSyncDisabledPackagesCache != null) {
            return notificationSyncDisabledPackagesCache
        }

        val createdCache = MutableStateFlow(sharedPreferences.getStringSet(NOTIFICATIONS_SYNC_FILTERED_APPS_KEY, emptySet()) ?: emptySet())
        this.notificationSyncDisabledPackagesCache = createdCache
        return createdCache
    }
}