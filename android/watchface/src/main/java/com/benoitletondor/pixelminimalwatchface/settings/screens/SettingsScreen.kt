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
package com.benoitletondor.pixelminimalwatchface.settings.screens

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderInfoRetriever
import android.support.wearable.phone.PhoneDeviceType
import android.support.wearable.view.ConfirmationOverlay
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Text
import com.benoitletondor.pixelminimalwatchface.BuildConfig
import com.benoitletondor.pixelminimalwatchface.Device
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.WeatherProviderInfo
import com.benoitletondor.pixelminimalwatchface.compose.component.RotatoryAwareLazyColumn
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingChip
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingComplicationSlot
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingComplicationSlotContainer
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingSectionItem
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingSlider
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingToggleChip
import com.benoitletondor.pixelminimalwatchface.compose.productSansFontFamily
import com.benoitletondor.pixelminimalwatchface.drawer.digital.android12.Android12DigitalWatchFaceDrawer
import com.benoitletondor.pixelminimalwatchface.drawer.digital.regular.RegularDigitalWatchFaceDrawer
import com.benoitletondor.pixelminimalwatchface.getWeatherProviderInfo
import com.benoitletondor.pixelminimalwatchface.helper.fontDisplaySizeToHumanReadableString
import com.benoitletondor.pixelminimalwatchface.helper.isPermissionGranted
import com.benoitletondor.pixelminimalwatchface.helper.isScreenRound
import com.benoitletondor.pixelminimalwatchface.helper.openActivity
import com.benoitletondor.pixelminimalwatchface.helper.openCompanionAppOnPhone
import com.benoitletondor.pixelminimalwatchface.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.model.Storage
import com.benoitletondor.pixelminimalwatchface.rating.FeedbackActivity
import com.benoitletondor.pixelminimalwatchface.settings.navigateToColorSelectionScreen
import com.benoitletondor.pixelminimalwatchface.settings.navigateToWidgetSelectionScreen
import com.benoitletondor.pixelminimalwatchface.settings.notificationssync.NotificationsSyncConfigurationActivity
import com.benoitletondor.pixelminimalwatchface.settings.phonebattery.PhoneBatteryConfigurationActivity
import com.google.android.wearable.intent.RemoteIntent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException

