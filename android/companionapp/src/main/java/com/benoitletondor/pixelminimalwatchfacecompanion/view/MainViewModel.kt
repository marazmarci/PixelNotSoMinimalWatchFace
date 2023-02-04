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
package com.benoitletondor.pixelminimalwatchfacecompanion.view

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.PremiumCheckStatus
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.PurchaseFlowResult
import com.benoitletondor.pixelminimalwatchfacecompanion.config.Config
import com.benoitletondor.pixelminimalwatchfacecompanion.config.getWarning
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.MutableLiveFlow
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.combine
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val billing: Billing,
    private val sync: Sync,
    private val config: Config,
    storage: Storage
) : ViewModel(), CapabilityClient.OnCapabilityChangedListener {
    private val navigationEventMutableFlow = MutableLiveFlow<NavigationDestination>()
    val navigationEventFlow: Flow<NavigationDestination> = navigationEventMutableFlow

    private val errorEventMutableFlow = MutableLiveFlow<ErrorType>()
    val errorEventFlow: Flow<ErrorType> = errorEventMutableFlow

    private val eventMutableFlow = MutableLiveFlow<EventType>()
    val eventFlow: Flow<EventType> = eventMutableFlow

    private val lastSyncedPremiumStatusStateFlow = MutableStateFlow<Boolean?>(null)
    private val isSyncingStateFlow = MutableStateFlow(false)
    private val userIsBuyingPremiumStateFlow = MutableStateFlow(false)
    private val appInstalledStatusStateFlow = MutableStateFlow<AppInstalledStatus>(
        AppInstalledStatus.Unknown
    )

    private val userForcedInstallStatusFlow = MutableStateFlow(UserForcedInstallStatus.UNSPECIFIED)

    private val currentStepFlow = combine(
        billing.userPremiumEventStream,
        userIsBuyingPremiumStateFlow,
        appInstalledStatusStateFlow,
        isSyncingStateFlow,
        userForcedInstallStatusFlow,
        storage.isBatterySyncActivatedFlow(),
        storage.isNotificationsSyncActivatedFlow(),
        flow { emit(config.getWarning()) },
        Companion::computeStep,
    ).stateIn(viewModelScope, SharingStarted.Eagerly, Step.Loading)
    val stepFlow: Flow<Step> = currentStepFlow
    val step: Step get() = currentStepFlow.value

    init {
        if( !storage.isOnboardingFinished() ) {
            viewModelScope.launch {
                navigationEventMutableFlow.emit(NavigationDestination.Onboarding)
            }
        }

        viewModelScope.launch {
            billing.userPremiumEventStream
                .collect { premiumStatus ->
                    if( (premiumStatus == PremiumCheckStatus.Premium && lastSyncedPremiumStatusStateFlow.value == false) ||
                        (premiumStatus == PremiumCheckStatus.NotPremium && lastSyncedPremiumStatusStateFlow.value == true) ||
                        (premiumStatus == PremiumCheckStatus.Premium || premiumStatus == PremiumCheckStatus.NotPremium) && lastSyncedPremiumStatusStateFlow.value == null ) {
                        syncState(premiumStatus == PremiumCheckStatus.Premium)
                    }
                }
        }

        syncAppInstalledStatus()
        sync.subscribeToCapabilityChanges(this)
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        syncAppInstalledStatus()
    }

    private fun syncState(userPremium: Boolean) {
        viewModelScope.launch {
            try {
                isSyncingStateFlow.value = true

                withContext(Dispatchers.IO) {
                    sync.sendPremiumStatus(userPremium)
                }

                lastSyncedPremiumStatusStateFlow.value = userPremium

                if(userPremium && (step is Step.Premium || step == Step.Syncing)) {
                    eventMutableFlow.emit(EventType.SYNC_WITH_WATCH_SUCCEED)
                }
            } catch (t: Throwable) {
                if (t is CancellationException) {
                    throw t
                }

                if (userPremium && (step is Step.Premium || step == Step.Syncing)) {
                    errorEventMutableFlow.emit(ErrorType.ErrorWhileSyncingWithWatch(t))
                }
            } finally {
                isSyncingStateFlow.value = false
            }
        }
    }

    override fun onCleared() {
        sync.unsubscribeToCapabilityChanges(this)

        super.onCleared()
    }

    fun triggerSync() {
        syncState(billing.isUserPremium())
    }

    fun retryPremiumStatusCheck() {
        billing.updatePremiumStatusIfNeeded()
    }

    fun launchPremiumBuyFlow(host: Activity) {
        viewModelScope.launch {
            try {
                userIsBuyingPremiumStateFlow.value = true

                val result = withContext(Dispatchers.IO) {
                    billing.launchPremiumPurchaseFlow(host)
                }

                // Success result will be handled automatically as notification to userPremiumEventObserver

                if( result is PurchaseFlowResult.Error ){
                    errorEventMutableFlow.emit(ErrorType.UnableToPay(Exception(result.reason)))
                }
            } catch (t: Throwable) {
                if (t is CancellationException) {
                    throw t
                }

                errorEventMutableFlow.emit(ErrorType.UnableToPay(t))
            } finally {
                userIsBuyingPremiumStateFlow.value = false
            }
        }
    }

    fun onVoucherInput(voucher: String) {
        viewModelScope.launch {
            navigationEventMutableFlow.emit(NavigationDestination.VoucherRedeem(voucher))
        }
    }

    fun onGoToInstallWatchFaceButtonPressed() {
        viewModelScope.launch {
            userForcedInstallStatusFlow.emit(UserForcedInstallStatus.UNINSTALLED)
        }
    }

    fun onWatchFaceInstalledButtonPressed() {
        viewModelScope.launch {
            userForcedInstallStatusFlow.emit(UserForcedInstallStatus.INSTALLED)
        }
    }

    fun onSupportButtonPressed() {
        viewModelScope.launch {
            eventMutableFlow.emit(EventType.OPEN_SUPPORT_EMAIL)
        }
    }

    fun onDebugPhoneBatteryIndicatorButtonPressed() {
        viewModelScope.launch {
            navigationEventMutableFlow.emit(NavigationDestination.DebugBatterySync)
        }
    }

    fun onSetupNotificationsSyncButtonPressed() {
        viewModelScope.launch {
            navigationEventMutableFlow.emit(NavigationDestination.SetupNotificationsSync)
        }
    }

    fun onSettingsButtonPressed() {
        viewModelScope.launch {
            navigationEventMutableFlow.emit(NavigationDestination.Settings)
        }
    }

    fun onInstallWatchFaceButtonPressed() {
        viewModelScope.launch {
            try {
                if (withTimeoutOrNull(1000) { sync.openPlayStoreOnWatch() } == true) {
                    eventMutableFlow.emit(EventType.PLAY_STORE_OPENED_ON_WATCH)
                } else {
                    errorEventMutableFlow.emit(ErrorType.UnableToOpenPlayStoreOnWatch)
                }
            } catch (t: Throwable) {
                if (t is CancellationException) {
                    throw t
                }

                Log.e("MainViewModel", "Error opening PlayStore", t)
                errorEventMutableFlow.emit(ErrorType.UnableToOpenPlayStoreOnWatch)
            }
        }
    }

    fun onRedeemPromoCodeButtonPressed() {
        viewModelScope.launch {
            eventMutableFlow.emit(EventType.SHOW_VOUCHER_INPUT)
        }
    }

    fun onDonateButtonPressed() {
        viewModelScope.launch {
            navigationEventMutableFlow.emit(NavigationDestination.Donate)
        }
    }

    private fun syncAppInstalledStatus() {
        viewModelScope.launch {
            appInstalledStatusStateFlow.value = AppInstalledStatus.Verifying
            appInstalledStatusStateFlow.value = AppInstalledStatus.Result(sync.getWearableStatus())
        }
    }

    fun onNavigateToInstallApp() {
        viewModelScope.launch {
            userForcedInstallStatusFlow.emit(UserForcedInstallStatus.UNINSTALLED)
        }
    }

    fun onNavigateToDonationScreen() {
        viewModelScope.launch {
            navigationEventMutableFlow.emit(NavigationDestination.Donate)
        }
    }

    sealed class Step {
        object Loading : Step()
        object Syncing : Step()
        class Error(val error: Throwable) : Step()
        class InstallWatchFace(val appInstalledStatus: AppInstalledStatus) : Step()
        object NotPremium : Step()
        class Premium(
            val isBatterySyncActivated: Boolean,
            val isNotificationsSyncActivated: Boolean,
            val maybeWarning: String?,
        ) : Step()
    }

    sealed class NavigationDestination {
        object Onboarding : NavigationDestination()
        class VoucherRedeem(val voucherCode: String) : NavigationDestination()
        object Donate : NavigationDestination()
        object DebugBatterySync : NavigationDestination()
        object SetupNotificationsSync : NavigationDestination()
        object Settings : NavigationDestination()
    }

    sealed class ErrorType {
        class ErrorWhileSyncingWithWatch(val error: Throwable) : ErrorType()
        class UnableToPay(val error: Throwable) : ErrorType()
        object UnableToOpenPlayStoreOnWatch : ErrorType()
    }

    enum class EventType {
        PLAY_STORE_OPENED_ON_WATCH,
        SYNC_WITH_WATCH_SUCCEED,
        SHOW_VOUCHER_INPUT,
        OPEN_SUPPORT_EMAIL,
    }

    sealed class AppInstalledStatus {
        object Unknown : AppInstalledStatus()
        object Verifying : AppInstalledStatus()
        class Result(val wearableStatus: Sync.WearableStatus) : AppInstalledStatus()
    }

    private enum class UserForcedInstallStatus {
        UNSPECIFIED, INSTALLED, UNINSTALLED
    }

    companion object {
        private fun computeStep(
            premiumStatus: PremiumCheckStatus,
            userIsBuyingPremium: Boolean,
            appInstalledStatus: AppInstalledStatus,
            isSyncing: Boolean,
            userForcedInstallStatus: UserForcedInstallStatus,
            isBatterySyncActivated: Boolean,
            isNotificationsSyncActivated: Boolean,
            maybeWarning: String?,
        ) : Step {
            if (userIsBuyingPremium) {
                return Step.Loading
            }

            if (isSyncing) {
                return Step.Syncing
            }

            if (userForcedInstallStatus == UserForcedInstallStatus.UNINSTALLED) {
                return Step.InstallWatchFace(appInstalledStatus)
            }

            return when(premiumStatus) {
                PremiumCheckStatus.Checking -> Step.Loading
                is PremiumCheckStatus.Error -> Step.Error(premiumStatus.error)
                PremiumCheckStatus.Initializing -> Step.Loading
                PremiumCheckStatus.NotPremium -> Step.NotPremium
                PremiumCheckStatus.Premium -> Step.Premium(
                    isBatterySyncActivated,
                    isNotificationsSyncActivated,
                    maybeWarning,
                )
            }
        }
    }
}