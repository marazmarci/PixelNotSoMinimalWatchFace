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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.primaryGreen
import com.benoitletondor.pixelminimalwatchfacecompanion.view.main.MainViewModel

@Composable
fun InstallWatchFace(
    step: MainViewModel.Step.InstallWatchFace,
    viewModel: MainViewModel,
) {
    InstallWatchFaceLayout(
        appInstalledStatus = step.appInstalledStatus,
        onWatchFaceInstalledButtonPressed = viewModel::onWatchFaceInstalledButtonPressed,
        onOpenPlayStoreButtonPressed = viewModel::onInstallWatchFaceButtonPressed,
        onSupportButtonPressed = viewModel::onSupportButtonPressed,
    )
}

@Composable
private fun InstallWatchFaceLayout(
    appInstalledStatus: MainViewModel.AppInstalledStatus,
    onWatchFaceInstalledButtonPressed: () -> Unit,
    onOpenPlayStoreButtonPressed: () -> Unit,
    onSupportButtonPressed: () -> Unit,
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
            text = "Install the watch face on your watch",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(20.dp))

        when(appInstalledStatus) {
            is MainViewModel.AppInstalledStatus.Result -> when(appInstalledStatus.wearableStatus) {
                Sync.WearableStatus.AvailableAppInstalled -> {
                    Installed(
                        onWatchFaceInstalledButtonPressed = onWatchFaceInstalledButtonPressed,
                        onSupportButtonPressed = onSupportButtonPressed,
                    )
                }
                Sync.WearableStatus.AvailableAppNotInstalled -> {
                    AutoInstall(
                        onOpenPlayStoreButtonPressed = onOpenPlayStoreButtonPressed,
                        onWatchFaceInstalledButtonPressed = onWatchFaceInstalledButtonPressed,
                        onSupportButtonPressed = onSupportButtonPressed,
                    )
                }
                is Sync.WearableStatus.Error, Sync.WearableStatus.NotAvailable -> {
                    ManualInstall(
                        onWatchFaceInstalledButtonPressed = onWatchFaceInstalledButtonPressed,
                        onSupportButtonPressed = onSupportButtonPressed,
                    )
                }
            }
            MainViewModel.AppInstalledStatus.Unknown -> {
                ManualInstall(
                    onWatchFaceInstalledButtonPressed = onWatchFaceInstalledButtonPressed,
                    onSupportButtonPressed = onSupportButtonPressed,
                )
            }
            MainViewModel.AppInstalledStatus.Verifying -> {
                VerifyingInstallStatus()
            }
        }
    }
}

@Composable
private fun Installed(
    onWatchFaceInstalledButtonPressed: () -> Unit,
    onSupportButtonPressed: () -> Unit,
) {
    Text(
        text = "Watch face detected on your watch",
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )

    Image(
        painter = painterResource(R.drawable.ic_baseline_check_24),
        contentDescription = null,
        modifier = Modifier.size(50.dp),
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "Make sure you activate it as your watch face",
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = onWatchFaceInstalledButtonPressed,
        colors = blueButtonColors(),
    ) {
        Text("Continue")
    }

    Spacer(modifier = Modifier.height(30.dp))

    Text(
        text = "Watch face not installed?",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 18.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    InstallManuallyLayout(
        onWatchFaceInstalledButtonPressed = onWatchFaceInstalledButtonPressed,
    )
    SupportLayout(
        onSupportButtonPressed = onSupportButtonPressed,
    )
}

@Composable
private fun AutoInstall(
    onWatchFaceInstalledButtonPressed: () -> Unit,
    onOpenPlayStoreButtonPressed: () -> Unit,
    onSupportButtonPressed: () -> Unit,
) {
    InstallAutoLayout(
        onOpenPlayStoreButtonPressed = onOpenPlayStoreButtonPressed,
        onWatchFaceInstalledButtonPressed = onWatchFaceInstalledButtonPressed,
    )
    SkipInstallLayout(
        onWatchFaceInstalledButtonPressed = onWatchFaceInstalledButtonPressed,
    )
    SupportLayout(
        onSupportButtonPressed = onSupportButtonPressed,
    )
}

@Composable
private fun ManualInstall(
    onWatchFaceInstalledButtonPressed: () -> Unit,
    onSupportButtonPressed: () -> Unit,
) {
    InstallManuallyLayout(
        onWatchFaceInstalledButtonPressed = onWatchFaceInstalledButtonPressed,
    )
    SkipInstallLayout(
        onWatchFaceInstalledButtonPressed = onWatchFaceInstalledButtonPressed,
    )
    SupportLayout(
        onSupportButtonPressed = onSupportButtonPressed,
    )
}

@Composable
private fun VerifyingInstallStatus() {
    Spacer(modifier = Modifier.height(10.dp))
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = "Verifying install status...",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun InstallAutoLayout(
    onOpenPlayStoreButtonPressed: () -> Unit,
    onWatchFaceInstalledButtonPressed: () -> Unit,
) {
    Text(
        text = "Follow those 4 steps to install it on your watch",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "1. Tap the button below to open the PlayStore on your watch, directly on the watch face page ",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
    )

    Button(
        onClick = onOpenPlayStoreButtonPressed,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = primaryGreen,
        ),
    ) {
        Text(
            text = "Open PlayStore on watch",
        )
    }

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "If that doesn't work, open the PlayStore on your watch manually and search for \"Pixel Minimal Watch Face\" to install it.",
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
            .background(color = Color(0x22FFFFFF), shape = RoundedCornerShape(10))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 13.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "2. Tap the install button",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "3. Once installed, activate it as your watch face",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = onWatchFaceInstalledButtonPressed,
        colors = blueButtonColors(),
    ) {
        Text("Continue")
    }
}

