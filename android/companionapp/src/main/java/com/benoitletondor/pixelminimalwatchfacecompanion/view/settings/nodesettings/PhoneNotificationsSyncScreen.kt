package com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.device.Device
import com.benoitletondor.pixelminimalwatchfacecompanion.platform.PhonePlatform
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings.PhoneSettingsComposeComponents
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.orange
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.primaryBlue
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.primaryGreen
import com.benoitletondor.pixelminimalwatchfacecompanion.view.NAV_NOTIFICATIONS_SYNC_ROUTE

@Composable
fun PhoneNotificationsSyncScreen(
    viewModel: PhoneNotificationsSyncScreenViewModel,
    navController: NavController,
    phonePlatform: PhonePlatform,
    composeComponents: PhoneSettingsComposeComponents,
) {
    AppTopBarScaffold(
        navController = navController,
        showBackButton = true,
        title = "Phone notification icons",
        content = {
            Content(
                phonePlatform = phonePlatform,
                composeComponents = composeComponents,
                hasNotificationsListenerPermission = viewModel.hasNotificationsListenerPermission,
                onSetupButtonPressed = {
                    navController.navigate(NAV_NOTIFICATIONS_SYNC_ROUTE)
                }
            )
        }
    )
}

@Composable
private fun Content(
    phonePlatform: PhonePlatform,
    composeComponents: PhoneSettingsComposeComponents,
    hasNotificationsListenerPermission: Boolean,
    onSetupButtonPressed: () -> Unit,
) {
    val isPhoneNotificationsSyncActivated by phonePlatform.watchIsNotificationsSyncActivated().collectAsState(phonePlatform.isNotificationsSyncActivated())

    composeComponents.PlatformLazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        item(key = "Title") {
            Text(
                text = "You can display the icons of your phone notification icons on the watch face to see them at a glance.",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item(key = "betaDisclaimer") {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(color = orange.copy(alpha = 0.7f), shape = RoundedCornerShape(10))
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "This feature is still in beta",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = "There are some known limitations:",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                )

                Text(
                    text = "- It cannot display your watch notifications, only your phone ones.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                )
                Text(
                    text = "- Since it's phone notifications, there can be a delta between the ones you have on your watch and the icons displayed.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                )
                Text(
                    text = "- It relies on a sync between your phone and watch so things can go wrong: network issues, WearOS specific bugs etc...",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                )
            }
        }

        item(key = "SyncButton") {
            composeComponents.SettingToggleChip(
                checked = isPhoneNotificationsSyncActivated,
                onCheckedChange = phonePlatform::setNotificationsSyncActivated,
                label = "Show phone notification icons",
                iconDrawable = null,
                modifier = Modifier.padding(bottom = 30.dp)
            )
        }

        if (isPhoneNotificationsSyncActivated) {
            item(key = "DebugButton") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ){
                    if (hasNotificationsListenerPermission) {
                        Text(
                            text = "Setup filters and debug",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Button(
                            colors = blueButtonColors(),
                            onClick = onSetupButtonPressed,
                        ) {
                            Text(
                                text = "Setup notification icons sync",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp,
                            )
                        }
                    } else {
                        Text(
                            text = "Missing permission",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "You need to grant notifications permission to continue",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .fillMaxWidth()
                        )

                        Button(
                            onClick = onSetupButtonPressed,
                        ) {
                            Text(
                                text = "Setup notification icons sync",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp,
                            )
                        }
                    }
                }
            }
        }

        item("bottomSpacer") {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}