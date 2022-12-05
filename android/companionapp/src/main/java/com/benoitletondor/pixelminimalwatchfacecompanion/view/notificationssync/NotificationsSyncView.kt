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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.notificationssync

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.NotificationsListener
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.startSupportEmailActivity
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.primaryBlue

@Composable
fun NotificationsSyncView(navController: NavController, viewModel: NotificationsSyncViewModel) {
    val notificationsPermissionLauncher = rememberLauncherForActivityResult(NotificationsListener.NotificationsListenerPermissionResultContract) {
        viewModel.onPermissionActivityResult()
    }

    val fallbackNotificationsPermissionLauncher = rememberLauncherForActivityResult(NotificationsListener.FallbackNotificationsListenerPermissionResultContract) {
        viewModel.onPermissionActivityResult()
    }

    val context = LocalContext.current

    LaunchedEffect("events") {
        viewModel.eventFlow.collect { event ->
            when(event) {
                NotificationsSyncViewModel.Event.OpenNotificationsPermissionActivity -> {
                    try {
                        notificationsPermissionLauncher.launch(Unit)
                    } catch (e: Exception) {
                        fallbackNotificationsPermissionLauncher.launch(Unit)
                    }
                }
                NotificationsSyncViewModel.Event.OpenSupportEmail -> {
                    context.startSupportEmailActivity()
                }
            }
        }
    }

    val state by viewModel.stateFlow.collectAsState()

    AppTopBarScaffold(
        navController = navController,
        showBackButton = true,
        title = "Notification icons sync",
        content = {
            NotificationsSyncLayout(
                onAskPermissionButtonPressed = viewModel::onAskPermissionButtonPressed,
                onSupportButtonPressed = viewModel::onSupportButtonPressed,
                state = state,
            )
        }
    )
}

@Composable
private fun NotificationsSyncLayout(
    onAskPermissionButtonPressed: () -> Unit,
    onSupportButtonPressed: () -> Unit,
    state: NotificationsSyncViewModel.State,
) {
    when(state) {
        NotificationsSyncViewModel.State.Activated -> ActivatedNotificationsSyncLayout(
            onSupportButtonPressed = onSupportButtonPressed,
        )
        NotificationsSyncViewModel.State.ActivatedNoPermission -> ActivatedNoPermissionNotificationsSyncLayout(
            onAskPermissionButtonPressed = onAskPermissionButtonPressed,
            onSupportButtonPressed = onSupportButtonPressed,
        )
        NotificationsSyncViewModel.State.Deactivated -> DeactivatedPermissionsNotificationsSyncLayout(
            onSupportButtonPressed = onSupportButtonPressed,
        )
    }
}

@Composable
private fun ActivatedNotificationsSyncLayout(
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
            text = "✔ Notification icons sync ready",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Everything seems to be ready to sync your notification icons with the watch face.",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Want to disable it?",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "- Go into the watch face settings on your watch (by long pressing on the time on the watch face, then tapping the Customize button at the bottom)",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = "- Tap the \"Phone notification icons\" button",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = "- Disable the \"Show notification icons\" switch",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        IssuesReportLayout(
            onSupportButtonPressed = onSupportButtonPressed,
        )
    }
}

@Composable
private fun ActivatedNoPermissionNotificationsSyncLayout(
    onAskPermissionButtonPressed: () -> Unit,
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
            text = "⚠️Notification permission missing",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "The companion app needs access to your notifications to sync them with the watch face:",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onAskPermissionButtonPressed,
            colors = blueButtonColors(),
        ) {
            Text(text = "Allow notifications access")
        }

        Spacer(modifier = Modifier.height(16.dp))

        IssuesReportLayout(
            onSupportButtonPressed = onSupportButtonPressed,
        )
    }
}

@Composable
private fun DeactivatedPermissionsNotificationsSyncLayout(
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
            text = "❌ Notification icons sync is disabled",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Want to enable it? everything happens on the watch face:",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "- Go into the watch face settings on your watch (by long pressing on the time on the watch face, then tapping the Customize button at the bottom)",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = "- Tap the \"Phone notification icons\" button",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = "- Enable the \"Show notification icons\" switch",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        IssuesReportLayout(
            onSupportButtonPressed = onSupportButtonPressed,
        )
    }
}

@Composable
private fun IssuesReportLayout(
    onSupportButtonPressed: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(color = Color.Gray.copy(alpha = 0.7f), shape = RoundedCornerShape(10))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Experiencing issues?",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This is a beta feature so things might not always work as expected. If you experience issues, please let me know.",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSupportButtonPressed,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = primaryBlue,
            ),
        ) {
            Text(
                text = "Contact me for support",
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
@Preview(showSystemUi = true, name = "Sync deactivated")
private fun DeactivatedPreview() {
    AppMaterialTheme {
        NotificationsSyncLayout(
            onAskPermissionButtonPressed = {},
            onSupportButtonPressed = {},
            state = NotificationsSyncViewModel.State.Deactivated,
        )
    }
}

@Composable
@Preview(showSystemUi = true, name = "Sync activated")
private fun ActivatedPreview() {
    AppMaterialTheme {
        NotificationsSyncLayout(
            onAskPermissionButtonPressed = {},
            onSupportButtonPressed = {},
            state = NotificationsSyncViewModel.State.Activated,
        )
    }
}

@Composable
@Preview(showSystemUi = true, name = "Sync activated no permission")
private fun ActivatedNoPermissionPreview() {
    AppMaterialTheme {
        NotificationsSyncLayout(
            onAskPermissionButtonPressed = {},
            onSupportButtonPressed = {},
            state = NotificationsSyncViewModel.State.ActivatedNoPermission,
        )
    }
}