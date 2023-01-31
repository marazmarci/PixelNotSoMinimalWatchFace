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
package com.benoitletondor.pixelminimalwatchface.common.settings

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchface.common.R
import com.benoitletondor.pixelminimalwatchface.common.compose.productSansFontFamily
import com.benoitletondor.pixelminimalwatchface.common.helper.fontDisplaySizeToHumanReadableString
import com.benoitletondor.pixelminimalwatchface.common.helper.openActivity
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.common.settings.model.Platform
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

interface SettingsScreen {
    fun LazyListScope.BecomePremiumSection()

    val includeHeader: Boolean
    val includeFooter: Boolean
    val weatherTempScaleDisclaimer: String

    @Composable
    fun Screen(
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
    ) {
        val isUserPremium by platform.watchIsUserPremium().collectAsState(platform.isUserPremium())
        val useAndroid12 by platform.watchUseAndroid12Style().collectAsState(platform.useAndroid12Style())
        val showWatchBattery by platform.watchShowWatchBattery().collectAsState(platform.showWatchBattery())
        val showPhoneBattery by platform.watchShowPhoneBattery().collectAsState(platform.showPhoneBattery())
        val showNotifications by platform.watchIsNotificationsSyncActivated().collectAsState(platform.isNotificationsSyncActivated())
        val showComplicationsColorInAmbient by platform.watchShowColorsInAmbientMode().collectAsState(platform.showColorsInAmbientMode())
        val showWearOSLogo by platform.watchShowWearOSLogo().collectAsState(platform.showWearOSLogo())

        composeComponents.PlatformLazyColumn {
            if (includeHeader) {
                item(key = "Title") {
                    composeComponents.PlatformText(
                        text = "Pixel Minimal Watch Face",
                        fontFamily = productSansFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillParentMaxWidth(),
                    )
                }
            }

            item(key = "Android 12 style") {
                composeComponents.SettingToggleChip(
                    label = "Android 12 style",
                    checked = useAndroid12,
                    onCheckedChange = platform::setUseAndroid12Style,
                    iconDrawable = R.drawable.ic_baseline_android,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            if (isUserPremium) {
                WidgetsSection(
                    composeComponents = composeComponents,
                    platform = platform,
                    navController = navController,
                    useAndroid12 = useAndroid12,
                    showPhoneBattery = showPhoneBattery,
                    showWatchBattery = showWatchBattery,
                    showNotifications = showNotifications,
                )
            } else {
                BecomePremiumSection()
            }

            if (!showNotifications || !useAndroid12) {
                item(key = "ShowWearOSLogo") {
                    composeComponents.SettingToggleChip(
                        label = if (useAndroid12 || !isUserPremium) { "Show WearOS logo" } else { "WearOS logo as middle widget" },
                        checked = showWearOSLogo,
                        onCheckedChange = platform::setShowWearOSLogo,
                        iconDrawable = R.drawable.ic_wear_os_logo_white,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }

            if (isUserPremium) {
                BatteryIndicatorSection(
                    composeComponents = composeComponents,
                    platform = platform,
                    navController = navController,
                    useAndroid12 = useAndroid12,
                    showWatchBattery = showWatchBattery,
                    showComplicationsColorInAmbient = showComplicationsColorInAmbient,
                )
            }

            if (isUserPremium) {
                NotificationsDisplaySection(
                    composeComponents = composeComponents,
                    platform = platform,
                    navController = navController,
                    useAndroid12 = useAndroid12,
                    showComplicationsColorInAmbient = showComplicationsColorInAmbient,
                )
            }

            DateTimeSection(
                composeComponents = composeComponents,
                platform = platform,
                navController = navController,
                isUserPremium = isUserPremium,
                showComplicationsColorInAmbient = showComplicationsColorInAmbient,
            )

            TimeStyleSection(
                composeComponents = composeComponents,
                platform = platform,
            )

            AmbientSection(
                composeComponents = composeComponents,
                platform = platform,
                isUserPremium = isUserPremium,
                showWatchBattery = showWatchBattery,
                showPhoneBattery = showPhoneBattery,
                showNotifications = showNotifications,
                useAndroid12 = useAndroid12,
                showComplicationsColorInAmbient = showComplicationsColorInAmbient,
                showWearOSLogo = showWearOSLogo,
            )

            SupportSection(
                composeComponents = composeComponents,
                platform = platform,
                navController = navController,
                isUserPremium = isUserPremium,
            )

            if (includeFooter) {
                item(key = "FooterVersion") {
                    composeComponents.PlatformText(
                        text ="Version: ${platform.appVersionName}",
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }

                item(key = "FooterCopyright") {
                    composeComponents.PlatformText(
                        text ="Made by Benoit Letondor",
                    )
                }
            }
        }
    }

    private fun LazyListScope.WidgetsSection(
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
        useAndroid12: Boolean,
        showPhoneBattery: Boolean,
        showWatchBattery: Boolean,
        showNotifications: Boolean,
    ) {
        item(key = "WidgetsSection") { composeComponents.SettingSectionItem(label = "Widgets") }

        if (useAndroid12) {
            item(key = "Android12Complications") {
                Android12Complications(
                    composeComponents = composeComponents,
                    platform = platform,
                    navController = navController,
                )
            }
        } else {
            item(key = "RegularComplications") {
                RegularComplications(
                    composeComponents = composeComponents,
                    platform = platform,
                    navController = navController,
                    showBattery = showPhoneBattery || showWatchBattery,
                    showNotifications = showNotifications,
                )
            }
        }

        item(key = "WidgetSize") {
            val widgetsSize by platform.watchWidgetsSize().collectAsState(platform.getWidgetsSize())
            val context = LocalContext.current

            composeComponents.SettingSlider(
                iconDrawable = R.drawable.ic_baseline_photo_size_select_small_24,
                onValueChange = platform::setWidgetsSize,
                value = widgetsSize,
                title = "Size of widgets: ${context.fontDisplaySizeToHumanReadableString(widgetsSize)}",
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }

    @Composable
    private fun Android12Complications(
        navController: NavController,
        composeComponents: SettingsComposeComponents,
        platform: Platform,
    ) {
        val complicationColors by platform.watchComplicationColors().collectAsState(platform.getComplicationColors())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                composeComponents.SettingComplicationSlotContainer(
                    modifier = Modifier,
                    onClick = { platform.startWidgetConfigurationScreen(navController, ComplicationLocation.ANDROID_12_TOP_LEFT) },
                ) {
                    composeComponents.SettingComplicationSlot(
                        complicationLocation = ComplicationLocation.ANDROID_12_TOP_LEFT,
                        color = complicationColors.android12TopLeftColor,
                    )
                }

                composeComponents.SettingComplicationSlotContainer(
                    modifier = Modifier,
                    onClick = { platform.startWidgetConfigurationScreen(navController, ComplicationLocation.ANDROID_12_TOP_RIGHT) },
                ) {
                    composeComponents.SettingComplicationSlot(
                        complicationLocation = ComplicationLocation.ANDROID_12_TOP_RIGHT,
                        color = complicationColors.android12TopRightColor,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                composeComponents.SettingComplicationSlotContainer(
                    modifier = Modifier,
                    onClick = { platform.startWidgetConfigurationScreen(navController, ComplicationLocation.ANDROID_12_BOTTOM_LEFT) },
                ) {
                    composeComponents.SettingComplicationSlot(
                        complicationLocation = ComplicationLocation.ANDROID_12_BOTTOM_LEFT,
                        color = complicationColors.android12BottomLeftColor,
                    )
                }

                composeComponents.SettingComplicationSlotContainer(
                    modifier = Modifier,
                    onClick = { platform.startWidgetConfigurationScreen(navController, ComplicationLocation.ANDROID_12_BOTTOM_RIGHT) },
                ) {
                    composeComponents.SettingComplicationSlot(
                        complicationLocation = ComplicationLocation.ANDROID_12_BOTTOM_RIGHT,
                        color = complicationColors.android12BottomRightColor,
                    )
                }
            }
        }
    }

    @Composable
    private fun RegularComplications(
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
        showBattery: Boolean,
        showNotifications: Boolean,
    ) {
        val complicationColors by platform.watchComplicationColors().collectAsState(platform.getComplicationColors())
        val showWearOSLogo by platform.watchShowWearOSLogo().collectAsState(platform.showWearOSLogo())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                composeComponents.SettingComplicationSlotContainer(
                    modifier = Modifier,
                    onClick = { platform.startWidgetConfigurationScreen(navController, ComplicationLocation.LEFT) },
                ) {
                    composeComponents.SettingComplicationSlot(
                        complicationLocation = ComplicationLocation.LEFT,
                        color = complicationColors.leftColor,
                    )
                }

                composeComponents.SettingComplicationSlotContainer(
                    modifier = Modifier,
                    onClick = if(!showWearOSLogo) { { platform.startWidgetConfigurationScreen(navController, ComplicationLocation.MIDDLE) } } else null,
                ) {
                    if (showWearOSLogo) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_wear_os_logo),
                            contentDescription = "Wear OS logo",
                        )
                    } else {
                        composeComponents.SettingComplicationSlot(
                            complicationLocation = ComplicationLocation.MIDDLE,
                            color = complicationColors.middleColor,
                        )
                    }
                }

                composeComponents.SettingComplicationSlotContainer(
                    modifier = Modifier,
                    onClick = { platform.startWidgetConfigurationScreen(navController, ComplicationLocation.RIGHT) },
                ) {
                    composeComponents.SettingComplicationSlot(
                        complicationLocation = ComplicationLocation.RIGHT,
                        color = complicationColors.rightColor,
                    )
                }
            }

            composeComponents.SettingComplicationSlotContainer(
                modifier = Modifier,
                onClick = if (!showBattery && !showNotifications) { { platform.startWidgetConfigurationScreen(navController, ComplicationLocation.BOTTOM) } } else null,
            ) {
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
                    composeComponents.SettingComplicationSlot(
                        complicationLocation = ComplicationLocation.BOTTOM,
                        color = complicationColors.bottomColor,
                    )
                }
            }
        }
    }

    private fun LazyListScope.BatteryIndicatorSection(
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
        useAndroid12: Boolean,
        showWatchBattery: Boolean,
        showComplicationsColorInAmbient: Boolean,
    ) {
        item(key = "BatteryIndicatorSection") {
            composeComponents.SettingSectionItem(
                label = "Display battery status",
                includeBottomPadding = false,
            )
        }

        if (!useAndroid12) {
            item(key = "BatteryIndicatorBottomWidgetWarning") {
                composeComponents.PlatformText(
                    text = "Activating any battery indicator replaces the bottom widget",
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                )
            }
        }

        item(key = "WatchBatteryIndicator") {
            val activity = LocalContext.current as ComponentActivity

            composeComponents.SettingToggleChip(
                label = "Watch battery indicator",
                checked = showWatchBattery,
                onCheckedChange = { showBattery ->
                    val job = activity.lifecycleScope.async {
                        if (showBattery) {
                            if (platform.requestComplicationsPermission(activity)) {
                                platform.setShowWatchBattery(true)
                            }
                        } else {
                            platform.setShowWatchBattery(false)
                        }
                    }

                    job.await()
                },
                iconDrawable = R.drawable.ic_baseline_battery_charging_full,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        item(key = "PhoneBatteryIndicator") {
            val activity = LocalContext.current as ComponentActivity

            composeComponents.SettingChip(
                label = "(Beta) Phone battery indicator setup",
                onClick = {
                    platform.startPhoneBatterySyncConfigScreen(navController, activity)
                },
                iconDrawable = R.drawable.ic_phone,
            )
        }

        item(key = "BatteryIndicatorsColor") {
            val activity = LocalContext.current as ComponentActivity

            composeComponents.SettingChip(
                label = "Battery indicators colors",
                secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
                onClick = {
                    activity.lifecycleScope.launch {
                        val selectedColor = platform.startColorSelectionScreen(navController, activity.getColor(R.color.white))
                        if (selectedColor != null) {
                            try {
                                platform.setBatteryIndicatorColor(selectedColor.color)
                            } catch (e: Exception) {
                                if (e is CancellationException) { throw e }

                                Log.e(TAG, "Error while setting battery indicator color", e)
                                Toast.makeText(activity, "Unable to sync battery indicator color", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                iconDrawable = R.drawable.ic_palette_24,
                modifier = Modifier.heightIn(min = 73.dp),
            )
        }
    }

    private fun LazyListScope.NotificationsDisplaySection(
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
        useAndroid12: Boolean,
        showComplicationsColorInAmbient: Boolean,
    ) {
        item(key = "NotificationsDisplaySection") {
            composeComponents.SettingSectionItem(
                label = "Display phone notifications",
                includeBottomPadding = false,
            )
        }

        if (useAndroid12) {
            item(key = "NotificationsDisplayBottomWidgetWarning") {
                composeComponents.PlatformText(
                    text = "Activating notifications display replaces the WearOS logo",
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                )
            }
        } else {
            item(key = "NotificationsDisplayBottomWidgetWarning") {
                composeComponents.PlatformText(
                    text = "Activating notifications display replaces the bottom widget",
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                )
            }
        }

        item(key = "NotificationsDisplayButton") {
            val activity = LocalContext.current as ComponentActivity

            composeComponents.SettingChip(
                label = "(Beta) Phone notification icons",
                onClick = {
                    platform.startPhoneNotificationIconsConfigScreen(navController, activity)
                },
                iconDrawable = R.drawable.ic_baseline_circle_notifications_24,
            )
        }

        item(key = "NotificationsDisplayColor") {
            val activity = LocalContext.current as ComponentActivity

            composeComponents.SettingChip(
                label = "Notification icons colors",
                secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
                onClick = {
                    activity.lifecycleScope.launch {
                        val selectedColor = platform.startColorSelectionScreen(navController, activity.getColor(R.color.white))
                        if (selectedColor != null) {
                            try {
                                platform.setNotificationIconsColor(selectedColor.color)
                            } catch (e: Exception) {
                                if (e is CancellationException) { throw e }

                                Log.e(TAG, "Error while setting notification icons colors", e)
                                Toast.makeText(activity, "Unable to sync notification icons color", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                iconDrawable = R.drawable.ic_palette_24,
                modifier = Modifier.heightIn(min = 73.dp),
            )
        }
    }

    private fun LazyListScope.DateTimeSection(
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
        isUserPremium: Boolean,
        showComplicationsColorInAmbient: Boolean,
    ) {
        item(key = "DateTimeSection") {
            composeComponents.SettingSectionItem(
                label = "Time and date",
                includeBottomPadding = false,
            )
        }

        item(key = "ShortDateFormat") {
            val useShortDateFormat by platform.watchUseShortDateFormat().collectAsState(platform.getUseShortDateFormat())

            composeComponents.SettingToggleChip(
                label = "Use short date format",
                checked = useShortDateFormat,
                onCheckedChange = platform::setUseShortDateFormat,
                iconDrawable = R.drawable.ic_baseline_short_text,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        val weatherProvider = platform.weatherProvider
        if( isUserPremium && weatherProvider.hasWeatherSupport ) {
            item(key = "ShowWeather") {
                val showWeather by platform.watchShowWeather().collectAsState(platform.showWeather())
                val activity = LocalContext.current as ComponentActivity

                Column {
                    composeComponents.SettingToggleChip(
                        label = "Show weather after date",
                        checked = showWeather,
                        onCheckedChange = { showWeather ->
                            val job = activity.lifecycleScope.async {
                                if (showWeather) {
                                    if (platform.requestComplicationsPermission(activity)) {
                                        platform.setShowWeather(true)
                                    }
                                } else {
                                    platform.setShowWeather(false)
                                }
                            }

                            job.await()
                        },
                        iconDrawable = R.drawable.ic_weather_partly_cloudy,
                    )

                    if (showWeather) {
                        composeComponents.PlatformText(
                            text = weatherTempScaleDisclaimer,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 40.dp, bottom = 4.dp),
                        )

                        if (weatherProvider.weatherProviderInfo != null) {
                            composeComponents.SettingChip(
                                label = "Open Weather app for setup",
                                onClick = {
                                    activity.openActivity(weatherProvider.weatherProviderInfo.appPackage, weatherProvider.weatherProviderInfo.weatherActivityName)
                                },
                                iconDrawable = null,
                                modifier = Modifier.padding(start = 40.dp, bottom = 8.dp),
                            )
                        }

                    }
                }

            }
        }

        item(key = "24hTimeFormatSetting") {
            val use24hTimeFormat by platform.watchUse24hTimeFormat().collectAsState(platform.getUse24hTimeFormat())

            composeComponents.SettingToggleChip(
                label = "Use 24h time format",
                checked = use24hTimeFormat,
                onCheckedChange = platform::setUse24hTimeFormat,
                iconDrawable = R.drawable.ic_access_time,
            )
        }

        item(key = "TimeSize") {
            val timeSize by platform.watchTimeSize().collectAsState(platform.getTimeSize())
            val context = LocalContext.current

            composeComponents.SettingSlider(
                iconDrawable = R.drawable.ic_baseline_format_size,
                onValueChange = platform::setTimeSize,
                value = timeSize,
                title = "Size of time: ${context.fontDisplaySizeToHumanReadableString(timeSize)}",
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item(key = "DateAndBatterySize") {
            val dateAndBatterySize by platform.watchDateAndBatterySize().collectAsState(platform.getDateAndBatterySize())
            val context = LocalContext.current

            composeComponents.SettingSlider(
                iconDrawable = R.drawable.ic_baseline_format_size,
                onValueChange = platform::setDateAndBatterySize,
                value = dateAndBatterySize,
                title = "Size of date & battery: ${context.fontDisplaySizeToHumanReadableString(dateAndBatterySize)}",
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item(key = "TimeColor") {
            val activity = LocalContext.current as ComponentActivity

            composeComponents.SettingChip(
                label = "Time color",
                secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
                onClick = {
                    activity.lifecycleScope.launch {
                        val selectedColor = platform.startColorSelectionScreen(navController, activity.getColor(R.color.white))
                        if (selectedColor != null) {
                            try {
                                platform.setTimeColor(selectedColor.color)
                            } catch (e: Exception) {
                                if (e is CancellationException) { throw e }

                                Log.e(TAG, "Error while setting time color", e)
                                Toast.makeText(activity, "Unable to sync time color", Toast.LENGTH_LONG).show()
                            }
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

            composeComponents.SettingChip(
                label = "Date color",
                secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
                onClick = {
                    activity.lifecycleScope.launch {
                        val selectedColor = platform.startColorSelectionScreen(navController, activity.getColor(R.color.white))
                        if (selectedColor != null) {
                            try {
                                platform.setDateColor(selectedColor.color)
                            } catch (e: Exception) {
                                if (e is CancellationException) { throw e }

                                Log.e(TAG, "Error while setting date color", e)
                                Toast.makeText(activity, "Unable to sync date color", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                iconDrawable = R.drawable.ic_palette_24,
                modifier = Modifier
                    .heightIn(min = 70.dp),
            )
        }

        if( platform.isScreenRound ) {
            item(key = "ShowSecondsRing") {
                val activity = LocalContext.current as ComponentActivity
                val showSecondsRing by platform.watchShowSecondsRing().collectAsState(platform.showSecondsRing())
                val useSweepingSecondsMotion by platform.watchUseSweepingSecondsRingMotion().collectAsState(platform.useSweepingSecondsRingMotion())

                Column {
                    composeComponents.SettingToggleChip(
                        label = "Show seconds ring",
                        checked = showSecondsRing,
                        onCheckedChange = platform::setShowSecondsRing,
                        iconDrawable = R.drawable.ic_baseline_panorama_fish_eye,
                    )

                    if (showSecondsRing) {
                        composeComponents.SettingChip(
                            label = "Seconds ring color",
                            onClick = {
                                activity.lifecycleScope.launch {
                                    val selectedColor = platform.startColorSelectionScreen(navController, activity.getColor(R.color.white))
                                    if (selectedColor != null) {
                                        try {
                                            platform.setSecondRingColor(selectedColor.color)
                                        } catch (e: Exception) {
                                            if (e is CancellationException) { throw e }

                                            Log.e(TAG, "Error while setting seconds ring color", e)
                                            Toast.makeText(activity, "Unable to sync seconds ring color", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            iconDrawable = R.drawable.ic_palette_24,
                            modifier = Modifier.padding(top = 4.dp),
                        )

                        composeComponents.SettingToggleChip(
                            checked = useSweepingSecondsMotion,
                            onCheckedChange = platform::setUseSweepingSecondsRingMotion,
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
        composeComponents: SettingsComposeComponents,
        platform: Platform,
    ) {
        item(key = "TimeStyleSection") {
            composeComponents.SettingSectionItem(
                label = "Time style",
            )
        }

        item(key = "ThinTimeWatchOn") {
            val useThinTimeStyleInRegularMode by platform.watchUseThinTimeStyleInRegularMode().collectAsState(platform.useThinTimeStyleInRegularMode())

            composeComponents.SettingToggleChip(
                label = "Use thin time style when watch is on",
                checked = useThinTimeStyleInRegularMode,
                onCheckedChange = platform::setUseThinTimeStyleInRegularMode,
                iconDrawable = R.drawable.ic_baseline_invert_colors,
                modifier = Modifier.heightIn(min = 70.dp),
            )
        }

        item(key = "NormalTimeWatchOff") {
            val useNormalTimeStyleInAmbientMode by platform.watchUseNormalTimeStyleInAmbientMode().collectAsState(platform.useNormalTimeStyleInAmbientMode())

            composeComponents.SettingToggleChip(
                label = "Use normal in place of thin time style in ambient mode",
                checked = useNormalTimeStyleInAmbientMode,
                onCheckedChange = platform::setUseNormalTimeStyleInAmbientMode,
                iconDrawable = R.drawable.ic_baseline_invert_colors_off,
                modifier = Modifier.heightIn(min = 70.dp),
            )
        }
    }

    private fun LazyListScope.AmbientSection(
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        isUserPremium: Boolean,
        showWatchBattery: Boolean,
        showPhoneBattery: Boolean,
        showNotifications: Boolean,
        useAndroid12: Boolean,
        showWearOSLogo: Boolean,
        showComplicationsColorInAmbient: Boolean,
    ) {
        item(key = "AmbientSection") {
            composeComponents.SettingSectionItem(
                label = "Ambient mode",
            )
        }

        item(key = "ShowDateInAmbientMode") {
            val showDateInAmbient by platform.watchShowDateInAmbient().collectAsState(platform.getShowDateInAmbient())

            composeComponents.SettingToggleChip(
                label = "Show date in ambient mode",
                checked = showDateInAmbient,
                onCheckedChange = platform::setShowDateInAmbient,
                iconDrawable = R.drawable.ic_outline_calendar_today,
            )
        }

        if (isUserPremium) {
            item(key= "ComplicationsInAmbientMode") {
                val showComplicationsInAmbient by platform.watchShowComplicationsInAmbientMode().collectAsState(platform.showComplicationsInAmbientMode())

                composeComponents.SettingToggleChip(
                    label = "Widgets in ambient mode",
                    checked = showComplicationsInAmbient,
                    onCheckedChange = platform::setShowComplicationsInAmbientMode,
                    iconDrawable = R.drawable.ic_settings_power,
                )
            }
        }

        if (isUserPremium && (showWatchBattery || showPhoneBattery)) {
            item(key = "ShowBatteryInAmbientMode") {
                val hideBatteryInAmbient by platform.watchHideBatteryInAmbient().collectAsState(platform.hideBatteryInAmbient())

                composeComponents.SettingToggleChip(
                    label = "Show battery indicators in ambient mode",
                    checked = !hideBatteryInAmbient,
                    onCheckedChange = { platform.setHideBatteryInAmbient(!it) },
                    iconDrawable = R.drawable.ic_settings_power,
                    modifier = Modifier.heightIn(min = 70.dp),
                )
            }
        }

        if (isUserPremium && showNotifications) {
            item(key = "ShowNotificationsInAmbientMode") {
                val showNotificationsInAmbient by platform.watchShowNotificationsInAmbient().collectAsState(platform.getShowNotificationsInAmbient())

                composeComponents.SettingToggleChip(
                    label = "Show notifications in ambient mode",
                    checked = showNotificationsInAmbient,
                    onCheckedChange = platform::setShowNotificationsInAmbient,
                    iconDrawable = R.drawable.ic_baseline_notifications_none_24,
                    modifier = Modifier.heightIn(min = 70.dp),
                )
            }
        }

        if (showWearOSLogo && (!showNotifications || !useAndroid12)) {
            item(key = "ShowWearOSLogoInAmbientMode") {
                val showWearOSLogoInAmbient by platform.watchShowWearOSLogoInAmbient().collectAsState(platform.getShowWearOSLogoInAmbient())

                composeComponents.SettingToggleChip(
                    label = "Show Wear OS logo in ambient mode",
                    checked = showWearOSLogoInAmbient,
                    onCheckedChange = platform::setShowWearOSLogoInAmbient,
                    iconDrawable = R.drawable.ic_wear_os_logo_white,
                    modifier = Modifier.heightIn(min = 70.dp),
                )
            }
        }

        if (platform.isWearOS3) {
            item(key = "showColorsInAmbient") {
                composeComponents.SettingToggleChip(
                    label = "Colors in ambient mode",
                    secondaryLabel = "Caution: can increase battery usage",
                    checked = showComplicationsColorInAmbient,
                    onCheckedChange = platform::setShowColorsInAmbientMode,
                    iconDrawable = R.drawable.ic_palette_24,
                    modifier = Modifier.heightIn(min = 90.dp),
                )
            }
        }
    }

    private fun LazyListScope.SupportSection(
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
        isUserPremium: Boolean
    ) {
        item(key = "SupportSection") {
            composeComponents.SettingSectionItem(
                label = "Support",
            )
        }

        item(key = "GiveFeedback") {
            val activity = LocalContext.current as ComponentActivity
            composeComponents.SettingChip(
                label = "Give your feedback",
                onClick = {
                    platform.setRatingDisplayed(true)
                    platform.startFeedbackScreen(navController, activity)
                },
                iconDrawable = R.drawable.ic_thumbs_up_down,
            )
        }

        if (isUserPremium) {
            item(key = "Donate") {
                val activity = LocalContext.current as ComponentActivity

                composeComponents.SettingChip(
                    label = "Donate to support development",
                    onClick = { platform.startDonationScreen(navController, activity) },
                    iconDrawable = R.drawable.ic_baseline_add_reaction,
                )
            }
        }
    }

    companion object {
        private const val TAG = "SettingsScreen"
    }
}