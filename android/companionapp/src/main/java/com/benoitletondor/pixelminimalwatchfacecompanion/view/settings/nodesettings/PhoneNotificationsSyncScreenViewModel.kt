package com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings

import androidx.lifecycle.ViewModel
import com.benoitletondor.pixelminimalwatchfacecompanion.device.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PhoneNotificationsSyncScreenViewModel @Inject constructor(
    private val device: Device,
): ViewModel() {
    val hasNotificationsListenerPermission get() = device.hasNotificationsListenerPermission()
}