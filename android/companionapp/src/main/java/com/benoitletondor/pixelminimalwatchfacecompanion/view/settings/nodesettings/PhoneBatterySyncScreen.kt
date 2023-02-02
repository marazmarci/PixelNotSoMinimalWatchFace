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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.platform.PhonePlatform
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings.PhoneSettingsComposeComponents
import com.benoitletondor.pixelminimalwatchfacecompanion.view.NAV_DEBUG_PHONE_BATTERY_SYNC_ROUTE

@Composable
fun PhoneBatterySyncScreen(
    navController: NavController,
    phonePlatform: PhonePlatform,
    composeComponents: PhoneSettingsComposeComponents,
    viewModel: PhoneBatterySyncScreenViewModel,
) {
    AppTopBarScaffold(
        navController = navController,
        showBackButton = true,
        title = "Phone battery indicator setup",
        content = {
            Content(
                phonePlatform = phonePlatform,
                composeComponents = composeComponents,
                onDebugButtonPressed = {
                    viewModel.setBatterySyncActivated()
                    navController.navigate(NAV_DEBUG_PHONE_BATTERY_SYNC_ROUTE)
                }
            )
        }
    )
}

@Composable
private fun Content(
    phonePlatform: PhonePlatform,
    composeComponents: PhoneSettingsComposeComponents,
    onDebugButtonPressed: () -> Unit,
) {
    val isPhoneBatterySyncActivated by phonePlatform.watchShowPhoneBattery().collectAsState(phonePlatform.showPhoneBattery())

    composeComponents.PlatformLazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        item(key = "Title") {
            Text(
                text = "You can display the phone battery at the bottom of the watch face to get the info at a glance",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item(key = "SyncButton") {
            composeComponents.SettingToggleChip(
                checked = isPhoneBatterySyncActivated,
                onCheckedChange = phonePlatform::setShowPhoneBattery,
                label = "Phone battery as bottom widget",
                iconDrawable = null,
                modifier = Modifier.padding(bottom = 30.dp)
            )
        }

        if (isPhoneBatterySyncActivated) {
            item(key = "DebugButton") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Text(
                        text = "Experiencing any issue?",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Button(
                        colors = blueButtonColors(),
                        onClick = onDebugButtonPressed,
                    ) {
                        Text(
                            text = "Debug phone battery sync",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                        )
                    }
                }
            }
        }
    }
}