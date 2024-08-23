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
package com.benoitletondor.pixelminimalwatchface.settings.phonebattery

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
import com.benoitletondor.pixelminimalwatchface.settings.phonebattery.troubleshoot.PhoneBatterySyncTroubleshootActivity
import com.google.android.gms.wearable.*
import com.google.android.gms.wearable.CapabilityClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhoneBatteryConfigurationActivity : ComponentActivity(), CapabilityClient.OnCapabilityChangedListener {
    private val viewModel: PhoneBatteryConfigurationViewModel by viewModels()

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
                    PhoneBatteryConfigurationViewModel.ErrorEventType.PHONE_CHANGED -> Toast.makeText(
                        this@PhoneBatteryConfigurationActivity,
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
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        viewModel.onCapabilityChanged(capabilityInfo)
    }

    private fun checkIfPhoneHasApp() {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val result = Wearable.getCapabilityClient(this@PhoneBatteryConfigurationActivity)
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
    private fun Content(state: PhoneBatteryConfigurationViewModel.State) {
        WearTheme {
            RotatoryAwareLazyColumn(
                horizontalPadding = 20.dp,
                modifier = Modifier,
            ) {
                item(key = "Title") {
                    Text(
                        text = "(Beta) Phone battery sync setup",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                }

                when(state) {
                    is PhoneBatteryConfigurationViewModel.State.Error -> ErrorView(syncActivated = state.syncActivated)
                    PhoneBatteryConfigurationViewModel.State.Loading -> LoadingView(connecting = true)
                    is PhoneBatteryConfigurationViewModel.State.PhoneFound -> LoadingView(connecting = false)
                    is PhoneBatteryConfigurationViewModel.State.PhoneNotFound -> ErrorView(syncActivated = state.syncActivated)
                    is PhoneBatteryConfigurationViewModel.State.PhoneStatusResponse -> ConnectedView(syncActivated = state.syncActivated)
                    is PhoneBatteryConfigurationViewModel.State.SendingStatusSyncToPhone -> LoadingView(connecting = false)
                    is PhoneBatteryConfigurationViewModel.State.WaitingForPhoneStatusResponse -> LoadingView(connecting = false)
                }
            }
        }
    }

    private fun LazyListScope.ConnectedView(syncActivated: Boolean) {
        item(key = "ConnectedViewText1") {
            ExplanationText(
                text = "Connected to your phone's companion app successfully.",
                modifier = Modifier,
            )
        }

        item(key = "ConnectedViewSyncButton") {
            SettingToggleChip(
                checked = syncActivated,
                onCheckedChange = { activateSync ->
                    if (activateSync) {
                        viewModel.onSyncWithPhoneActivated()
                    } else {
                        viewModel.onSyncWithPhoneDeactivated()
                    }
                },
                label = "Phone battery as bottom widget",
                iconDrawable = null,
                modifier = Modifier,
            )
        }

        item(key = "ConnectedViewText2") {
            Column {
                Text(
                    text = "Experiencing issues?",
                    modifier = Modifier.padding(top = 8.dp),
                )

                ExplanationText(
                    text = "Open the companion app on your phone and tap \"Troubleshoot\" button then \"Debug phone battery indicator\"",
                    modifier = Modifier.padding(bottom = 20.dp),
                )
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
                    modifier = Modifier,
                )

                ExplanationText(
                    text = "Make sure your phone is connected and \"Pixel Minimal Watch Face\" companion app is installed on it.",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier,
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
                modifier = Modifier,
            )
        }

        item(key = "ErrorViewTroubleShootButton") {
            ChipButton(
                text = "Troubleshoot",
                onClick = {
                    startActivity(Intent(this@PhoneBatteryConfigurationActivity, PhoneBatterySyncTroubleshootActivity::class.java))
                },
                modifier = Modifier,
            )
        }

        if (syncActivated) {
            item(key = "ErrorViewForceDeactivateButton") {
                ChipButton(
                    text = "Deactivate phone battery sync",
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
            state = PhoneBatteryConfigurationViewModel.State.Loading
        )
    }

    @Composable
    @Preview
    private fun SyncingPreview() {
        Content(
            state = PhoneBatteryConfigurationViewModel.State.WaitingForPhoneStatusResponse(fakePreviewNode)
        )
    }

    @Composable
    @Preview
    private fun ErrorPreviewSyncDeactivated() {
        Content(
            state = PhoneBatteryConfigurationViewModel.State.PhoneNotFound(syncActivated = false)
        )
    }

    @Composable
    @Preview
    private fun ErrorPreviewSyncActivated() {
        Content(
            state = PhoneBatteryConfigurationViewModel.State.PhoneNotFound(syncActivated = true)
        )
    }

    @Composable
    @Preview
    private fun ConnectedPreviewSyncDeactivated() {
        Content(
            state = PhoneBatteryConfigurationViewModel.State.PhoneStatusResponse(fakePreviewNode, syncActivated = false)
        )
    }

    @Composable
    @Preview
    private fun ConnectedPreviewSyncActivated() {
        Content(
            state = PhoneBatteryConfigurationViewModel.State.PhoneStatusResponse(fakePreviewNode, syncActivated = true)
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
