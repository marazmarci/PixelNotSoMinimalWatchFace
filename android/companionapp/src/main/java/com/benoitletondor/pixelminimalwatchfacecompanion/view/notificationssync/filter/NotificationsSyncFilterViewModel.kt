package com.benoitletondor.pixelminimalwatchfacecompanion.view.notificationssync.filter

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benoitletondor.pixelminimalwatchfacecompanion.device.Device
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsSyncFilterViewModel @Inject constructor(
    private val storage: Storage,
    private val device: Device,
) : ViewModel() {
    private val retryAfterErrorTriggerFlow = MutableSharedFlow<Unit>()

    val stateFlow: StateFlow<State> = combine(
        flow { emit(device.listAllApps()) }.flowOn(Dispatchers.IO),
        storage.watchNotificationSyncDisabledPackages(),
    ) { apps, disabledAppsPackages ->
        State.Loaded(
            apps = apps.map { appInfo ->
                App(
                    appInfo = appInfo,
                    disabled = disabledAppsPackages.contains(appInfo.packageName),
                )
            }
        ).eraseType()
    }
    .retryWhen { e, _ ->
        Log.e("NotificationsSyncFilterViewModel", "Error getting filtered apps", e)

        emit(State.Error(e))

        retryAfterErrorTriggerFlow.first()
        emit(State.Loading)
        true
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    fun onRetryButtonPressed() {
        viewModelScope.launch {
            retryAfterErrorTriggerFlow.emit(Unit)
        }
    }

    fun onAppFilteringChanged(appInfo: Device.AppInfo, filtered: Boolean) {
        if (filtered) {
            storage.setNotificationsSyncAppDisabled(appInfo.packageName)
        } else {
            storage.removeNotificationsSyncAppDisabled(appInfo.packageName)
        }
    }

    @Stable
    @Immutable
    sealed class State {
        @Stable
        @Immutable
        object Loading : State()

        @Stable
        @Immutable
        data class Loaded(
            @Stable
            val apps: List<App>
        ) : State()

        @Stable
        @Immutable
        data class Error(val exception: Throwable) : State()

        fun eraseType(): State = this
    }

    @Stable
    @Immutable
    data class App(
        val appInfo: Device.AppInfo,
        val disabled: Boolean,
    )
}