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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.MutableLiveFlow
import com.benoitletondor.pixelminimalwatchfacecompanion.platform.PhonePlatform
import com.benoitletondor.pixelminimalwatchfacecompanion.platform.SyncSession
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import com.benoitletondor.pixelminimalwatchfacecompanion.view.NAV_SETTINGS_NODE_ROUTE_ARG
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class NodeSettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val billing: Billing,
    private val storage: Storage,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val nodeId: String = savedStateHandle.get<String>(NAV_SETTINGS_NODE_ROUTE_ARG)
        ?: throw IllegalStateException("Unable to find node id arg")

    private val syncSession = SyncSession(
        Wearable.getMessageClient(appContext),
        nodeId,
    )

    private val stateMutableFlow = MutableStateFlow<State>(State.InitializingSession)
    val stateFlow: StateFlow<State> = stateMutableFlow

    private val eventMutableFlow = MutableLiveFlow<Event>()
    val eventFlow: Flow<Event> = eventMutableFlow

    init {
        getInitialWatchState()
    }

    override fun onCleared() {
        syncSession.close()
        currentSettingsPlatform = null
        super.onCleared()
    }

    private fun getInitialWatchState() {
        if (DEBUG_LOGS) Log.d(TAG, "getInitialWatchState: start")

        stateMutableFlow.value = State.InitializingSession
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (DEBUG_LOGS) Log.d(TAG, "getInitialWatchState: getWatchNodeVersion")

                val watchNodeVersion = syncSession.getWatchNodeVersion()
                val appVersion = BuildConfig.VERSION_CODE - 10000 // TODO: inject that ideally

                if (DEBUG_LOGS) Log.d(TAG, "getInitialWatchState: watchNodeVersion: $watchNodeVersion, appVersion: $appVersion")

                if (watchNodeVersion != appVersion) {
                    stateMutableFlow.value = State.IncompatibleVersion(watchNodeVersion, appVersion)
                } else {
                    if (DEBUG_LOGS) Log.d(TAG, "getInitialWatchState: getInitialState")
                    val initialState = syncSession.getInitialState()
                    if (DEBUG_LOGS) Log.d(TAG, "getInitialWatchState: initialState: $initialState")

                    val platform = PhonePlatform(appContext, billing, syncSession, initialState, storage)
                    currentSettingsPlatform = platform
                    stateMutableFlow.value = State.Loaded(platform)
                }
            } catch (e: Exception) {
                if (e is CancellationException) { throw e }

                Log.e(TAG, "Error while getting watch node version", e)
                stateMutableFlow.value = State.Error(e)
            }
        }
    }

    fun onRetryButtonPressed() {
        getInitialWatchState()
    }

    fun onOpenPlayStoreOnPhonePressed() {
        viewModelScope.launch {
            eventMutableFlow.emit(Event.OpenPlayStore)
        }
    }

    fun onHowToActivateButtonPressed() {
        viewModelScope.launch {
            eventMutableFlow.emit(Event.ShowActivationInstructions)
        }
    }

    sealed class State {
        object InitializingSession : State()
        class IncompatibleVersion(val watchNodeVersion: Int, val appVersion: Int) : State()
        class Error(val e: Exception) : State()
        class Loaded(val platform: PhonePlatform) : State()
    }

    sealed class Event {
        object OpenPlayStore : Event()
        object ShowActivationInstructions: Event()
    }

    companion object {
        private const val TAG = "NodeSettingsViewModel"
        private val DEBUG_LOGS = BuildConfig.DEBUG

        var currentSettingsPlatform: PhonePlatform? = null
    }
}