@Composable
private fun InstallManuallyLayout(
    onWatchFaceInstalledButtonPressed: () -> Unit,
) {
    Text(
        text = "Follow those 4 steps to install it on your watch",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "You need to do that on your watch directly, not from your phone",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
            .background(color = Color(0x22FFFFFF), shape = RoundedCornerShape(10))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 13.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "1. Open the PlayStore on your watch",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "2. Search for \"Pixel Minimal Watch Face\"",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "3. Tap the install button",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "4. Once installed, activate it as your watch face",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = onWatchFaceInstalledButtonPressed,
        colors = blueButtonColors(),
    ) {
        Text("Continue")
    }
}

@Composable
private fun SkipInstallLayout(
    onWatchFaceInstalledButtonPressed: () -> Unit,
) {
    Spacer(modifier = Modifier.height(30.dp))

    Text(
        text = "Watch face already installed?",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 18.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "If the watch face is already installed on your watch, make sure you activate it and you can continue",
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = onWatchFaceInstalledButtonPressed,
        colors = blueButtonColors(),
    ) {
        Text("Continue")
    }
}

@Composable
private fun SupportLayout(
    onSupportButtonPressed: () -> Unit,
) {
    Spacer(modifier = Modifier.height(30.dp))

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(color = Color.DarkGray, shape = RoundedCornerShape(10))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Install doesn't work? Have another issue? I'm here to help",
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(5.dp))

        Button(
            onClick = onSupportButtonPressed,
        ) {
            Text(text = "Contact me for support")
        }
    }
}

@Composable
@Preview(showSystemUi = true, name = "Verifying")
private fun VerifyingPreview() {
    Preview(appInstalledStatus = MainViewModel.AppInstalledStatus.Verifying)
}

@Composable
@Preview(showSystemUi = true, name = "Installed")
private fun InstalledPreview() {
    Preview(appInstalledStatus = MainViewModel.AppInstalledStatus.Result(Sync.WearableStatus.AvailableAppInstalled))
}

@Composable
@Preview(showSystemUi = true, name = "Not installed - manual")
private fun NotInstalledManualPreview() {
    Preview(appInstalledStatus = MainViewModel.AppInstalledStatus.Result(Sync.WearableStatus.NotAvailable))
}

@Composable
@Preview(showSystemUi = true, name = "Not installed - auto")
private fun NotInstalledAutoPreview() {
    Preview(appInstalledStatus = MainViewModel.AppInstalledStatus.Result(Sync.WearableStatus.AvailableAppNotInstalled))
}

@Composable
private fun Preview(
    appInstalledStatus: MainViewModel.AppInstalledStatus,
) {
    AppMaterialTheme {
        InstallWatchFaceLayout(
            appInstalledStatus = appInstalledStatus,
            onOpenPlayStoreButtonPressed = {},
            onWatchFaceInstalledButtonPressed = {},
            onSupportButtonPressed = {},
        )
    }
}