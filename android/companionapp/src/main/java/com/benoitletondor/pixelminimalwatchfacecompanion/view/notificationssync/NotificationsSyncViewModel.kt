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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.notificationssync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benoitletondor.pixelminimalwatchfacecompanion.device.Device
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.MutableLiveFlow
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsSyncViewModel @Inject constructor(
    storage: Storage,
    private val device: Device,
    private val sync: Sync,
) : ViewModel() {
    private val eventMutableFlow = MutableLiveFlow<Event>()
    val eventFlow: Flow<Event> = eventMutableFlow

    private val hasNotificationsListenerPermissionMutableFlow = MutableStateFlow(device.hasNotificationsListenerPermission())

    val stateFlow = combine(
        hasNotificationsListenerPermissionMutableFlow
            .handleSideEffects(),
        storage.isNotificationsSyncActivatedFlow(),
        ::buildState
    ).stateIn(viewModelScope, SharingStarted.Eagerly, buildState(
        hasNotificationsListenerPermission = hasNotificationsListenerPermissionMutableFlow.value,
        isNotificationsSyncActivated = storage.isNotificationsSyncActivated(),
    ))

    fun onAskPermissionButtonPressed() {
        viewModelScope.launch {
            eventMutableFlow.emit(Event.OpenNotificationsPermissionActivity)
        }
    }

    fun onSupportButtonPressed() {
        viewModelScope.launch {
            eventMutableFlow.emit(Event.OpenSupportEmail)
        }
    }

    fun onFilterAppsButtonPressed() {
        viewModelScope.launch {
            eventMutableFlow.emit(Event.OpenFilterApps)
        }
    }

    fun onPermissionActivityResult() {
        hasNotificationsListenerPermissionMutableFlow.value = device.hasNotificationsListenerPermission()
    }

    private fun StateFlow<Boolean>.handleSideEffects(): Flow<Boolean> {
        return onEach {
            sync.sendNotificationsSyncStatus(
                if (it) {
                    Sync.NotificationsSyncStatus.ACTIVATED
                } else {
                    Sync.NotificationsSyncStatus.ACTIVATED_MISSING_PERMISSION
                }
            )
        }
    }

    sealed class Event {
        object OpenNotificationsPermissionActivity : Event()
        object OpenSupportEmail : Event()
        object OpenFilterApps : Event()
    }

    sealed class State {
        object Deactivated : State()
        object ActivatedNoPermission : State()
        object Activated : State()
    }

    companion object {
        private fun buildState(
            hasNotificationsListenerPermission: Boolean,
            isNotificationsSyncActivated: Boolean,
        ): State {
            if (!isNotificationsSyncActivated) {
                return State.Deactivated
            }

            if (!hasNotificationsListenerPermission) {
                return State.ActivatedNoPermission
            }

            return State.Activated
        }
    }
}