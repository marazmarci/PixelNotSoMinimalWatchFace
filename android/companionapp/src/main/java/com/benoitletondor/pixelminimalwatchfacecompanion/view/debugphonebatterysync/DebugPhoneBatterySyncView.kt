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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.debugphonebatterysync

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.ForegroundService
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.primaryBlue
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.primaryGreen

@Composable
fun DebugPhoneBatterySync(
    navController: NavController,
    viewModel: DebugPhoneBatterySyncViewModel,
) {
    val state: DebugPhoneBatterySyncViewModel.State
        by viewModel.stateFlow.collectAsState()

    val context = LocalContext.current

    val batteryOptimizationOptOutActivityResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.onBatteryOptimizationOptOutResult()
    }

    LaunchedEffect("events") {
        viewModel.eventLiveFlow.collect { event ->
            when(event) {
                DebugPhoneBatterySyncViewModel.Event.NavigateToDisableOptimizationActivity -> {
                    val intents = viewModel.device.getBatteryOptimizationOptOutIntents()
                    for(intent in intents) {
                        val resolveInfo = intent.resolveActivityInfo(
                            context.packageManager,
                            PackageManager.MATCH_DEFAULT_ONLY,
                        )
                        if (resolveInfo?.exported == true) {
                            batteryOptimizationOptOutActivityResultLauncher.launch(intent)
                            break
                        }
                    }
                }
                DebugPhoneBatterySyncViewModel.Event.ManageForegroundNotificationVisibility -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        intent.putExtra(Settings.EXTRA_CHANNEL_ID, ForegroundService.channelId)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    AppTopBarScaffold(
        navController = navController,
        showBackButton = true,
        title = "Debug phone battery sync",
        content = {
            DebugPhoneBatterySyncLayout(
                isBatteryOptimizationOff = state.isBatteryOptimizationOff,
                isAndroid13OrMore = Build.VERSION.SDK_INT >= 33,
                onDisableBatteryOptimizationButtonPressed = viewModel::onDisableBatteryOptimizationButtonPressed,
                isForegroundServiceOn = state.isForegroundServiceOn,
                onForegroundServiceSwitchedChanged = viewModel::onForegroundServiceSwitchedChanged,
                onNotificationSettingsButtonPressed = viewModel::onNotificationSettingsButtonPressed,
            )
        }
    )
}

@Composable
private fun DebugPhoneBatterySyncLayout(
    isBatteryOptimizationOff: Boolean,
    isAndroid13OrMore: Boolean,
    onDisableBatteryOptimizationButtonPressed: () -> Unit,
    isForegroundServiceOn: Boolean,
    onForegroundServiceSwitchedChanged: (Boolean) -> Unit,
    onNotificationSettingsButtonPressed: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Disable battery optimization",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isBatteryOptimizationOff) {
            Text(
                text = "✔️ Battery optimization is off.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
            )
        } else {
            Text(
                text = "The majority of phone battery sync issues can be resolved by disabling battery optimization for the companion app, so that the system doesn't kill the app in background.",
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Search for Pixel Minimal Watch Face and disable battery optimization.",
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDisableBatteryOptimizationButtonPressed,
                colors = blueButtonColors(),
            ) {
                Text(
                    text = "Open battery optimization settings",
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = if (isAndroid13OrMore) "Always-on mode" else "Always-on notification",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "If you still experience sync issues, you can try to activate always-on mode.",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isAndroid13OrMore) "It will go into active mode to prevent the system from killing the app." else "It will use a permanent notification to prevent the system from killing the app.",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Switch(
            checked = isForegroundServiceOn,
            onCheckedChange = onForegroundServiceSwitchedChanged,
        )

        Text(
            text = if (isForegroundServiceOn) {
                if (isAndroid13OrMore) "Always-on mode activated" else "Always-on notification activated"
            } else {
                if (isAndroid13OrMore) "Always-on mode deactivated"  else "Always-on notification deactivated"
            },
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
        )

        if (isForegroundServiceOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isAndroid13OrMore) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(color = Color.Gray.copy(alpha = 0.7f), shape = RoundedCornerShape(10))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Annoyed by the persistent notification? You can choose to hide it by disabling it from the notifications settings",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onNotificationSettingsButtonPressed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = primaryBlue,
                    ),
                ) {
                    Text(
                        text = "Edit always-on notification settings",
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
@Preview(showSystemUi = true, name = "Battery optimization off, foreground on")
private fun PreviewBatteryOptimOff() {
    AppMaterialTheme {
        DebugPhoneBatterySyncLayout(
            isBatteryOptimizationOff = true,
            isAndroid13OrMore = false,
            onDisableBatteryOptimizationButtonPressed = {},
            isForegroundServiceOn = true,
            onForegroundServiceSwitchedChanged = {},
            onNotificationSettingsButtonPressed = {},
        )
    }
}

@Composable
@Preview(showSystemUi = true, name = "Battery optimization on, foreground off")
private fun PreviewBatteryOptimOn() {
    AppMaterialTheme {
        DebugPhoneBatterySyncLayout(
            isBatteryOptimizationOff = false,
            isAndroid13OrMore = false,
            onDisableBatteryOptimizationButtonPressed = {},
            isForegroundServiceOn = false,
            onForegroundServiceSwitchedChanged = {},
            onNotificationSettingsButtonPressed = {},
        )
    }
}

@Composable
@Preview(showSystemUi = true, name = "Battery optimization off, foreground on, android 13")
private fun PreviewBatteryOptimOffAndroid13() {
    AppMaterialTheme {
        DebugPhoneBatterySyncLayout(
            isBatteryOptimizationOff = true,
            isAndroid13OrMore = true,
            onDisableBatteryOptimizationButtonPressed = {},
            isForegroundServiceOn = true,
            onForegroundServiceSwitchedChanged = {},
            onNotificationSettingsButtonPressed = {},
        )
    }
}

@Composable
@Preview(showSystemUi = true, name = "Battery optimization on, foreground off, android 13")
private fun PreviewBatteryOptimOnAndroid13() {
    AppMaterialTheme {
        DebugPhoneBatterySyncLayout(
            isBatteryOptimizationOff = false,
            isAndroid13OrMore = true,
            onDisableBatteryOptimizationButtonPressed = {},
            isForegroundServiceOn = false,
            onForegroundServiceSwitchedChanged = {},
            onNotificationSettingsButtonPressed = {},
        )
    }
}