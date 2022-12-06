package com.benoitletondor.pixelminimalwatchfacecompanion.view.notificationssync.filter

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchface.common.helper.dpToPx
import com.benoitletondor.pixelminimalwatchfacecompanion.device.Device
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
        val bitmap = remember(key1 = app.appInfo.packageName) { app.appInfo.icon.toBitmap(context.dpToPx(40), context.dpToPx(40)).asImageBitmap() }

        Image(
            bitmap = bitmap,
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