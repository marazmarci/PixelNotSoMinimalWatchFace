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

import kotlinx.coroutines.flow.Flow

interface Storage {
    fun setOnboardingFinished(finished: Boolean)
    fun isOnboardingFinished(): Boolean
    fun isBatterySyncActivatedFlow(): Flow<Boolean>
    fun isBatterySyncActivated(): Boolean
    fun setBatterySyncActivated(activated: Boolean)
    fun isNotificationsSyncActivated(): Boolean
    fun setNotificationsSyncActivated(activated: Boolean)
    fun isNotificationsSyncActivatedFlow(): Flow<Boolean>
    fun isForegroundServiceEnabled(): Boolean
    fun setForegroundServiceEnabled(enabled: Boolean)
    fun watchNotificationSyncDisabledPackages(): Flow<Set<String>>
    fun setNotificationsSyncAppDisabled(packageName: String)
    fun removeNotificationsSyncAppDisabled(packageName: String)
    fun getRecentCustomColors(): List<Int>
    fun setRecentCustomColors(colors: List<Int>)
}