@Composable
fun SettingsScreen(
    navController: NavHostController,
    storage: Storage,
) {
    val context = LocalContext.current

    val regularComplicationsState = remember { mutableStateMapOf<ComplicationLocation, ComplicationProviderInfo?>() }
    val android12ComplicationsState = remember { mutableStateMapOf<ComplicationLocation, ComplicationProviderInfo?>() }
    val watchFaceComponentName = remember { ComponentName(context, PixelMinimalWatchFace::class.java) }
    val providerInfoRetriever = remember { ProviderInfoRetriever(context, Dispatchers.IO.asExecutor()) }

    LaunchedEffect("updateComplication") {
        launch {
            storage.watchUseAndroid12Style()
                .mapLatest { useAndroid12 ->
                    storage.watchIsUserPremium()
                        .first { it }

                    useAndroid12
                }
                .collect { useAndroid12 ->
                    updateComplications(
                        useAndroid12,
                        providerInfoRetriever,
                        watchFaceComponentName,
                        android12ComplicationsState,
                        regularComplicationsState,
                    )
                }
        }
    }

    DisposableEffect("providerInfoRetriever") {
        providerInfoRetriever.init()

        onDispose {
            providerInfoRetriever.release()
        }
    }

    val isScreenRound = remember { context.isScreenRound() }
    val weatherProviderInfo = remember { context.getWeatherProviderInfo() }
    val useAndroid12 by storage.watchUseAndroid12Style().collectAsState(storage.useAndroid12Style())
    val isUserPremium by storage.watchIsUserPremium().collectAsState(storage.isUserPremium())
    val showWatchBattery by storage.watchShowWatchBattery().collectAsState(storage.showWatchBattery())
    val showPhoneBattery by storage.watchShowPhoneBattery().collectAsState(storage.showPhoneBattery())
    val showNotifications by storage.watchIsNotificationsSyncActivated().collectAsState(storage.isNotificationsSyncActivated())
    val showWearOSLogo by storage.watchShowWearOSLogo().collectAsState(storage.showWearOSLogo())
    val showComplicationsColorInAmbient by storage.watchShowColorsInAmbientMode().collectAsState(storage.showColorsInAmbientMode())

    RotatoryAwareLazyColumn {
        item(key = "Title") {
            Text(
                text = "Pixel Minimal Watch Face",
                fontFamily = productSansFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillParentMaxWidth(),
            )
        }
        item(key = "Android 12 style") {
            SettingToggleChip(
                label = "Android 12 style",
                checked = useAndroid12,
                onCheckedChange = storage::setUseAndroid12Style,
                iconDrawable = R.drawable.ic_baseline_android,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        WidgetsOrBecomePremiumSection(
            storage = storage,
            navController = navController,
            isUserPremium = isUserPremium,
            useAndroid12 = useAndroid12,
            showPhoneBattery = showPhoneBattery,
            showWatchBattery = showWatchBattery,
            showNotifications = showNotifications,
            showWearOSLogo = showWearOSLogo,
            regularComplicationsState = regularComplicationsState,
            android12ComplicationsState = android12ComplicationsState,
        )

        if (isUserPremium) {
            BatteryIndicatorSection(
                storage = storage,
                navController = navController,
                watchFaceComponentName = watchFaceComponentName,
                useAndroid12 = useAndroid12,
                showWatchBattery = showWatchBattery,
                showComplicationsColorInAmbient = showComplicationsColorInAmbient,
            )
        }

        if (isUserPremium) {
            NotificationsDisplaySection(
                navController = navController,
                storage = storage,
                useAndroid12 = useAndroid12,
                showComplicationsColorInAmbient = showComplicationsColorInAmbient,
            )
        }

        DateTimeSection(
            storage = storage,
            navController = navController,
            watchFaceComponentName = watchFaceComponentName,
            isUserPremium = isUserPremium,
            isScreenRound = isScreenRound,
            weatherProviderInfo = weatherProviderInfo,
            showComplicationsColorInAmbient = showComplicationsColorInAmbient,
        )

        TimeStyleSection(
            storage = storage,
        )

        AmbientSection(
            storage = storage,
            isUserPremium = isUserPremium,
            showWatchBattery = showWatchBattery,
            showPhoneBattery = showPhoneBattery,
            showNotifications = showNotifications,
            useAndroid12 = useAndroid12,
            showWearOSLogo = showWearOSLogo,
            showComplicationsColorInAmbient = showComplicationsColorInAmbient,
        )

        SupportSection(
            storage = storage,
            isUserPremium = isUserPremium,
        )

        item(key = "FooterVersion") {
            Text(
                text ="Version: ${BuildConfig.VERSION_NAME}",
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        item(key = "FooterCopyright") {
            Text(
                text ="Made by Benoit Letondor",
            )
        }
    }
}

private fun LazyListScope.WidgetsOrBecomePremiumSection(
    storage: Storage,
    navController: NavHostController,
    isUserPremium: Boolean,
    useAndroid12: Boolean,
    showPhoneBattery: Boolean,
    showWatchBattery: Boolean,
    showNotifications: Boolean,
    showWearOSLogo: Boolean,
    regularComplicationsState: SnapshotStateMap<ComplicationLocation, ComplicationProviderInfo?>,
    android12ComplicationsState: SnapshotStateMap<ComplicationLocation, ComplicationProviderInfo?>,
) {
    if (isUserPremium) {
        item(key = "WidgetsSection") { SettingSectionItem(label = "Widgets") }

        if (useAndroid12) {
            item(key = "Android12Complications") {
                Android12Complications(
                    storage = storage,
                    navController = navController,
                    android12ComplicationsState = android12ComplicationsState,
                )
            }
        } else {
            item(key = "RegularComplications") {
                RegularComplications(
                    storage = storage,
                    navController = navController,
                    regularComplicationsState = regularComplicationsState,
                    showBattery = showPhoneBattery || showWatchBattery,
                    showNotifications = showNotifications,
                )
            }
        }

        item(key = "WidgetSize") {
            val widgetsSize by storage.watchWidgetsSize().collectAsState(storage.getWidgetsSize())
            val context = LocalContext.current

            SettingSlider(
                iconDrawable = R.drawable.ic_baseline_photo_size_select_small_24,
                onValueChange = storage::setWidgetsSize,
                value = widgetsSize,
                title = "Size of widgets: ${context.fontDisplaySizeToHumanReadableString(widgetsSize)}",
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    } else {
        item(key = "PremiumSection") { SettingSectionItem(label = "Premium features") }

        item(key = "PremiumCompanion") {
            val activity = LocalContext.current as ComponentActivity

            Column {
                Text(
                    text = "To setup widgets, display weather, battery indicators and notification icons you have to become a premium user.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillParentMaxWidth(),
                )

                Text(
                    text = "You can buy it from the phone companion app and sync it with your watch to setup premium features right here.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillParentMaxWidth(),
                )

                SettingChip(
                    label ="Become premium",
                    onClick = { openAppOnPhone(activity) },
                    iconDrawable = R.drawable.ic_baseline_stars_24,
                    modifier = Modifier.padding(top = 6.dp),
                )

                Text(
                    text = "Already bought premium? Sync it from the phone app using the \"Troubleshoot\" button.",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(top = 10.dp),
                )
            }
        }
    }

    if (!showNotifications || !useAndroid12) {
        item(key = "ShowWearOSLogo") {
            SettingToggleChip(
                label = if (useAndroid12 || !isUserPremium) { "Show WearOS logo" } else { "WearOS logo as middle widget" },
                checked = showWearOSLogo,
                onCheckedChange = storage::setShowWearOSLogo,
                iconDrawable = R.drawable.ic_wear_os_logo_white,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

private fun LazyListScope.BatteryIndicatorSection(
    storage: Storage,
    navController: NavHostController,
    watchFaceComponentName: ComponentName,
    useAndroid12: Boolean,
    showWatchBattery: Boolean,
    showComplicationsColorInAmbient: Boolean,
) {
    item(key = "BatteryIndicatorSection") {
        SettingSectionItem(
            label = "Display battery status",
            includeBottomPadding = false,
        )
    }

    if (!useAndroid12) {
        item(key = "BatteryIndicatorBottomWidgetWarning") {
            Text(
                text = "Activating any battery indicator replaces the bottom widget",
                fontSize = 12.sp,
                lineHeight = 14.sp,
            )
        }
    }

    item(key = "WatchBatteryIndicator") {
        val context = LocalContext.current

        val permissionActivityResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val granted = context.isPermissionGranted("com.google.android.wearable.permission.RECEIVE_COMPLICATION_DATA")
            storage.setShowWatchBattery(granted)
        }

        SettingToggleChip(
            label = "Watch battery indicator",
            checked = showWatchBattery,
            onCheckedChange = { showBattery ->
                if (showBattery) {
                    permissionActivityResult.launch(
                        ComplicationHelperActivity.createPermissionRequestHelperIntent(
                            context,
                            watchFaceComponentName,
                        )
                    )
                } else {
                    storage.setShowWatchBattery(false)
                }
            },
            iconDrawable = R.drawable.ic_baseline_battery_charging_full,
            modifier = Modifier.padding(top = 10.dp),
        )
    }

    item(key = "PhoneBatteryIndicator") {
        val activity = LocalContext.current as Activity

        SettingChip(
            label = "(Beta) Phone battery indicator setup",
            onClick = {
                activity.startActivity(Intent(activity, PhoneBatteryConfigurationActivity::class.java))
            },
            iconDrawable = R.drawable.ic_phone,
        )
    }

    item(key = "BatteryIndicatorsColor") {
        val activity = LocalContext.current as ComponentActivity

        SettingChip(
            label = "Battery indicators colors",
            secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
            onClick = {
                activity.lifecycleScope.launch {
                    val selectedColor = navController.navigateToColorSelectionScreen(activity.getColor(R.color.white))
                    if (selectedColor != null) {
                        storage.setBatteryIndicatorColor(selectedColor.color)
                    }
                }
            },
            iconDrawable = R.drawable.ic_palette_24,
            modifier = Modifier.heightIn(min = 73.dp),
        )
    }
}

private fun LazyListScope.NotificationsDisplaySection(
    navController: NavHostController,
    storage: Storage,
    useAndroid12: Boolean,
    showComplicationsColorInAmbient: Boolean,
) {
    item(key = "NotificationsDisplaySection") {
        SettingSectionItem(
            label = "Display phone notifications",
            includeBottomPadding = false,
        )
    }

    if (useAndroid12) {
        item(key = "NotificationsDisplayBottomWidgetWarning") {
            Text(
                text = "Activating notifications display replaces the WearOS logo",
                fontSize = 12.sp,
                lineHeight = 14.sp,
            )
        }
    } else {
        item(key = "NotificationsDisplayBottomWidgetWarning") {
            Text(
                text = "Activating notifications display replaces the bottom widget",
                fontSize = 12.sp,
                lineHeight = 14.sp,
            )
        }
    }

    item(key = "NotificationsDisplayButton") {
        val activity = LocalContext.current as Activity

        SettingChip(
            label = "(Beta) Phone notification icons",
            onClick = {
                activity.startActivity(Intent(activity, NotificationsSyncConfigurationActivity::class.java))
            },
            iconDrawable = R.drawable.ic_baseline_circle_notifications_24,
        )
    }

    item(key = "NotificationsDisplayColor") {
        val activity = LocalContext.current as ComponentActivity

        SettingChip(
            label = "Notification icons colors",
            secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
            onClick = {
                activity.lifecycleScope.launch {
                    val selectedColor = navController.navigateToColorSelectionScreen(activity.getColor(R.color.white))
                    if (selectedColor != null) {
                        storage.setNotificationIconsColor(selectedColor.color)
                    }
                }
            },
            iconDrawable = R.drawable.ic_palette_24,
            modifier = Modifier.heightIn(min = 73.dp),
        )
    }
}

private fun LazyListScope.DateTimeSection(
    storage: Storage,
    navController: NavHostController,
    watchFaceComponentName: ComponentName,
    isUserPremium: Boolean,
    isScreenRound: Boolean,
    weatherProviderInfo: WeatherProviderInfo?,
    showComplicationsColorInAmbient: Boolean,
) {
    item(key = "DateTimeSection") {
        SettingSectionItem(
            label = "Time and date",
            includeBottomPadding = false,
        )
    }

    item(key = "ShortDateFormat") {
        val useShortDateFormat by storage.watchUseShortDateFormat().collectAsState(storage.getUseShortDateFormat())

        SettingToggleChip(
            label = "Use short date format",
            checked = useShortDateFormat,
            onCheckedChange = storage::setUseShortDateFormat,
            iconDrawable = R.drawable.ic_baseline_short_text,
            modifier = Modifier.padding(top = 10.dp),
        )
    }

    if( isUserPremium && weatherProviderInfo != null ) {
        item(key = "ShowWeather") {
            val showWeather by storage.watchShowWeather().collectAsState(storage.showWeather())
            val context = LocalContext.current

            val permissionActivityResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val granted = context.isPermissionGranted("com.google.android.wearable.permission.RECEIVE_COMPLICATION_DATA")
                storage.setShowWeather(granted)
            }

            Column {
                SettingToggleChip(
                    label = "Show weather after date",
                    checked = showWeather,
                    onCheckedChange = { showWeather ->
                        if (showWeather) {
                            permissionActivityResult.launch(ComplicationHelperActivity.createPermissionRequestHelperIntent(
                                context,
                                watchFaceComponentName,
                            ))
                        } else {
                            storage.setShowWeather(false)
                        }
                    },
                    iconDrawable = R.drawable.ic_weather_partly_cloudy,
                )

                if (showWeather) {
                    Text(
                        text = "Temperature scale (°F or °C) is controlled by the Weather app.",
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 40.dp, bottom = 4.dp),
                    )

                    SettingChip(
                        label = "Open Weather app for setup",
                        onClick = {
                            weatherProviderInfo.let { weatherProviderInfo ->
                                context.openActivity(weatherProviderInfo.appPackage, weatherProviderInfo.weatherActivityName)
                            }
                        },
                        iconDrawable = null,
                        modifier = Modifier.padding(start = 40.dp, bottom = 8.dp),
                    )
                }
            }

        }
    }

    item(key = "24hTimeFormatSetting") {
        val use24hTimeFormat by storage.watchUse24hTimeFormat().collectAsState(storage.getUse24hTimeFormat())

        SettingToggleChip(
            label = "Use 24h time format",
            checked = use24hTimeFormat,
            onCheckedChange = storage::setUse24hTimeFormat,
            iconDrawable = R.drawable.ic_access_time,
        )
    }

    item(key = "TimeSize") {
        val timeSize by storage.watchTimeSize().collectAsState(storage.getTimeSize())
        val context = LocalContext.current

        SettingSlider(
            iconDrawable = R.drawable.ic_baseline_format_size,
            onValueChange = storage::setTimeSize,
            value = timeSize,
            title = "Size of time: ${context.fontDisplaySizeToHumanReadableString(timeSize)}",
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    item(key = "DateAndBatterySize") {
        val dateAndBatterySize by storage.watchDateAndBatterySize().collectAsState(storage.getDateAndBatterySize())
        val context = LocalContext.current

        SettingSlider(
            iconDrawable = R.drawable.ic_baseline_format_size,
            onValueChange = storage::setDateAndBatterySize,
            value = dateAndBatterySize,
            title = "Size of date & battery: ${context.fontDisplaySizeToHumanReadableString(dateAndBatterySize)}",
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    item(key = "TimeColor") {
        val activity = LocalContext.current as ComponentActivity

        SettingChip(
            label = "Time color",
            secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
            onClick = {
                activity.lifecycleScope.launch {
                    val selectedColor = navController.navigateToColorSelectionScreen(activity.getColor(R.color.white))
                    if (selectedColor != null) {
                        storage.setTimeColor(selectedColor.color)
                    }
                }
            },
            iconDrawable = R.drawable.ic_palette_24,
            modifier = Modifier
                .padding(top = 10.dp)
                .heightIn(min = 70.dp),
        )
    }

    item(key = "DateColor") {
        val activity = LocalContext.current as ComponentActivity

        SettingChip(
            label = "Date color",
            secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
            onClick = {
                activity.lifecycleScope.launch {
                    val selectedColor = navController.navigateToColorSelectionScreen(activity.getColor(R.color.white))
                    if (selectedColor != null) {
                        storage.setDateColor(selectedColor.color)
                    }
                }
            },
            iconDrawable = R.drawable.ic_palette_24,
            modifier = Modifier
                .heightIn(min = 70.dp),
        )
    }

    if( isScreenRound ) {
        item(key = "ShowSecondsRing") {
            val activity = LocalContext.current as ComponentActivity
            val showSecondsRing by storage.watchShowSecondsRing().collectAsState(storage.showSecondsRing())
            val useSweepingSecondsMotion by storage.watchUseSweepingSecondsRingMotion().collectAsState(storage.useSweepingSecondsRingMotion())

            Column {
                SettingToggleChip(
                    label = "Show seconds ring",
                    checked = showSecondsRing,
                    onCheckedChange = storage::setShowSecondsRing,
                    iconDrawable = R.drawable.ic_baseline_panorama_fish_eye,
                )

                if (showSecondsRing) {
                    SettingChip(
                        label = "Seconds ring color",
                        onClick = {
                            activity.lifecycleScope.launch {
                                val selectedColor = navController.navigateToColorSelectionScreen(activity.getColor(R.color.white))
                                if (selectedColor != null) {
                                    storage.setSecondRingColor(selectedColor.color)
                                }
                            }
                        },
                        iconDrawable = R.drawable.ic_palette_24,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    SettingToggleChip(
                        checked = useSweepingSecondsMotion,
                        onCheckedChange = storage::setUseSweepingSecondsRingMotion,
                        label = "Smooth ring motion",
                        iconDrawable = R.drawable.ic_baseline_refresh_24,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

private fun LazyListScope.TimeStyleSection(
    storage: Storage,
) {
    item(key = "TimeStyleSection") {
        SettingSectionItem(
            label = "Time style",
        )
    }

    item(key = "ThinTimeWatchOn") {
        val useThinTimeStyleInRegularMode by storage.watchUseThinTimeStyleInRegularMode().collectAsState(storage.useThinTimeStyleInRegularMode())

        SettingToggleChip(
            label = "Use thin time style when watch is on",
            checked = useThinTimeStyleInRegularMode,
            onCheckedChange = storage::setUseThinTimeStyleInRegularMode,
            iconDrawable = R.drawable.ic_baseline_invert_colors,
            modifier = Modifier.heightIn(min = 70.dp),
        )
    }

    item(key = "NormalTimeWatchOff") {
        val useNormalTimeStyleInAmbientMode by storage.watchUseNormalTimeStyleInAmbientMode().collectAsState(storage.useNormalTimeStyleInAmbientMode())

        SettingToggleChip(
            label = "Use normal in place of thin time style in ambient mode",
            checked = useNormalTimeStyleInAmbientMode,
            onCheckedChange = storage::setUseNormalTimeStyleInAmbientMode,
            iconDrawable = R.drawable.ic_baseline_invert_colors_off,
            modifier = Modifier.heightIn(min = 70.dp),
        )
    }
}

private fun LazyListScope.AmbientSection(
    storage: Storage,
    isUserPremium: Boolean,
    showWatchBattery: Boolean,
    showPhoneBattery: Boolean,
    showNotifications: Boolean,
    useAndroid12: Boolean,
    showWearOSLogo: Boolean,
    showComplicationsColorInAmbient: Boolean,
) {
    item(key = "AmbientSection") {
        SettingSectionItem(
            label = "Ambient mode",
        )
    }

    item(key = "ShowDateInAmbientMode") {
        val showDateInAmbient by storage.watchShowDateInAmbient().collectAsState(storage.getShowDateInAmbient())

        SettingToggleChip(
            label = "Show date in ambient mode",
            checked = showDateInAmbient,
            onCheckedChange = storage::setShowDateInAmbient,
            iconDrawable = R.drawable.ic_outline_calendar_today,
        )
    }

    if (isUserPremium) {
        item(key= "ComplicationsInAmbientMode") {
            val showComplicationsInAmbient by storage.watchShowComplicationsInAmbientMode().collectAsState(storage.showComplicationsInAmbientMode())

            SettingToggleChip(
                label = "Widgets in ambient mode",
                checked = showComplicationsInAmbient,
                onCheckedChange = storage::setShowComplicationsInAmbientMode,
                iconDrawable = R.drawable.ic_settings_power,
            )
        }
    }

    if (isUserPremium && (showWatchBattery || showPhoneBattery)) {
        item(key = "ShowBatteryInAmbientMode") {
            val hideBatteryInAmbient by storage.watchHideBatteryInAmbient().collectAsState(storage.hideBatteryInAmbient())

            SettingToggleChip(
                label = "Show battery indicators in ambient mode",
                checked = !hideBatteryInAmbient,
                onCheckedChange = { storage.setHideBatteryInAmbient(!it) },
                iconDrawable = R.drawable.ic_settings_power,
                modifier = Modifier.heightIn(min = 70.dp),
            )
        }
    }

    if (isUserPremium && showNotifications) {
        item(key = "ShowNotificationsInAmbientMode") {
            val showNotificationsInAmbient by storage.watchShowNotificationsInAmbient().collectAsState(storage.getShowNotificationsInAmbient())

            SettingToggleChip(
                label = "Show notifications in ambient mode",
                checked = showNotificationsInAmbient,
                onCheckedChange = storage::setShowNotificationsInAmbient,
                iconDrawable = R.drawable.ic_baseline_notifications_none_24,
                modifier = Modifier.heightIn(min = 70.dp),
            )
        }
    }

    if (showWearOSLogo && (!showNotifications || !useAndroid12)) {
        item(key = "ShowWearOSLogoInAmbientMode") {
            val showWearOSLogoInAmbient by storage.watchShowWearOSLogoInAmbient().collectAsState(storage.getShowWearOSLogoInAmbient())

            SettingToggleChip(
                label = "Show Wear OS logo in ambient mode",
                checked = showWearOSLogoInAmbient,
                onCheckedChange = storage::setShowWearOSLogoInAmbient,
                iconDrawable = R.drawable.ic_wear_os_logo_white,
                modifier = Modifier.heightIn(min = 70.dp),
            )
        }
    }

    if (Device.isWearOS3) {
        item(key = "showColorsInAmbient") {
            SettingToggleChip(
                label = "Colors in ambient mode",
                secondaryLabel = "Caution: can increase battery usage",
                checked = showComplicationsColorInAmbient,
                onCheckedChange = storage::setShowColorsInAmbientMode,
                iconDrawable = R.drawable.ic_palette_24,
                modifier = Modifier.heightIn(min = 90.dp),
            )
        }
    }
}

private fun LazyListScope.SupportSection(
    storage: Storage,
    isUserPremium: Boolean
) {
    item(key = "SupportSection") {
        SettingSectionItem(
            label = "Support",
        )
    }

    item(key = "GiveFeedback") {
        val activity = LocalContext.current as Activity
        SettingChip(
            label = "Give your feedback",
            onClick = {
                storage.setRatingDisplayed(true)
                activity.startActivity(Intent(activity, FeedbackActivity::class.java))
            },
            iconDrawable = R.drawable.ic_thumbs_up_down,
        )
    }

    if (isUserPremium) {
        item(key = "Donate") {
            val activity = LocalContext.current as ComponentActivity

            SettingChip(
                label = "Donate to support development",
                onClick = { openAppForDonationOnPhone(activity) },
                iconDrawable = R.drawable.ic_baseline_add_reaction,
            )
        }
    }
}

@Composable
private fun Android12Complications(
    storage: Storage,
    navController: NavHostController,
    android12ComplicationsState: SnapshotStateMap<ComplicationLocation, ComplicationProviderInfo?>,
) {
    val complicationColors by storage.watchComplicationColors().collectAsState(storage.getComplicationColors())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(fraction = 0.7f),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SettingComplicationSlotContainer {
                SettingComplicationSlot(
                    providerInfo = android12ComplicationsState[ComplicationLocation.ANDROID_12_TOP_LEFT],
                    color = complicationColors.android12TopLeftColor,
                    onClick = { navController.navigateToWidgetSelectionScreen(ComplicationLocation.ANDROID_12_TOP_LEFT) },
                )
            }

            SettingComplicationSlotContainer {
                SettingComplicationSlot(
                    providerInfo = android12ComplicationsState[ComplicationLocation.ANDROID_12_TOP_RIGHT],
                    color = complicationColors.android12TopRightColor,
                    onClick = { navController.navigateToWidgetSelectionScreen(ComplicationLocation.ANDROID_12_TOP_RIGHT) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(fraction = 0.7f),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SettingComplicationSlotContainer {
                SettingComplicationSlot(
                    providerInfo = android12ComplicationsState[ComplicationLocation.ANDROID_12_BOTTOM_LEFT],
                    color = complicationColors.android12BottomLeftColor,
                    onClick = { navController.navigateToWidgetSelectionScreen(ComplicationLocation.ANDROID_12_BOTTOM_LEFT) },
                )
            }

            SettingComplicationSlotContainer {
                SettingComplicationSlot(
                    providerInfo = android12ComplicationsState[ComplicationLocation.ANDROID_12_BOTTOM_RIGHT],
                    color = complicationColors.android12BottomRightColor,
                    onClick = { navController.navigateToWidgetSelectionScreen(ComplicationLocation.ANDROID_12_BOTTOM_RIGHT) },
                )
            }
        }
    }
}

@Composable
private fun RegularComplications(
    storage: Storage,
    navController: NavHostController,
    regularComplicationsState: SnapshotStateMap<ComplicationLocation, ComplicationProviderInfo?>,
    showBattery: Boolean,
    showNotifications: Boolean,
) {
    val complicationColors by storage.watchComplicationColors().collectAsState(storage.getComplicationColors())
    val showWearOSLogo by storage.watchShowWearOSLogo().collectAsState(storage.showWearOSLogo())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SettingComplicationSlotContainer {
                SettingComplicationSlot(
                    providerInfo = regularComplicationsState[ComplicationLocation.LEFT],
                    color = complicationColors.leftColor,
                    onClick = { navController.navigateToWidgetSelectionScreen(ComplicationLocation.LEFT) },
                )
            }

            SettingComplicationSlotContainer {
                if (showWearOSLogo) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_wear_os_logo),
                        contentDescription = "Wear OS logo",
                    )
                } else {
                    SettingComplicationSlot(
                        providerInfo = regularComplicationsState[ComplicationLocation.MIDDLE],
                        color = complicationColors.middleColor,
                        onClick = { navController.navigateToWidgetSelectionScreen(ComplicationLocation.MIDDLE) },
                    )
                }
            }

            SettingComplicationSlotContainer {
                SettingComplicationSlot(
                    providerInfo = regularComplicationsState[ComplicationLocation.RIGHT],
                    color = complicationColors.rightColor,
                    onClick = { navController.navigateToWidgetSelectionScreen(ComplicationLocation.RIGHT) },
                )
            }
        }

        SettingComplicationSlotContainer {
            if (showBattery) {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_battery_charging_full),
                    contentDescription = "Battery indicator",
                )
            } else if (showNotifications) {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_circle_notifications_24),
                    contentDescription = "Phone notification icons",
                )
            } else {
                SettingComplicationSlot(
                    providerInfo = regularComplicationsState[ComplicationLocation.BOTTOM],
                    color = complicationColors.bottomColor,
                    onClick = { navController.navigateToWidgetSelectionScreen(ComplicationLocation.BOTTOM) },
                )
            }
        }
    }
}

private suspend fun fetchComplicationProviders(
    complicationIds: IntArray,
    providerInfoRetriever: ProviderInfoRetriever,
    watchFaceComponentName: ComponentName,
) = suspendCancellableCoroutine<Map<ComplicationLocation, ComplicationProviderInfo?>> { continuation ->
    val results = mutableMapOf<ComplicationLocation, ComplicationProviderInfo?>()
    var count = 0
    providerInfoRetriever.retrieveProviderInfo(
        object : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
            override fun onProviderInfoReceived(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {
                count++

                val complicationLocation = when (watchFaceComplicationId) {
                    PixelMinimalWatchFace.getComplicationId(ComplicationLocation.LEFT) -> { ComplicationLocation.LEFT }
                    PixelMinimalWatchFace.getComplicationId(ComplicationLocation.MIDDLE) -> { ComplicationLocation.MIDDLE }
                    PixelMinimalWatchFace.getComplicationId(ComplicationLocation.BOTTOM) -> { ComplicationLocation.BOTTOM }
                    PixelMinimalWatchFace.getComplicationId(ComplicationLocation.RIGHT) -> { ComplicationLocation.RIGHT  }
                    PixelMinimalWatchFace.getComplicationId(ComplicationLocation.ANDROID_12_TOP_LEFT) -> { ComplicationLocation.ANDROID_12_TOP_LEFT }
                    PixelMinimalWatchFace.getComplicationId(ComplicationLocation.ANDROID_12_TOP_RIGHT) -> { ComplicationLocation.ANDROID_12_TOP_RIGHT }
                    PixelMinimalWatchFace.getComplicationId(ComplicationLocation.ANDROID_12_BOTTOM_LEFT) -> { ComplicationLocation.ANDROID_12_BOTTOM_LEFT }
                    PixelMinimalWatchFace.getComplicationId(ComplicationLocation.ANDROID_12_BOTTOM_RIGHT) -> { ComplicationLocation.ANDROID_12_BOTTOM_RIGHT }
                    else -> null
                } ?: return

                results[complicationLocation] = complicationProviderInfo

                if (count == complicationIds.size) {
                    if (continuation.isActive) {
                        continuation.resume(results, onCancellation = null)
                    }
                }
            }

            override fun onRetrievalFailed() {
                super.onRetrievalFailed()

                if (continuation.isActive) {
                    continuation.resumeWithException(Exception("Failed to retrieve complication provider info"))
                }
            }
        },
        watchFaceComponentName,
        *complicationIds
    )
}

private fun openAppOnPhone(activity: ComponentActivity) {
    activity.lifecycleScope.launch {
        if (!activity.openCompanionAppOnPhone("open")) {
            openAppInStoreOnPhone(activity)
        }
    }
}

private fun openAppForDonationOnPhone(activity: ComponentActivity) {
    activity.lifecycleScope.launch {
        if(!activity.openCompanionAppOnPhone("donate")) {
            openAppInStoreOnPhone(activity = activity, finish = false)
        }
    }
}

private fun openAppInStoreOnPhone(
    activity: Activity,
    finish: Boolean = true,
) {
    when (PhoneDeviceType.getPhoneDeviceType(activity)) {
        PhoneDeviceType.DEVICE_TYPE_ANDROID -> {
            // Create Remote Intent to open Play Store listing of app on remote device.
            val intentAndroid = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse(BuildConfig.COMPANION_APP_PLAYSTORE_URL))

            RemoteIntent.startRemoteActivity(
                activity,
                intentAndroid,
                object : ResultReceiver(Handler()) {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultCode == RemoteIntent.RESULT_OK) {
                            ConfirmationOverlay()
                                .setFinishedAnimationListener {
                                    if( finish ) {
                                        activity.finish()
                                    }
                                }
                                .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                .setDuration(3000)
                                .setMessage(activity.getString(R.string.open_phone_url_android_device))
                                .showOn(activity)
                        } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                            ConfirmationOverlay()
                                .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                .setDuration(3000)
                                .setMessage(activity.getString(R.string.open_phone_url_android_device_failure))
                                .showOn(activity)
                        }
                    }
                }
            )
        }
        PhoneDeviceType.DEVICE_TYPE_IOS -> {
            Toast.makeText(activity, R.string.open_phone_url_ios_device, Toast.LENGTH_LONG).show()
        }
        PhoneDeviceType.DEVICE_TYPE_ERROR_UNKNOWN -> {
            Toast.makeText(activity, R.string.open_phone_url_android_device_failure, Toast.LENGTH_LONG).show()
        }
    }
}

private suspend fun updateComplications(
    useAndroid12: Boolean,
    providerInfoRetriever: ProviderInfoRetriever,
    watchFaceComponentName: ComponentName,
    android12ComplicationsState: SnapshotStateMap<ComplicationLocation, ComplicationProviderInfo?>,
    regularComplicationsState: SnapshotStateMap<ComplicationLocation, ComplicationProviderInfo?>,
) {
    val complicationIds = if (useAndroid12) {
        Android12DigitalWatchFaceDrawer.ACTIVE_COMPLICATIONS
    } else {
        RegularDigitalWatchFaceDrawer.ACTIVE_COMPLICATIONS
    }

    try {
        val complicationProviders = withContext(Dispatchers.IO) {
            fetchComplicationProviders(
                complicationIds,
                providerInfoRetriever,
                watchFaceComponentName,
            )
        }

        if (useAndroid12) {
            android12ComplicationsState.clear()
            android12ComplicationsState.putAll(complicationProviders)
        } else {
            regularComplicationsState.clear()
            regularComplicationsState.putAll(complicationProviders)
        }
    } catch (e: Exception) {
        if (e is CancellationException) { throw e }
        Log.e("SettingsView", "Failed to retrieve complication provider info", e)
    }
}