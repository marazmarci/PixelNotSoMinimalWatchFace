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
package com.benoitletondor.pixelminimalwatchfacecompanion.view

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.benoitletondor.pixelminimalwatchface.common.settings.ROUTE_COLOR
import com.benoitletondor.pixelminimalwatchface.common.settings.ROUTE_COLOR_DEFAULT_ARG
import com.benoitletondor.pixelminimalwatchface.common.settings.ROUTE_WIDGET
import com.benoitletondor.pixelminimalwatchface.common.settings.ROUTE_WIDGET_LOCATION_ARG
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColorsProvider
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.startSupportEmailActivity
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarMoreMenuItem
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings.PhoneSettingsComposeComponents
import com.benoitletondor.pixelminimalwatchfacecompanion.view.debugphonebatterysync.DebugPhoneBatterySync
import com.benoitletondor.pixelminimalwatchfacecompanion.view.donation.Donation
import com.benoitletondor.pixelminimalwatchfacecompanion.view.main.subviews.*
import com.benoitletondor.pixelminimalwatchfacecompanion.view.notificationssync.NotificationsSyncView
import com.benoitletondor.pixelminimalwatchfacecompanion.view.notificationssync.filter.NotificationsSyncFilterView
import com.benoitletondor.pixelminimalwatchfacecompanion.view.onboarding.OnboardingView
import com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.NavigateToInstallWatchFaceScreenResult
import com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.SettingsView
import com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings.ColorSelectorScreen
import com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings.NodeSettingsScreen
import com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings.NodeSettingsViewModel
import com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings.PhoneBatterySyncScreen
import com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings.PhoneComplicationColorScreen
import com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings.PhoneNotificationsSyncScreen
import com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings.PhoneWidgetConfigurationScreen
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.net.URLEncoder

private const val NAV_MAIN_ROUTE = "main"
const val NAV_DONATION_ROUTE = "donation"
private const val NAV_ONBOARDING_ROUTE = "onboarding"
const val NAV_DEBUG_PHONE_BATTERY_SYNC_ROUTE = "phoneBatterySyncDebug"
const val NAV_NOTIFICATIONS_SYNC_ROUTE = "notificationsSync"
const val NAV_PHONE_BATTERY_SYNC_SETTINGS_ROUTE = "phoneBatterySyncSettings"
const val NAV_PHONE_NOTIFICATIONS_SYNC_SETTINGS_ROUTE = "phoneNotificationsSyncSettings"
const val NAV_COLOR_SELECTOR_SCREEN_ROUTE = "colorSelectorScreen"
const val NAV_SETTINGS_ROUTE = "settings"
const val NAV_SETTINGS_NODE_ROUTE_ARG = "node"
const val NAV_SETTINGS_NODE_ROUTE = "settings/node/{$NAV_SETTINGS_NODE_ROUTE_ARG}"
const val NAV_NOTIFICATIONS_SYNC_FILTER_ROUTE = "notificationsSyncFilterRoute"
private const val DEEPLINK_SCHEME = "pixelminimalwatchface"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainView()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }
}

