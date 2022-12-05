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
package com.benoitletondor.pixelminimalwatchface.settings.notificationssync

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.*
import com.benoitletondor.pixelminimalwatchface.BuildConfig
import com.benoitletondor.pixelminimalwatchface.compose.WearTheme
import com.benoitletondor.pixelminimalwatchface.compose.component.ChipButton
import com.benoitletondor.pixelminimalwatchface.compose.component.ExplanationText
import com.benoitletondor.pixelminimalwatchface.compose.component.RotatoryAwareLazyColumn
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingToggleChip
import com.benoitletondor.pixelminimalwatchface.helper.await
import com.benoitletondor.pixelminimalwatchface.helper.openCompanionAppOnPhone
import com.benoitletondor.pixelminimalwatchface.settings.notificationssync.betadisclaimer.BetaDisclaimerActivity
import com.benoitletondor.pixelminimalwatchface.settings.notificationssync.troubleshoot.NotificationsSyncTroubleshootActivity
import com.google.android.gms.wearable.*
import com.google.android.gms.wearable.CapabilityClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationsSyncConfigurationActivity : ComponentActivity(), CapabilityClient.OnCapabilityChangedListener {
    private val viewModel: NotificationsSyncConfigurationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindToViewModelEvents()

        setContent {
            val state by viewModel.stateFlow.collectAsState()
            Content(state = state)
        }

        Wearable.getCapabilityClient(this).addListener(this, BuildConfig.COMPANION_APP_CAPABILITY)
        checkIfPhoneHasApp()
    }

    override fun onDestroy() {
        Wearable.getCapabilityClient(this).removeListener(this, BuildConfig.COMPANION_APP_CAPABILITY)
        super.onDestroy()
    }

    private fun bindToViewModelEvents() {
        lifecycleScope.launch {
            viewModel.errorEventFlow.collect { error ->
                when(error) {
                    NotificationsSyncConfigurationViewModel.ErrorEventType.PHONE_CHANGED -> Toast.makeText(
                        this@NotificationsSyncConfigurationActivity,
                        "An error occurred, please make sure your phone is correctly connected and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.retryEventFlow.collect {
                checkIfPhoneHasApp()
            }
        }

        lifecycleScope.launch {
            viewModel.navigationEventFlow.collect { event ->
                when(event) {
                    NotificationsSyncConfigurationViewModel.NavigationEvent.SHOW_BETA_DISCLAIMER -> {
                        startActivity(Intent(this@NotificationsSyncConfigurationActivity, BetaDisclaimerActivity::class.java))
                    }
                }
            }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        viewModel.onCapabilityChanged(capabilityInfo)
    }

    private fun checkIfPhoneHasApp() {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val result = Wearable.getCapabilityClient(this@NotificationsSyncConfigurationActivity)
                    .getCapability(BuildConfig.COMPANION_APP_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                    .await()
                viewModel.onPhoneAppDetectionResult(result.nodes)
            } catch (t: Throwable) {
                if (t is CancellationException) {
                    throw t
                }

                viewModel.onPhoneAppDetectionFailed(t)
            }
        }
    }

    @Composable
    private fun Content(state: NotificationsSyncConfigurationViewModel.State) {
        WearTheme {
            RotatoryAwareLazyColumn(
                horizontalPadding = 20.dp,
            ) {
                item(key = "Title") {
                    Text(
                        text = "(Beta) Phone notification icons setup",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                }

                when(state) {
                    is NotificationsSyncConfigurationViewModel.State.Error -> ErrorView(syncActivated = state.syncActivated)
                    NotificationsSyncConfigurationViewModel.State.Loading -> LoadingView(connecting = true)
                    is NotificationsSyncConfigurationViewModel.State.PhoneFound -> LoadingView(connecting = false)
                    is NotificationsSyncConfigurationViewModel.State.PhoneNotFound -> ErrorView(syncActivated = state.syncActivated)
                    is NotificationsSyncConfigurationViewModel.State.PhoneStatusResponse -> ConnectedView(syncStatus = state.syncStatus)
                    is NotificationsSyncConfigurationViewModel.State.SendingStatusSyncToPhone -> LoadingView(connecting = false)
                    is NotificationsSyncConfigurationViewModel.State.WaitingForPhoneStatusResponse -> LoadingView(connecting = false)
                }
            }
        }
    }

    private fun LazyListScope.ConnectedView(syncStatus: NotificationsSyncConfigurationViewModel.NotificationsSyncStatus) {
        when(syncStatus) {
            NotificationsSyncConfigurationViewModel.NotificationsSyncStatus.DEACTIVATED,
            NotificationsSyncConfigurationViewModel.NotificationsSyncStatus.ACTIVATED -> {
                item(key = "ConnectedViewText1") {
                    ExplanationText(
                        text = "Connected to your phone's companion app successfully.",
                    )
                }

                item(key = "ConnectedViewSyncButton") {
                    SettingToggleChip(
                        checked = syncStatus == NotificationsSyncConfigurationViewModel.NotificationsSyncStatus.ACTIVATED,
                        onCheckedChange = { activateSync ->
                            if (activateSync) {
                                viewModel.onSyncWithPhoneActivated()
                            } else {
                                viewModel.onSyncWithPhoneDeactivated()
                            }
                        },
                        label = "Show notification icons",
                        iconDrawable = null,
                    )
                }

                item(key = "ConnectedViewText2") {
                    Column {
                        Text(
                            text = "Experiencing issues?",
                            modifier = Modifier.padding(top = 8.dp),
                        )

                        ExplanationText(
                            text = "Open the companion app on your phone and tap \"Troubleshoot\" button then \"Setup notifications display\"",
                            modifier = Modifier.padding(bottom = 20.dp),
                        )
                    }
                }
            }
            NotificationsSyncConfigurationViewModel.NotificationsSyncStatus.ACTIVATED_MISSING_PERMISSION -> {
                item(key = "ConnectedMissingPermissionViewText") {
                    Column {
                        Text(
                            text = "Missing notifications permission for phone companion app",
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }

                item(key = "ConnectedMissingPermissionViewText2") {
                    ExplanationText(
                        text = "To display phone notification icons, the phone companion app needs to have the notification access permission",
                    )
                }

                item(key = "ConnectedMissingPermissionViewCTA") {
                    ChipButton(
                        text = "Open setup screen on your phone",
                        onClick = {
                            lifecycleScope.launch {
                                if (!openCompanionAppOnPhone("notifications")) {
                                    Toast.makeText(
                                        this@NotificationsSyncConfigurationActivity,
                                        "Open the companion app manually on your phone",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }
                            }
                        },
                        backgroundColor = MaterialTheme.colors.secondary,
                    )
                }

                item(key = "ConnectedMissingPermissionViewText3") {
                    Column {
                        Text(
                            text = "In the companion app:",
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        )

                        ExplanationText(
                            text = "- Tap \"Troubleshoot\" button",
                        )

                        ExplanationText(
                            text = "- Then tap \"Setup notification icons sync\" button",
                        )
                    }
                }

                item(key = "ConnectedMissingPermissionViewDisableTitle") {
                    Text(
                        text = "Want to disable notification icons?",
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                item(key = "ConnectedMissingPermissionViewDisableCTA") {
                    ChipButton(
                        onClick = viewModel::onSyncWithPhoneDeactivated,
                        text = "Deactivate notification icons",
                    )
                }
            }
        }
    }

    private fun LazyListScope.LoadingView(connecting: Boolean) {
        item(key = "LoadingViewTitle+$connecting") {
            Text(
                text = if (connecting) "Connecting to your phone…" else "Syncing state with phone…",
                textAlign = TextAlign.Center,
            )
        }

        item(key = "LoadingViewProgress") {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                CircularProgressIndicator()
            }
        }
    }

    private fun LazyListScope.ErrorView(syncActivated: Boolean) {
        item(key = "ErrorViewTitle") {
            Text(
                text = "Connection error",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        item(key = "ErrorViewText1") {
            Column {
                ExplanationText(
                    text = "Unable to connect to the companion app on your phone",
                )

                ExplanationText(
                    text = "Make sure your phone is connected and \"Pixel Minimal Watch Face\" companion app is installed on it.",
                    fontWeight = FontWeight.Bold,
                )
            }

        }

        item(key = "ErrorViewRetryButton") {
            ChipButton(
                text = "Retry",
                onClick = viewModel::onRetryConnectionClicked,
                backgroundColor = MaterialTheme.colors.secondary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        item(key = "ErrorViewText2") {
            ExplanationText(
                text = "If the issue persists:",
            )
        }

        item(key = "ErrorViewTroubleShootButton") {
            ChipButton(
                text = "Troubleshoot",
                onClick = {
                    startActivity(Intent(this@NotificationsSyncConfigurationActivity, NotificationsSyncTroubleshootActivity::class.java))
                },
            )
        }

        if (syncActivated) {
            item(key = "ErrorViewForceDeactivateButton") {
                ChipButton(
                    text = "Deactivate notifications icons",
                    onClick = viewModel::onForceDeactivateSyncClicked,
                    backgroundColor = MaterialTheme.colors.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }

    @Composable
    @Preview
    private fun ConnectingPreview() {
        Content(
            state = NotificationsSyncConfigurationViewModel.State.Loading
        )
    }

    @Composable
    @Preview
    private fun SyncingPreview() {
        Content(
            state = NotificationsSyncConfigurationViewModel.State.WaitingForPhoneStatusResponse(fakePreviewNode)
        )
    }

    @Composable
    @Preview
    private fun ErrorPreviewSyncDeactivated() {
        Content(
            state = NotificationsSyncConfigurationViewModel.State.PhoneNotFound(syncActivated = false)
        )
    }

    @Composable
    @Preview
    private fun ErrorPreviewSyncActivated() {
        Content(
            state = NotificationsSyncConfigurationViewModel.State.PhoneNotFound(syncActivated = true)
        )
    }

    @Composable
    @Preview
    private fun ConnectedPreviewSyncDeactivated() {
        Content(
            state = NotificationsSyncConfigurationViewModel.State.PhoneStatusResponse(
                fakePreviewNode,
                syncStatus = NotificationsSyncConfigurationViewModel.NotificationsSyncStatus.DEACTIVATED
            )
        )
    }

    @Composable
    @Preview
    private fun ConnectedPreviewSyncActivated() {
        Content(
            state = NotificationsSyncConfigurationViewModel.State.PhoneStatusResponse(
                fakePreviewNode,
                syncStatus = NotificationsSyncConfigurationViewModel.NotificationsSyncStatus.ACTIVATED
            )
        )
    }

    @Composable
    @Preview
    private fun ConnectedPreviewSyncActivatedNoPermission() {
        Content(
            state = NotificationsSyncConfigurationViewModel.State.PhoneStatusResponse(
                fakePreviewNode,
                syncStatus = NotificationsSyncConfigurationViewModel.NotificationsSyncStatus.ACTIVATED_MISSING_PERMISSION
            )
        )
    }

    companion object {
        private val fakePreviewNode = object : Node {
            override fun getId(): String = "id"

            override fun getDisplayName(): String = "Phone name"

            override fun isNearby(): Boolean = true
        }
    }
}
