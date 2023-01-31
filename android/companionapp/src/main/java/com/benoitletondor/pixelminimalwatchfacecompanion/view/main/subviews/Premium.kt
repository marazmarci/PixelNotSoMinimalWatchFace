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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.main.subviews

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.*
import com.benoitletondor.pixelminimalwatchfacecompanion.view.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun Premium(
    step: MainViewModel.Step.Premium,
    viewModel: MainViewModel,
) {
    PremiumLayout(
        installWatchFaceButtonPressed = viewModel::onGoToInstallWatchFaceButtonPressed,
        syncPremiumStatusButtonPressed = viewModel::triggerSync,
        donateButtonPressed = viewModel::onDonateButtonPressed,
        onSupportButtonPressed = viewModel::onSupportButtonPressed,
        isBatterySyncActivated = step.isBatterySyncActivated,
        isNotificationsSyncActivated = step.isNotificationsSyncActivated,
        maybeWarning = step.maybeWarning,
        debugPhoneBatteryIndicatorButtonPressed = viewModel::onDebugPhoneBatteryIndicatorButtonPressed,
        setupNotificationsSyncButtonPressed = viewModel::onSetupNotificationsSyncButtonPressed,
        settingsButtonPressed = viewModel::onSettingsButtonPressed,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PremiumLayout(
    installWatchFaceButtonPressed: () -> Unit,
    syncPremiumStatusButtonPressed: () -> Unit,
    donateButtonPressed: () -> Unit,
    onSupportButtonPressed: () -> Unit,
    isBatterySyncActivated: Boolean,
    isNotificationsSyncActivated: Boolean,
    maybeWarning: String?,
    debugPhoneBatteryIndicatorButtonPressed: () -> Unit,
    setupNotificationsSyncButtonPressed: () -> Unit,
    settingsButtonPressed: () -> Unit,
) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            TroubleShootBottomSheet(
                bottomSheetScaffoldState,
                installWatchFaceButtonPressed = installWatchFaceButtonPressed,
                onSupportButtonPressed = onSupportButtonPressed,
                syncPremiumStatusButtonPressed = syncPremiumStatusButtonPressed,
                isBatterySyncActivated = isBatterySyncActivated,
                isNotificationsSyncActivated = isNotificationsSyncActivated,
                debugPhoneBatteryIndicatorButtonPressed = debugPhoneBatteryIndicatorButtonPressed,
                setupNotificationsSyncButtonPressed = setupNotificationsSyncButtonPressed,
            )
        },
        sheetPeekHeight = 0.dp,
        backgroundColor = MaterialTheme.colorScheme.background,
        sheetBackgroundColor = Color.DarkGray,
    ) {
        PremiumLayoutContent(
            bottomSheetScaffoldState,
            donateButtonPressed = donateButtonPressed,
            settingsButtonPressed = settingsButtonPressed,
            maybeWarning = maybeWarning,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PremiumLayoutContent(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    donateButtonPressed: () -> Unit,
    settingsButtonPressed: () -> Unit,
    maybeWarning: String?,
) {
    val coroutineScope = rememberCoroutineScope()

    BackHandler(bottomSheetScaffoldState.bottomSheetState.isExpanded) {
        coroutineScope.launch {
            bottomSheetScaffoldState.bottomSheetState.collapse()
        }
    }

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
            text = "You're premium!",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Thank you so much for your support :)",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (maybeWarning != null) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(color = orange, shape = RoundedCornerShape(10))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = maybeWarning,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Setup the watch face",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Customize the watch face look and feel",
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = settingsButtonPressed,
            colors = blueButtonColors(),
        ) {
            Text(text = "Settings")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "All the settings are also available on your watch: on the watch face, long press on the time. After 1s, you'll see a button appear at the bottom, tap on it to customize.",
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Troubleshooting",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You have any issue with the watch face? Something's not working?",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    } else {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }
            },
            colors = blueButtonColors(),
        ) {
            Text(text = "Troubleshoot")
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
                .background(color = primaryGreen.copy(alpha = 0.7f), shape = RoundedCornerShape(10))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Feel like helping even more with a tip?",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = donateButtonPressed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = primaryGreen,
                ),
            ) {
                Text(text = "Donate")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TroubleShootBottomSheet(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    installWatchFaceButtonPressed: () -> Unit,
    syncPremiumStatusButtonPressed: () -> Unit,
    onSupportButtonPressed: () -> Unit,
    isBatterySyncActivated: Boolean,
    isNotificationsSyncActivated: Boolean,
    debugPhoneBatteryIndicatorButtonPressed: () -> Unit,
    setupNotificationsSyncButtonPressed: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    TroubleshootingContent(
        installWatchFaceButtonPressed = {
            coroutineScope.launch {
                bottomSheetScaffoldState.bottomSheetState.collapse()
            }
            installWatchFaceButtonPressed()
        },
        syncPremiumStatusButtonPressed = {
            coroutineScope.launch {
                bottomSheetScaffoldState.bottomSheetState.collapse()
            }
            syncPremiumStatusButtonPressed()
        } ,
        onSupportButtonPressed = {
            coroutineScope.launch {
                bottomSheetScaffoldState.bottomSheetState.collapse()
            }
            onSupportButtonPressed()
        },
        onCloseButtonPressed = {
            coroutineScope.launch {
                bottomSheetScaffoldState.bottomSheetState.collapse()
            }
        },
        isBatterySyncActivated = isBatterySyncActivated,
        isNotificationsSyncActivated = isNotificationsSyncActivated,
        debugPhoneBatteryIndicatorButtonPressed = debugPhoneBatteryIndicatorButtonPressed,
        setupNotificationsSyncButtonPressed = setupNotificationsSyncButtonPressed,
    )
}

@Composable
private fun TroubleshootingContent(
    installWatchFaceButtonPressed: () -> Unit,
    syncPremiumStatusButtonPressed: () -> Unit,
    onSupportButtonPressed: () -> Unit,
    onCloseButtonPressed: () -> Unit,
    isBatterySyncActivated: Boolean,
    isNotificationsSyncActivated: Boolean,
    debugPhoneBatteryIndicatorButtonPressed: () -> Unit,
    setupNotificationsSyncButtonPressed: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopEnd,
    ) {
        Button(
            onClick = onCloseButtonPressed,
            shape = CircleShape,
            modifier = Modifier
                .padding(10.dp)
                .width(40.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
            ),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(text = "X")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Troubleshooting",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Watch face is not installed on your watch?",
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(5.dp))

            TextButton(
                onClick = installWatchFaceButtonPressed,
                colors = blueButtonColors(),
            ) {
                Text(
                    text = "Install watch face",
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Watch face doesn't recognize you as premium?",
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(5.dp))

            TextButton(
                onClick = syncPremiumStatusButtonPressed,
                colors = blueButtonColors(),
            ) {
                Text(
                    text = "Sync premium with Watch",
                    textAlign = TextAlign.Center,
                )
            }

            if (isBatterySyncActivated) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Phone battery indicator sync issue?",
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(5.dp))

                TextButton(
                    onClick = debugPhoneBatteryIndicatorButtonPressed,
                    colors = blueButtonColors(),
                ) {
                    Text(
                        text = "Debug phone battery indicator",
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (isNotificationsSyncActivated) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Manage notification icons display?",
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(5.dp))

                TextButton(
                    onClick = setupNotificationsSyncButtonPressed,
                    colors = blueButtonColors(),
                ) {
                    Text(
                        text = "Setup notification icons sync",
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Sync doesn't work? Have another issue? I'm here to help",
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(5.dp))

            Button(
                onClick = onSupportButtonPressed,
            ) {
                Text(
                    text = "Contact me for support",
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "I'll help you within less than 24h",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 13.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
@Preview(showSystemUi = true)
private fun Preview() {
    AppMaterialTheme {
        PremiumLayout(
            installWatchFaceButtonPressed = {},
            syncPremiumStatusButtonPressed = {},
            donateButtonPressed = {},
            onSupportButtonPressed = {},
            isBatterySyncActivated = true,
            isNotificationsSyncActivated = true,
            maybeWarning = null,
            debugPhoneBatteryIndicatorButtonPressed = {},
            setupNotificationsSyncButtonPressed = {},
            settingsButtonPressed = {},
        )
    }
}

@Composable
@Preview(showSystemUi = true, name = "Warning")
private fun PreviewWarning() {
    AppMaterialTheme {
        PremiumLayout(
            installWatchFaceButtonPressed = {},
            syncPremiumStatusButtonPressed = {},
            donateButtonPressed = {},
            onSupportButtonPressed = {},
            isBatterySyncActivated = true,
            isNotificationsSyncActivated = true,
            maybeWarning = "This is a warning",
            debugPhoneBatteryIndicatorButtonPressed = {},
            setupNotificationsSyncButtonPressed = {},
            settingsButtonPressed = {},
        )
    }
}

@Composable
@Preview(name = "Troubleshooting")
private fun TroubleshootPreview() {
    AppMaterialTheme {
        TroubleshootingContent(
            onSupportButtonPressed = {},
            installWatchFaceButtonPressed = {},
            syncPremiumStatusButtonPressed = {},
            onCloseButtonPressed = {},
            isBatterySyncActivated = false,
            isNotificationsSyncActivated = false,
            debugPhoneBatteryIndicatorButtonPressed = {},
            setupNotificationsSyncButtonPressed = {},
        )
    }
}

@Composable
@Preview(name = "Troubleshooting battery sync activated")
private fun TroubleshootPreviewBatterySyncActivated() {
    AppMaterialTheme {
        TroubleshootingContent(
            onSupportButtonPressed = {},
            installWatchFaceButtonPressed = {},
            syncPremiumStatusButtonPressed = {},
            onCloseButtonPressed = {},
            isBatterySyncActivated = true,
            isNotificationsSyncActivated = false,
            debugPhoneBatteryIndicatorButtonPressed = {},
            setupNotificationsSyncButtonPressed = {},
        )
    }
}

@Composable
@Preview(name = "Troubleshooting battery sync activated")
private fun TroubleshootPreviewNotificationsSyncActivated() {
    AppMaterialTheme {
        TroubleshootingContent(
            onSupportButtonPressed = {},
            installWatchFaceButtonPressed = {},
            syncPremiumStatusButtonPressed = {},
            onCloseButtonPressed = {},
            isBatterySyncActivated = false,
            isNotificationsSyncActivated = true,
            debugPhoneBatteryIndicatorButtonPressed = {},
            setupNotificationsSyncButtonPressed = {},
        )
    }
}

@Composable
@Preview(name = "Troubleshooting battery sync activated")
private fun TroubleshootPreviewNotificationsAndBatterySyncActivated() {
    AppMaterialTheme {
        TroubleshootingContent(
            onSupportButtonPressed = {},
            installWatchFaceButtonPressed = {},
            syncPremiumStatusButtonPressed = {},
            onCloseButtonPressed = {},
            isBatterySyncActivated = true,
            isNotificationsSyncActivated = true,
            debugPhoneBatteryIndicatorButtonPressed = {},
            setupNotificationsSyncButtonPressed = {},
        )
    }
}