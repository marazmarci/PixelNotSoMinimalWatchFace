package com.benoitletondor.pixelminimalwatchfacecompanion.view.notificationssync.filter

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchface.common.helper.dpToPx
import com.benoitletondor.pixelminimalwatchfacecompanion.device.Device
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.ErrorLayout
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.LoadingLayout
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.primaryRed

@Composable
fun NotificationsSyncFilterView(navController: NavController, viewModel: NotificationsSyncFilterViewModel) {
    AppTopBarScaffold(
        navController = navController,
        showBackButton = true,
        title = "Notification icons filter apps",
        content = {
            val state by viewModel.stateFlow.collectAsState()

            NotificationsSyncFilterAppsLayout(
                state = state,
                onRetryButtonPressed = viewModel::onRetryButtonPressed,
                onAppRowTapped = viewModel::onAppFilteringChanged,
            )
        }
    )
}

@Composable
private fun NotificationsSyncFilterAppsLayout(
    state: NotificationsSyncFilterViewModel.State,
    onRetryButtonPressed: () -> Unit,
    onAppRowTapped: (Device.AppInfo, Boolean) -> Unit,
) {
    when(state) {
        NotificationsSyncFilterViewModel.State.Loading -> Loading()
        is NotificationsSyncFilterViewModel.State.Error -> Error(state.exception, onRetryButtonPressed)
        is NotificationsSyncFilterViewModel.State.Loaded -> Loaded(state.apps, onAppRowTapped)
    }
}

@Composable
private fun Error(
    error: Throwable,
    onRetryButtonPressed: () -> Unit,
) {
    ErrorLayout(
        errorMessage = "An error occurred while getting all installed apps on your device. (${error.message})",
        onRetryButtonClicked = onRetryButtonPressed,
    )
}

@Composable
private fun Loaded(
    apps: List<NotificationsSyncFilterViewModel.App>,
    onAppRowTapped: (Device.AppInfo, Boolean) -> Unit,
) {
    LazyColumn {
        item("explanation") {
            Text(
                text = "All checked apps' notifications will be synced, unchecked will be ignored.",
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 16.dp, top = 6.dp, start = 20.dp, end = 20.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
            )
        }

        for(app in apps) {
            item(key = app.appInfo.packageName) {
                AppRow(app, onAppRowTapped)
            }
        }
    }
}

@Composable
private fun AppRow(
    app: NotificationsSyncFilterViewModel.App,
    onAppRowTapped: (Device.AppInfo, filtered: Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onAppRowTapped(app.appInfo, !app.disabled)
            }
            .padding(start = 20.dp, top = 6.dp, bottom = 6.dp, end = 10.dp)
    ) {
        val context = LocalContext.current
        val iconSize = remember { context.dpToPx(40) }

        Image(
            bitmap = app.appInfo.icon.toBitmap(iconSize, iconSize).asImageBitmap(),
            contentDescription = "App icon",
            modifier = Modifier
                .width(40.dp)
                .height(40.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = app.appInfo.appName,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Checkbox(
            checked = !app.disabled,
            onCheckedChange = { active ->
                onAppRowTapped(app.appInfo, !active)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = primaryRed,
                uncheckedColor = Color.Gray,
            )
        )
    }
}

@Composable
private fun Loading() {
    LoadingLayout()
}

@Composable
@Preview(showSystemUi = true, name = "Sync deactivated")
private fun ErrorPreview() {
    AppMaterialTheme {
        NotificationsSyncFilterAppsLayout(
            state = NotificationsSyncFilterViewModel.State.Error(Exception("Test")),
            onRetryButtonPressed = {},
            onAppRowTapped = { _, _ -> },
        )
    }
}

@Composable
@Preview(showSystemUi = true, name = "Loading")
private fun LoadingPreview() {
    AppMaterialTheme {
        NotificationsSyncFilterAppsLayout(
            state = NotificationsSyncFilterViewModel.State.Loading,
            onRetryButtonPressed = {},
            onAppRowTapped = { _, _ -> },
        )
    }
}

@Composable
@Preview(showSystemUi = true, name = "Loaded")
private fun LoadedPreview() {
    AppMaterialTheme {
        NotificationsSyncFilterAppsLayout(
            state = NotificationsSyncFilterViewModel.State.Loaded(
                apps = listOf(
                    NotificationsSyncFilterViewModel.App(
                        disabled = false,
                        appInfo = Device.AppInfo(
                            packageName = "com.test.1",
                            appName = "Test app 1",
                            icon = ColorDrawable(Color.Blue.toArgb()),
                        )
                    ),
                    NotificationsSyncFilterViewModel.App(
                        disabled = false,
                        appInfo = Device.AppInfo(
                            packageName = "com.test.2",
                            appName = "Test app 2 with super long title that should overflow to see how it looks",
                            icon = ColorDrawable(Color.Red.toArgb()),
                        )
                    ),
                    NotificationsSyncFilterViewModel.App(
                        disabled = true,
                        appInfo = Device.AppInfo(
                            packageName = "com.test.3",
                            appName = "Test app 3",
                            icon = ColorDrawable(Color.Green.toArgb()),
                        )
                    ),
                ),
            ),
            onRetryButtonPressed = {},
            onAppRowTapped = { _, _ -> },
        )
    }
}