@Composable
private fun MainView() {
    val navController = rememberNavController()

    AppMaterialTheme {
        NavHost(navController = navController, startDestination = NAV_MAIN_ROUTE) {
            composable(
                route = NAV_MAIN_ROUTE,
                deepLinks = listOf(navDeepLink { uriPattern = "$DEEPLINK_SCHEME://open" }),
            ) {
                Main(navController, hiltViewModel())
            }
            composable(
                route = NAV_DONATION_ROUTE,
                deepLinks = listOf(navDeepLink { uriPattern = "$DEEPLINK_SCHEME://donate" }),
            ) {
                Donation(navController, hiltViewModel())
            }
            composable(
                route = NAV_DEBUG_PHONE_BATTERY_SYNC_ROUTE,
            ) {
                DebugPhoneBatterySync(navController, hiltViewModel())
            }
            composable(route = NAV_ONBOARDING_ROUTE) {
                OnboardingView(navController, hiltViewModel())
            }
            composable(
                route = NAV_NOTIFICATIONS_SYNC_ROUTE,
                deepLinks = listOf(navDeepLink { uriPattern = "$DEEPLINK_SCHEME://notifications" }),
            ) {
                NotificationsSyncView(navController, hiltViewModel())
            }
            composable(
                route = NAV_NOTIFICATIONS_SYNC_FILTER_ROUTE,
            ) {
                NotificationsSyncFilterView(navController, hiltViewModel())
            }
            composable(
                route = NAV_SETTINGS_ROUTE,
            ) {
                SettingsView(navController, hiltViewModel())
            }
            composable(
                route = NAV_SETTINGS_NODE_ROUTE,
                arguments = listOf(navArgument(NAV_SETTINGS_NODE_ROUTE_ARG) { type = NavType.StringType })
            ) {
                val nodeId: String = it.arguments?.getString(NAV_SETTINGS_NODE_ROUTE_ARG)
                    ?: throw IllegalStateException("Unable to find $NAV_SETTINGS_NODE_ROUTE_ARG arg")

                NodeSettingsScreen(
                    navController = navController,
                    viewModel = hiltViewModel(),
                    nodeId = nodeId,
                )
            }
            composable(
                ROUTE_COLOR,
                arguments = listOf(navArgument(ROUTE_COLOR_DEFAULT_ARG) { type = NavType.IntType })
            ) {
                val complicationColor: Int = it.arguments?.getInt(ROUTE_COLOR_DEFAULT_ARG)
                    ?: throw IllegalStateException("Unable to find $ROUTE_COLOR_DEFAULT_ARG arg")

                PhoneComplicationColorScreen(hiltViewModel()).PhoneScreen(
                    composeComponents = PhoneSettingsComposeComponents(),
                    platform = NodeSettingsViewModel.currentSettingsPlatform ?: throw IllegalStateException("Null currentSettingsPlatform"),
                    navController = navController,
                    defaultColor = ComplicationColor(complicationColor, ComplicationColorsProvider.defaultColorName, true),
                )
            }

            composable(
                ROUTE_WIDGET,
                arguments = listOf(navArgument(ROUTE_WIDGET_LOCATION_ARG) { type = NavType.StringType })
            ) {
                val complicationLocation = ComplicationLocation.valueOf( it.arguments?.getString(
                    ROUTE_WIDGET_LOCATION_ARG
                ) ?: throw IllegalStateException("Unable to find $ROUTE_WIDGET_LOCATION_ARG arg"))

                val phonePlatform = NodeSettingsViewModel.currentSettingsPlatform ?: throw IllegalStateException("Null currentSettingsPlatform")

                PhoneWidgetConfigurationScreen(phonePlatform).PhoneScreen(
                    composeComponents = PhoneSettingsComposeComponents(),
                    platform = phonePlatform,
                    navController = navController,
                    complicationLocation = complicationLocation,
                )
            }

            composable(
                NAV_PHONE_BATTERY_SYNC_SETTINGS_ROUTE
            ) {
                val phonePlatform = NodeSettingsViewModel.currentSettingsPlatform ?: throw IllegalStateException("Null currentSettingsPlatform")

                PhoneBatterySyncScreen(
                    navController = navController,
                    phonePlatform = phonePlatform,
                    composeComponents = PhoneSettingsComposeComponents(),
                    viewModel = hiltViewModel(),
                )
            }

            composable(
                NAV_PHONE_NOTIFICATIONS_SYNC_SETTINGS_ROUTE
            ) {
                val phonePlatform = NodeSettingsViewModel.currentSettingsPlatform ?: throw IllegalStateException("Null currentSettingsPlatform")

                PhoneNotificationsSyncScreen(
                    viewModel = hiltViewModel(),
                    navController = navController,
                    phonePlatform = phonePlatform,
                    composeComponents = PhoneSettingsComposeComponents(),
                )
            }

            composable(
                NAV_COLOR_SELECTOR_SCREEN_ROUTE
            ) {
                ColorSelectorScreen(
                    navController = navController,
                    viewModel = hiltViewModel(),
                )
            }
        }
    }
}

