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
import androidx.lifecycle.ViewModel
import com.benoitletondor.pixelminimalwatchfacecompanion.BatteryStatusBroadcastReceiver
import com.benoitletondor.pixelminimalwatchfacecompanion.device.Device
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class PhoneBatterySyncScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: Storage,
): ViewModel() {
    fun setBatterySyncActivated() {
        storage.setBatterySyncActivated(true)
        BatteryStatusBroadcastReceiver.subscribeToUpdates(context)
    }
}