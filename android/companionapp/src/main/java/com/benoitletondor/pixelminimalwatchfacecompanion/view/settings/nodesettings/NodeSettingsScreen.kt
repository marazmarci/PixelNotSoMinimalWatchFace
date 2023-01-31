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

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.openPlayStore
import com.benoitletondor.pixelminimalwatchfacecompanion.platform.PhonePlatform
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.LoadingLayout
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings.PhoneSettingsComposeComponents
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

@Composable
fun NodeSettingsScreen(
    navController: NavController,
    viewModel: NodeSettingsViewModel,
    nodeId: String,
) {
    val activity = LocalContext.current as ComponentActivity

    LaunchedEffect("eventListener") {
        launch {
            viewModel.eventFlow.collect { event ->
                when(event) {
                    NodeSettingsViewModel.Event.OpenPlayStore -> activity.openPlayStore()
                    NodeSettingsViewModel.Event.ShowActivationInstructions -> {
                        MaterialAlertDialogBuilder(activity)
                            .setTitle("Activate Pixel Minimal Watch Face as your watch face")
                            .setMessage("Everything happens on your watch:\n\n" +
                                    "- Long press at the center of your current watch face\n" +
                                    "- After 2 seconds, you'll enter the watch face selection menu\n" +
                                    "- Scroll all the way to the right to search for more watch faces, tap for the + button\n" +
                                    "- Scroll in that list to find Pixel Minimal Watch Face and tap on it")
                            .setPositiveButton("Ok") { _, _ -> }
                            .show()
                    }
                }
            }
        }
    }

    AppTopBarScaffold(
        navController = navController,
        showBackButton = true,
        title = "Watch Face settings",
        actions = {
            IconButton(
                onClick = viewModel::onRetryButtonPressed,
            ) {
                Icon(Icons.Filled.Refresh, "Refresh")
            }
        },
        content = {
            val state by viewModel.stateFlow.collectAsState()

            Content(
                state = state,
                navController = navController,
                onRetryButtonPressed = viewModel::onRetryButtonPressed,
                onOpenPlayStoreOnPhonePressed = viewModel::onOpenPlayStoreOnPhonePressed,
                onHowToActivateButtonPressed = viewModel::onHowToActivateButtonPressed,
            )
        }
    )
}

@Composable
private fun Content(
    state: NodeSettingsViewModel.State,
    navController: NavController,
    onRetryButtonPressed: () -> Unit,
    onOpenPlayStoreOnPhonePressed: () -> Unit,
    onHowToActivateButtonPressed: () -> Unit,
) {
    when(state) {
        is NodeSettingsViewModel.State.Error -> ErrorView(
            error = state.e,
            onRetryButtonPressed = onRetryButtonPressed,
            onHowToActivateButtonPressed = onHowToActivateButtonPressed,
        )
        is NodeSettingsViewModel.State.IncompatibleVersion -> IncompatibleVersionView(
            watchNodeVersion = state.watchNodeVersion,
            appVersion = state.appVersion,
            onOpenPlayStoreOnPhonePressed = onOpenPlayStoreOnPhonePressed,
        )
        NodeSettingsViewModel.State.InitializingSession -> LoadingView()
        is NodeSettingsViewModel.State.Loaded -> SettingsView(
            platform = state.platform,
            navController = navController,
        )
    }
}

@Composable
private fun ErrorView(
    error: Exception,
    onRetryButtonPressed: () -> Unit,
    onHowToActivateButtonPressed: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 26.dp)
            .fillMaxHeight(fraction = 0.9f)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Unable to connect",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "An error occurred while connecting to the watch face on your watch.",
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Make sure that:",
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "- Your watch is correctly connected via Bluetooth to your phone",
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "- Pixel Minimal Watch Face is your current active watch face",
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onHowToActivateButtonPressed,
            colors = blueButtonColors(),
        ) {
            Text("How to activate the watch face")
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Once done, you can try again:",
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetryButtonPressed) {
            Text("Retry")
        }
    }
}

@Composable
private fun IncompatibleVersionView(
    watchNodeVersion: Int,
    appVersion: Int,
    onOpenPlayStoreOnPhonePressed: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 26.dp)
            .fillMaxHeight(fraction = 0.9f)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Incompatible versions detected",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "The version of the Pixel Minimal Watch Face app is different on your phone and watch.",
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (watchNodeVersion > appVersion) {
            Text(
                text = "You need to update the app on your phone:",
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onOpenPlayStoreOnPhonePressed,
                colors = blueButtonColors(),
            ) {
                Text("Open PlayStore to update")
            }
        } else {
            Text(
                text = "You need to update the app on your watch:",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "- Open the PlayStore on your watch (not phone!)",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "- Scroll all the way down to \"Manage apps\" button and tap on it",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "- Update all the apps from there, including Pixel Minimal Watch Face",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun LoadingView() {
    LoadingLayout()
}

@Composable
private fun SettingsView(
    platform: PhonePlatform,
    navController: NavController,
) {
    val phoneSettingsComponent = remember("settingsScreenComponents") { PhoneSettingsComposeComponents() }
    val settingsScreen = remember("settingsScreen") { PhoneSettingsScreen() }

    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        settingsScreen.Screen(
            composeComponents = phoneSettingsComponent,
            platform = platform,
            navController = navController,
        )
    }

}

@Preview
@Composable
private fun IncompatibleVersionViewUpdateAppPreview() {
    AppMaterialTheme {
        IncompatibleVersionView(
            watchNodeVersion = 101,
            appVersion = 201,
            onOpenPlayStoreOnPhonePressed = {},
        )
    }
}

@Preview
@Composable
private fun IncompatibleVersionViewUpdatePhonePreview() {
    AppMaterialTheme {
        IncompatibleVersionView(
            watchNodeVersion = 201,
            appVersion = 101,
            onOpenPlayStoreOnPhonePressed = {},
        )
    }
}

@Preview
@Composable
private fun ErrorViewPreview() {
    AppMaterialTheme {
        ErrorView(
            error = Exception("Test message"),
            onRetryButtonPressed = {},
            onHowToActivateButtonPressed = {},
        )
    }
}