@Composable
private fun Main(navController: NavController, mainViewModel: MainViewModel) {
    val step: MainViewModel.Step by mainViewModel.stepFlow.collectAsState(initial = mainViewModel.step)
    val context = LocalContext.current

    LaunchedEffect("settingsResult") {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow(NavigateToInstallWatchFaceScreenResult, false)
            ?.collect { shouldNavigateToInstallWatch ->
                if (shouldNavigateToInstallWatch){
                    navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>(NavigateToInstallWatchFaceScreenResult)
                    mainViewModel.onNavigateToInstallApp()
                }
            }
    }

    LaunchedEffect("nav") {
        launch {
            mainViewModel.navigationEventFlow.collect { navDestination ->
                when(navDestination) {
                    MainViewModel.NavigationDestination.Donate -> {
                        navController.navigate(NAV_DONATION_ROUTE)
                    }
                    MainViewModel.NavigationDestination.Onboarding -> {
                        navController.navigate(NAV_ONBOARDING_ROUTE)
                    }
                    is MainViewModel.NavigationDestination.VoucherRedeem -> {
                        if ( !context.launchRedeemVoucherFlow(navDestination.voucherCode) ) {
                            MaterialAlertDialogBuilder(context)
                                .setTitle(R.string.iab_purchase_error_title)
                                .setMessage(R.string.iab_purchase_error_message)
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                        }
                    }
                    MainViewModel.NavigationDestination.DebugBatterySync -> {
                        navController.navigate(NAV_DEBUG_PHONE_BATTERY_SYNC_ROUTE)
                    }
                    MainViewModel.NavigationDestination.SetupNotificationsSync -> {
                        navController.navigate(NAV_NOTIFICATIONS_SYNC_ROUTE)
                    }
                    MainViewModel.NavigationDestination.Settings -> {
                        navController.navigate(NAV_SETTINGS_ROUTE)
                    }
                }
            }
        }
    }

    LaunchedEffect("errors") {
        launch {
            mainViewModel.errorEventFlow.collect { errorType ->
                when(errorType) {
                    is MainViewModel.ErrorType.ErrorWhileSyncingWithWatch -> {
                        MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.error_syncing_title)
                            .setMessage(context.getString(R.string.error_syncing_message, errorType.error.message))
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                    MainViewModel.ErrorType.UnableToOpenPlayStoreOnWatch -> {
                        MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.playstore_not_opened_on_watch_title)
                            .setMessage(R.string.playstore_not_opened_on_watch_message)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                    is MainViewModel.ErrorType.UnableToPay -> {
                        MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.error_paying_title)
                            .setMessage(context.getString(R.string.error_paying_message, errorType.error.message))
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                }
            }
        }
    }

    LaunchedEffect("events") {
        launch {
            mainViewModel.eventFlow.collect { eventType ->
                when(eventType) {
                    MainViewModel.EventType.PLAY_STORE_OPENED_ON_WATCH -> {
                        Toast.makeText(context, R.string.playstore_opened_on_watch_message, Toast.LENGTH_LONG).show()
                    }
                    MainViewModel.EventType.SYNC_WITH_WATCH_SUCCEED -> {
                        Toast.makeText(context, "Sync started! Check your watch.", Toast.LENGTH_LONG).show()
                    }
                    MainViewModel.EventType.SHOW_VOUCHER_INPUT -> {
                        context.showRedeemVoucherUI { voucherInput ->
                            mainViewModel.onVoucherInput(voucherInput)
                        }
                    }
                    MainViewModel.EventType.OPEN_SUPPORT_EMAIL -> context.startSupportEmailActivity()
                }
            }
        }
    }

    AppTopBarScaffold(
        navController = navController,
        showBackButton = false,
        title = stringResource(R.string.app_name),
        actions = {
            AppTopBarMoreMenuItem {
                DropdownMenuItem(
                    onClick = mainViewModel::onSupportButtonPressed,
                    text = {
                        Text(stringResource(R.string.send_feedback_cta), fontSize = 18.sp)
                    },
                )
                DropdownMenuItem(
                    onClick = mainViewModel::onDonateButtonPressed,
                    text = {
                        Text("Donate", fontSize = 18.sp)
                    },
                )
                DropdownMenuItem(
                    enabled = false,
                    onClick = {},
                    text = {
                        Text("Version ${BuildConfig.VERSION_NAME}. Made by Benoit Letondor", fontSize = 18.sp)
                    },
                )
            }
        },
        content = {
            when(val currentStep = step) {
                is MainViewModel.Step.Error -> Error(error = currentStep.error) {
                    mainViewModel.retryPremiumStatusCheck()
                }
                is MainViewModel.Step.InstallWatchFace -> InstallWatchFace(step = currentStep, viewModel = mainViewModel)
                MainViewModel.Step.Loading -> Loading()
                is MainViewModel.Step.NotPremium -> NotPremium(viewModel = mainViewModel)
                is MainViewModel.Step.Premium -> Premium(step = currentStep, viewModel = mainViewModel)
                MainViewModel.Step.Syncing -> Syncing()
            }
        }
    )
}

private fun Context.launchRedeemVoucherFlow(voucher: String): Boolean {
    return try {
        val url = "https://play.google.com/redeem?code=" + URLEncoder.encode(voucher, "UTF-8")
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        true
    } catch (e: Exception) {
        false
    }
}

private fun Context.showRedeemVoucherUI(onVoucherInput: (String) -> Unit) {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_redeem_voucher, null)
    val voucherEditText: EditText = dialogView.findViewById(R.id.voucher)

    val builder = MaterialAlertDialogBuilder(this)
        .setTitle(R.string.voucher_redeem_dialog_title)
        .setMessage(R.string.voucher_redeem_dialog_message)
        .setView(dialogView)
        .setPositiveButton(R.string.voucher_redeem_dialog_cta) { dialog, _ ->
            dialog.dismiss()

            val voucher = voucherEditText.text.toString()
            if (voucher.trim { it <= ' ' }.isEmpty()) {
                android.app.AlertDialog.Builder(this)
                    .setTitle(R.string.voucher_redeem_error_dialog_title)
                    .setMessage(R.string.voucher_redeem_error_code_invalid_dialog_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()

                return@setPositiveButton
            }

            onVoucherInput(voucher)
        }
        .setNegativeButton(android.R.string.cancel, null)

    val dialog = builder.show()

    // Directly show keyboard when the dialog pops
    voucherEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        // Check if the device doesn't have a physical keyboard
        if (hasFocus && resources.configuration.keyboard == Configuration.KEYBOARD_NOKEYS) {
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
}