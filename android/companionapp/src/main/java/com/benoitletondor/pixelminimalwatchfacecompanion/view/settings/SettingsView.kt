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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.ErrorLayout
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.LoadingLayout
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings.Chip
import com.benoitletondor.pixelminimalwatchfacecompanion.view.NAV_SETTINGS_NODE_ROUTE
import com.benoitletondor.pixelminimalwatchfacecompanion.view.NAV_SETTINGS_NODE_ROUTE_ARG
import com.google.android.gms.wearable.Node
import kotlinx.coroutines.launch

const val NavigateToInstallWatchFaceScreenResult = "NavigateToInstallWatchFaceScreenResult"

@Composable
fun SettingsView(navController: NavController, viewModel: SettingsViewModel) {
    LaunchedEffect("init") {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.remove<Boolean>(NavigateToInstallWatchFaceScreenResult)
    }

    LaunchedEffect("events") {
        launch {
            viewModel.eventFlow.collect { event ->
                when(event) {
                    is SettingsViewModel.Event.NavigateToNodeView -> {
                        navController.navigate(NAV_SETTINGS_NODE_ROUTE.replace("{$NAV_SETTINGS_NODE_ROUTE_ARG}", event.node.id))
                    }
                    SettingsViewModel.Event.NavigateToInstallWatchFaceScreen -> {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(NavigateToInstallWatchFaceScreenResult, true)

                        navController.popBackStack()
                    }
                }
            }
        }
    }

    AppTopBarScaffold(
        navController = navController,
        showBackButton = true,
        title = "Watch selector",
        content = {
            val state by viewModel.stateFlow.collectAsState()

            ContentView(
                state = state,
                onRetryButtonPressed = viewModel::onRetryButtonPressed,
                onInstallWatchFaceButtonPressed = viewModel::onInstallWatchFaceButtonPressed,
                onNodeSelected = viewModel::onNodeSelected,
            )
        }
    )
}

@Composable
private fun ContentView(
    state: SettingsViewModel.State,
    onRetryButtonPressed: () -> Unit,
    onInstallWatchFaceButtonPressed: () -> Unit,
    onNodeSelected: (Node) -> Unit,
) {
    when(state) {
        is SettingsViewModel.State.ErrorLoadingNodes -> ErrorView(state.error, onRetryButtonPressed)
        SettingsViewModel.State.Loading -> LoadingView()
        SettingsViewModel.State.NoNodesAvailable -> EmptyStateView(onRetryButtonPressed, onInstallWatchFaceButtonPressed)
        is SettingsViewModel.State.NodesAvailable -> LoadedView(state.nodes, onNodeSelected, onRetryButtonPressed)
    }
}

@Composable
private fun LoadingView() {
    LoadingLayout()
}

@Composable
private fun ErrorView(
    error: Exception,
    onRetryButtonPressed: () -> Unit,
) {
    ErrorLayout(
        errorMessage = "An error occurred while getting the list of connected watches. Please try again.\n(${error.message})",
        onRetryButtonClicked = onRetryButtonPressed,
    )
}

@Composable
private fun EmptyStateView(
    onRetryButtonPressed: () -> Unit,
    onInstallWatchFaceButtonPressed: () -> Unit,
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
            text = "No watches found",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Please make sure that your watch is connected to your phone via Bluetooth and that Pixel Minimal Watch Face is installed on your watch too.",
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = onRetryButtonPressed) {
            Text("Retry")
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Watch face not installed on your watch?",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onInstallWatchFaceButtonPressed,
            colors = blueButtonColors(),
        ) {
            Text("Install watch face")
        }
    }
}

@Composable
private fun LoadedView(
    nodes: Set<Node>,
    onNodeSelected: (Node) -> Unit,
    onRetryButtonPressed: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for(node in nodes) {
            WatchNode(
                node = node,
                onNodeSelected = onNodeSelected,
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Can't find your watch?",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Please make sure that your watch is connected to your phone via Bluetooth and that Pixel Minimal Watch Face is installed on your watch too.",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onRetryButtonPressed,
            colors = blueButtonColors(),
        ) {
            Text("Reload")
        }
    }
}

@Composable
private fun WatchNode(
    node: Node,
    onNodeSelected: (Node) -> Unit,
) {
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        onClick =  { onNodeSelected(node) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_watch_24),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = node.displayName,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
        }

    }
}

@Preview
@Composable
private fun EmptyViewPreview() {
    AppMaterialTheme {
        EmptyStateView(
            onRetryButtonPressed = {},
            onInstallWatchFaceButtonPressed = {},
        )
    }
}

@Preview
@Composable
private fun LoadedViewPreview() {
    AppMaterialTheme {
        LoadedView(
            nodes = setOf(
                object : Node {
                    override fun getDisplayName(): String = "Watch 1"
                    override fun getId(): String = "1"
                    override fun isNearby(): Boolean = true
                },
                object : Node {
                    override fun getDisplayName(): String = "Watch 2 with a super long name that takes more than 1 line to see how it behaves"
                    override fun getId(): String = "2"
                    override fun isNearby(): Boolean = true
                },
                object : Node {
                    override fun getDisplayName(): String = "Watch 3 with a medium name"
                    override fun getId(): String = "3"
                    override fun isNearby(): Boolean = true
                }
            ),
            onNodeSelected = {},
            onRetryButtonPressed = {},
        )
    }
}