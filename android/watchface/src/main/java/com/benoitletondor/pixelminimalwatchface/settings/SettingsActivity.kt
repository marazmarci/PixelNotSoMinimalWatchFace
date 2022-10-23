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
package com.benoitletondor.pixelminimalwatchface.settings

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
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.*
import com.benoitletondor.pixelminimalwatchface.*
import com.benoitletondor.pixelminimalwatchface.BuildConfig.COMPANION_APP_PLAYSTORE_URL
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.compose.*
import com.benoitletondor.pixelminimalwatchface.compose.component.*
import com.benoitletondor.pixelminimalwatchface.drawer.digital.android12.Android12DigitalWatchFaceDrawer
import com.benoitletondor.pixelminimalwatchface.drawer.digital.regular.RegularDigitalWatchFaceDrawer
import com.benoitletondor.pixelminimalwatchface.helper.*
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColorsProvider
import com.benoitletondor.pixelminimalwatchface.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.model.Storage
import com.benoitletondor.pixelminimalwatchface.rating.FeedbackActivity
import com.benoitletondor.pixelminimalwatchface.settings.notificationssync.NotificationsSyncConfigurationActivity
import com.benoitletondor.pixelminimalwatchface.settings.phonebattery.PhoneBatteryConfigurationActivity
import com.google.android.wearable.intent.RemoteIntent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resumeWithException

class SettingsActivity : ComponentActivity() {
    private lateinit var storage: Storage

    private lateinit var watchFaceComponentName: ComponentName
    private lateinit var providerInfoRetriever: ProviderInfoRetriever

    private val regularComplicationsMutableFlow = MutableStateFlow(emptyMap<ComplicationLocation, ComplicationProviderInfo?>())
    private val android12ComplicationsMutableFlow = MutableStateFlow(emptyMap<ComplicationLocation, ComplicationProviderInfo?>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        watchFaceComponentName = ComponentName(this, PixelMinimalWatchFace::class.java)
        providerInfoRetriever = ProviderInfoRetriever(this, Dispatchers.IO.asExecutor())
        providerInfoRetriever.init()
        storage = Injection.storage(this)

        setContent {
            SettingsScreen()
        }

        lifecycleScope.launch {
            storage.watchUseAndroid12Style()
                .mapLatest { useAndroid12 ->
                    storage.watchIsUserPremium()
                        .first { it }

                    useAndroid12
                }
                .collect { useAndroid12 ->
                    if (useAndroid12) {
                        updateAndroid12Complications()
                    } else {
                        updateRegularComplications()
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        providerInfoRetriever.release()
    }

    @Composable
    private fun SettingsScreen() {
        val context = LocalContext.current
        val isScreenRound = remember { context.isScreenRound() }
        val weatherProviderInfo = remember { context.getWeatherProviderInfo() }
        val useAndroid12 by storage.watchUseAndroid12Style().collectAsState(storage.useAndroid12Style())
        val isUserPremium by storage.watchIsUserPremium().collectAsState(storage.isUserPremium())
        val showWatchBattery by storage.watchShowWatchBattery().collectAsState(storage.showWatchBattery())
        val showPhoneBattery by storage.watchShowPhoneBattery().collectAsState(storage.showPhoneBattery())
        val showNotifications by storage.watchIsNotificationsSyncActivated().collectAsState(storage.isNotificationsSyncActivated())
        val showWearOSLogo by storage.watchShowWearOSLogo().collectAsState(storage.showWearOSLogo())
        val showComplicationsColorInAmbient by storage.watchShowColorsInAmbientMode().collectAsState(storage.showColorsInAmbientMode())

        WearTheme {
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
                        onCheckedChange = { storage.setUseAndroid12Style(it) },
                        iconDrawable = R.drawable.ic_baseline_android,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }

                WidgetsOrBecomePremiumSection(
                    isUserPremium = isUserPremium,
                    useAndroid12 = useAndroid12,
                    showPhoneBattery = showPhoneBattery,
                    showWatchBattery = showWatchBattery,
                    showNotifications = showNotifications,
                    showWearOSLogo = showWearOSLogo,
                )

                if (isUserPremium) {
                    BatteryIndicatorSection(
                        useAndroid12 = useAndroid12,
                        showWatchBattery = showWatchBattery,
                    )
                }

                if (isUserPremium) {
                    NotificationsDisplaySection(
                        useAndroid12 = useAndroid12,
                    )
                }

                DateTimeSection(
                    isUserPremium = isUserPremium,
                    isScreenRound = isScreenRound,
                    weatherProviderInfo = weatherProviderInfo,
                )

                TimeStyleSection()

                AmbientSection(
                    isUserPremium = isUserPremium,
                    showWatchBattery = showWatchBattery,
                    showPhoneBattery = showPhoneBattery,
                    showNotifications = showNotifications,
                    useAndroid12 = useAndroid12,
                    showWearOSLogo = showWearOSLogo,
                    showComplicationsColorInAmbient = showComplicationsColorInAmbient,
                )

                SupportSection(
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
    }

    private fun LazyListScope.WidgetsOrBecomePremiumSection(
        isUserPremium: Boolean,
        useAndroid12: Boolean,
        showPhoneBattery: Boolean,
        showWatchBattery: Boolean,
        showNotifications: Boolean,
        showWearOSLogo: Boolean,
    ) {


        if (isUserPremium) {
            item(key = "WidgetsSection") { SettingSectionItem(label = "Widgets") }

            if (useAndroid12) {
                item(key = "Android12Complications") {
                    Android12Complications()
                }
            } else {
                item(key = "RegularComplications") {
                    RegularComplications(
                        showBattery = showPhoneBattery || showWatchBattery,
                    )
                }
            }

            item(key = "WidgetSize") {
                val widgetsSize by storage.watchWidgetsSize().collectAsState(storage.getWidgetsSize())
                val context = LocalContext.current

                SettingSlider(
                    iconDrawable = R.drawable.ic_baseline_photo_size_select_small_24,
                    onValueChange = { newValue ->
                        storage.setWidgetsSize(newValue)
                    },
                    value = widgetsSize,
                    title = "Size of widgets: ${context.fontDisplaySizeToHumanReadableString(widgetsSize)}",
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        } else {
            item(key = "PremiumSection") { SettingSectionItem(label = "Premium features") }

            item(key = "PremiumCompanion") {
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
                        onClick = ::openAppOnPhone,
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
                    onCheckedChange = { storage.setShowWearOSLogo(it) },
                    iconDrawable = R.drawable.ic_wear_os_logo_white,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }

    private fun LazyListScope.BatteryIndicatorSection(
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

            SettingToggleChip(
                label = "Watch battery indicator",
                checked = showWatchBattery,
                onCheckedChange = { showBattery ->
                    if (showBattery) {
                        startActivityForResult(
                            ComplicationHelperActivity.createPermissionRequestHelperIntent(
                                context,
                                watchFaceComponentName
                            ),
                            COMPLICATION_BATTERY_PERMISSION_REQUEST_CODE
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
            SettingChip(
                label = "(Beta) Phone battery indicator setup",
                onClick = {
                    startActivityForResult(
                        Intent(this@SettingsActivity, PhoneBatteryConfigurationActivity::class.java),
                        COMPLICATION_PHONE_BATTERY_SETUP_REQUEST_CODE,
                    )
                },
                iconDrawable = R.drawable.ic_phone,
            )
        }

        item(key = "BatteryIndicatorsColor") {
            SettingChip(
                label = "Battery indicators colors",
                secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
                onClick = {
                    startActivityForResult(
                        ColorSelectionActivity.createIntent(
                            this@SettingsActivity,
                            ComplicationColor(getColor(R.color.white), ComplicationColorsProvider.defaultColorName, true)
                        ),
                        BATTERY_COLOR_REQUEST_CODE
                    )
                },
                iconDrawable = R.drawable.ic_palette_24,
                modifier = Modifier.heightIn(min = 73.dp),
            )
        }
    }

    private fun LazyListScope.NotificationsDisplaySection(
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
            SettingChip(
                label = "(Beta) Phone notification icons",
                onClick = {
                    startActivityForResult(
                        Intent(this@SettingsActivity, NotificationsSyncConfigurationActivity::class.java),
                        NOTIFICATIONS_SYNC_SETUP_REQUEST_CODE,
                    )
                },
                iconDrawable = R.drawable.ic_baseline_circle_notifications_24,
            )
        }

        item(key = "NotificationsDisplayColor") {
            SettingChip(
                label = "Notification icons colors",
                secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
                onClick = {
                    startActivityForResult(
                        ColorSelectionActivity.createIntent(
                            this@SettingsActivity,
                            ComplicationColor(getColor(R.color.white), ComplicationColorsProvider.defaultColorName, true)
                        ),
                        NOTIFICATIONS_COLOR_REQUEST_CODE
                    )
                },
                iconDrawable = R.drawable.ic_palette_24,
                modifier = Modifier.heightIn(min = 73.dp),
            )
        }
    }

    private fun LazyListScope.DateTimeSection(
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
                onCheckedChange = { storage.setUseShortDateFormat(it) },
                iconDrawable = R.drawable.ic_baseline_short_text,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        if( isUserPremium && weatherProviderInfo != null ) {
            item(key = "ShowWeather") {
                val showWeather by storage.watchShowWeather().collectAsState(storage.showWeather())
                val context = LocalContext.current

                Column {
                    SettingToggleChip(
                        label = "Show weather after date",
                        checked = showWeather,
                        onCheckedChange = { showWeather ->
                            if (showWeather) {
                                startActivityForResult(
                                    ComplicationHelperActivity.createPermissionRequestHelperIntent(
                                        context,
                                        watchFaceComponentName
                                    ),
                                    COMPLICATION_WEATHER_PERMISSION_REQUEST_CODE
                                )
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
                                    openActivity(weatherProviderInfo.appPackage, weatherProviderInfo.weatherActivityName)
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
                onCheckedChange = { storage.setUse24hTimeFormat(it) },
                iconDrawable = R.drawable.ic_access_time,
            )
        }

        item(key = "TimeSize") {
            val timeSize by storage.watchTimeSize().collectAsState(storage.getTimeSize())
            val context = LocalContext.current

            SettingSlider(
                iconDrawable = R.drawable.ic_baseline_format_size,
                onValueChange = { newValue ->
                    storage.setTimeSize(newValue)
                },
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
                onValueChange = { newValue ->
                    storage.setDateAndBatterySize(newValue)
                },
                value = dateAndBatterySize,
                title = "Size of date & battery: ${context.fontDisplaySizeToHumanReadableString(dateAndBatterySize)}",
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item(key = "TimeColor") {
            SettingChip(
                label = "Time color",
                secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
                onClick = {
                    startActivityForResult(
                        ColorSelectionActivity.createIntent(
                            this@SettingsActivity,
                            ComplicationColor(getColor(R.color.white), ComplicationColorsProvider.defaultColorName, true)
                        ),
                        TIME_COLOR_REQUEST_CODE
                    )
                },
                iconDrawable = R.drawable.ic_palette_24,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .heightIn(min = 70.dp),
            )
        }

        item(key = "DateColor") {
            SettingChip(
                label = "Date color",
                secondaryLabel = if (showComplicationsColorInAmbient) null else "(doesn't affect ambient)",
                onClick = {
                    startActivityForResult(
                        ColorSelectionActivity.createIntent(
                            this@SettingsActivity,
                            ComplicationColor(getColor(R.color.white), ComplicationColorsProvider.defaultColorName, true)
                        ),
                        DATE_COLOR_REQUEST_CODE
                    )
                },
                iconDrawable = R.drawable.ic_palette_24,
                modifier = Modifier
                    .heightIn(min = 70.dp),
            )
        }

        if( isScreenRound ) {
            item(key = "ShowSecondsRing") {
                val showSecondsRing by storage.watchShowSecondsRing().collectAsState(storage.showSecondsRing())

                Column {
                    SettingToggleChip(
                        label = "Show seconds ring",
                        checked = showSecondsRing,
                        onCheckedChange = { storage.setShowSecondsRing(it) },
                        iconDrawable = R.drawable.ic_baseline_panorama_fish_eye,
                    )

                    if (showSecondsRing) {
                        SettingChip(
                            label = "Seconds ring color",
                            onClick = {
                                startActivityForResult(
                                    ColorSelectionActivity.createIntent(
                                        this@SettingsActivity,
                                        ComplicationColor(getColor(R.color.white), ComplicationColorsProvider.defaultColorName, true)
                                    ),
                                    SECONDS_RING_COLOR_REQUEST_CODE
                                )
                            },
                            iconDrawable = R.drawable.ic_palette_24,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }

    private fun LazyListScope.TimeStyleSection() {
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
                onCheckedChange = { storage.setUseThinTimeStyleInRegularMode(it) },
                iconDrawable = R.drawable.ic_baseline_invert_colors,
                modifier = Modifier.heightIn(min = 70.dp),
            )
        }

        item(key = "NormalTimeWatchOff") {
            val useNormalTimeStyleInAmbientMode by storage.watchUseNormalTimeStyleInAmbientMode().collectAsState(storage.useNormalTimeStyleInAmbientMode())

            SettingToggleChip(
                label = "Use normal in place of thin time style in ambient mode",
                checked = useNormalTimeStyleInAmbientMode,
                onCheckedChange = { storage.setUseNormalTimeStyleInAmbientMode(it) },
                iconDrawable = R.drawable.ic_baseline_invert_colors_off,
                modifier = Modifier.heightIn(min = 70.dp),
            )
        }
    }

    private fun LazyListScope.AmbientSection(
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
                onCheckedChange = { storage.setShowDateInAmbient(it) },
                iconDrawable = R.drawable.ic_outline_calendar_today,
            )
        }

        if (isUserPremium) {
            item(key= "ComplicationsInAmbientMode") {
                val showComplicationsInAmbient by storage.watchShowComplicationsInAmbientMode().collectAsState(storage.showComplicationsInAmbientMode())

                SettingToggleChip(
                    label = "Widgets in ambient mode",
                    checked = showComplicationsInAmbient,
                    onCheckedChange = { storage.setShowComplicationsInAmbientMode(it) },
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
                    onCheckedChange = { storage.setShowNotificationsInAmbient(it) },
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
                    onCheckedChange = { storage.setShowWearOSLogoInAmbient(it) },
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
                    onCheckedChange = { storage.setShowColorsInAmbientMode(it) },
                    iconDrawable = R.drawable.ic_palette_24,
                    modifier = Modifier.heightIn(min = 90.dp),
                )
            }
        }
    }

    private fun LazyListScope.SupportSection(
        isUserPremium: Boolean
    ) {
        item(key = "SupportSection") {
            SettingSectionItem(
                label = "Support",
            )
        }

        item(key = "GiveFeedback") {
            SettingChip(
                label = "Give your feedback",
                onClick = {
                    storage.setRatingDisplayed(true)
                    startActivity(Intent(this@SettingsActivity, FeedbackActivity::class.java))
                },
                iconDrawable = R.drawable.ic_thumbs_up_down,
            )
        }

        if (isUserPremium) {
            item(key = "Donate") {
                SettingChip(
                    label = "Donate to support development",
                    onClick = ::openAppForDonationOnPhone,
                    iconDrawable = R.drawable.ic_baseline_add_reaction,
                )
            }
        }
    }

    @Composable
    private fun Android12Complications() {
        val complicationColors by storage.watchComplicationColors().collectAsState(storage.getComplicationColors())
        val complications by android12ComplicationsMutableFlow.collectAsState()

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
                        providerInfo = complications[ComplicationLocation.ANDROID_12_TOP_LEFT],
                        color = complicationColors.android12TopLeftColor,
                        onClick = { startComplicationSelectionActivity(ComplicationLocation.ANDROID_12_TOP_LEFT) },
                    )
                }

                SettingComplicationSlotContainer {
                    SettingComplicationSlot(
                        providerInfo = complications[ComplicationLocation.ANDROID_12_TOP_RIGHT],
                        color = complicationColors.android12TopRightColor,
                        onClick = { startComplicationSelectionActivity(ComplicationLocation.ANDROID_12_TOP_RIGHT) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SettingComplicationSlotContainer {
                    SettingComplicationSlot(
                        providerInfo = complications[ComplicationLocation.ANDROID_12_BOTTOM_LEFT],
                        color = complicationColors.android12BottomLeftColor,
                        onClick = { startComplicationSelectionActivity(ComplicationLocation.ANDROID_12_BOTTOM_LEFT) },
                    )
                }

                SettingComplicationSlotContainer {
                    SettingComplicationSlot(
                        providerInfo = complications[ComplicationLocation.ANDROID_12_BOTTOM_RIGHT],
                        color = complicationColors.android12BottomRightColor,
                        onClick = { startComplicationSelectionActivity(ComplicationLocation.ANDROID_12_BOTTOM_RIGHT) },
                    )
                }
            }
        }
    }

    @Composable
    private fun RegularComplications(
        showBattery: Boolean,
    ) {
        val complicationColors by storage.watchComplicationColors().collectAsState(storage.getComplicationColors())
        val complications by regularComplicationsMutableFlow.collectAsState()
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
                        providerInfo = complications[ComplicationLocation.LEFT],
                        color = complicationColors.leftColor,
                        onClick = { startComplicationSelectionActivity(ComplicationLocation.LEFT) },
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
                            providerInfo = complications[ComplicationLocation.MIDDLE],
                            color = complicationColors.middleColor,
                            onClick = { startComplicationSelectionActivity(ComplicationLocation.MIDDLE) },
                        )
                    }
                }

                SettingComplicationSlotContainer {
                    SettingComplicationSlot(
                        providerInfo = complications[ComplicationLocation.RIGHT],
                        color = complicationColors.rightColor,
                        onClick = { startComplicationSelectionActivity(ComplicationLocation.RIGHT) },
                    )
                }
            }

            SettingComplicationSlotContainer {
                if (showBattery) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_baseline_battery_charging_full),
                        contentDescription = "Battery indicator",
                    )
                } else {
                    SettingComplicationSlot(
                        providerInfo = complications[ComplicationLocation.BOTTOM],
                        color = complicationColors.bottomColor,
                        onClick = { startComplicationSelectionActivity(ComplicationLocation.BOTTOM) },
                    )
                }
            }
        }
    }

    private fun startComplicationSelectionActivity(location: ComplicationLocation) {
        startActivityForResult(
            WidgetConfigurationActivity.createIntent(this, location),
            COMPLICATION_CONFIG_REQUEST_CODE,
        )
    }

    private suspend fun updateRegularComplications() {
        try {
            val complicationIds = RegularDigitalWatchFaceDrawer.ACTIVE_COMPLICATIONS
            val complicationProviders = fetchComplicationProviders(complicationIds)
            regularComplicationsMutableFlow.value = complicationProviders
        } catch (e: Exception) {
            if (e is CancellationException) { throw e }
            Log.e(TAG, "Failed to retrieve regular complication provider info", e)
        }
    }

    private suspend fun updateAndroid12Complications() {
        try {
            val complicationIds = Android12DigitalWatchFaceDrawer.ACTIVE_COMPLICATIONS
            val complicationProviders = fetchComplicationProviders(complicationIds)
            android12ComplicationsMutableFlow.value = complicationProviders
        } catch (e: Exception) {
            if (e is CancellationException) { throw e }
            Log.e(TAG, "Failed to retrieve android 12 complication provider info", e)
        }
    }

    private suspend fun fetchComplicationProviders(complicationIds: IntArray) =
        suspendCancellableCoroutine<Map<ComplicationLocation, ComplicationProviderInfo?>> { continuation ->
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( requestCode == COMPLICATION_WEATHER_PERMISSION_REQUEST_CODE ) {
            val granted = isPermissionGranted("com.google.android.wearable.permission.RECEIVE_COMPLICATION_DATA")
            storage.setShowWeather(granted)
        } else if( requestCode == COMPLICATION_BATTERY_PERMISSION_REQUEST_CODE ) {
            val granted = isPermissionGranted("com.google.android.wearable.permission.RECEIVE_COMPLICATION_DATA")
            storage.setShowWatchBattery(granted)
        } else if ( requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK ) {
            if (storage.useAndroid12Style()) {
                lifecycleScope.launch { updateAndroid12Complications() }
            } else {
                lifecycleScope.launch { updateRegularComplications() }
            }
        } else if ( requestCode == TIME_COLOR_REQUEST_CODE && resultCode == RESULT_OK ) {
            val color = data?.getParcelableExtra<ComplicationColor>(ColorSelectionActivity.RESULT_SELECTED_COLOR)
            if (color != null) {
                storage.setTimeColor(color.color)
            }
        } else if ( requestCode == DATE_COLOR_REQUEST_CODE && resultCode == RESULT_OK ) {
            val color = data?.getParcelableExtra<ComplicationColor>(ColorSelectionActivity.RESULT_SELECTED_COLOR)
            if (color != null) {
                storage.setDateColor(color.color)
            }
        } else if ( requestCode == BATTERY_COLOR_REQUEST_CODE && resultCode == RESULT_OK ) {
            val color = data?.getParcelableExtra<ComplicationColor>(ColorSelectionActivity.RESULT_SELECTED_COLOR)
            if (color != null) {
                storage.setBatteryIndicatorColor(color.color)
            }
        } else if (requestCode == SECONDS_RING_COLOR_REQUEST_CODE && resultCode == RESULT_OK) {
            val color = data?.getParcelableExtra<ComplicationColor>(ColorSelectionActivity.RESULT_SELECTED_COLOR)
            if (color != null) {
                storage.setSecondRingColor(color.color)
            }
        } else if (requestCode == NOTIFICATIONS_COLOR_REQUEST_CODE && resultCode == RESULT_OK) {
            val color = data?.getParcelableExtra<ComplicationColor>(ColorSelectionActivity.RESULT_SELECTED_COLOR)
            if (color != null) {
                storage.setNotificationIconsColor(color.color)
            }
        }
    }

    private fun openAppOnPhone() {
        lifecycleScope.launch {
            if (!openCompanionAppOnPhone("open")) {
                openAppInStoreOnPhone()
            }
        }
    }

    private fun openAppForDonationOnPhone() {
        lifecycleScope.launch {
            if(!openCompanionAppOnPhone("donate")) {
                openAppInStoreOnPhone(finish = false)
            }
        }
    }

    private fun openAppInStoreOnPhone(finish: Boolean = true) {
        when (PhoneDeviceType.getPhoneDeviceType(applicationContext)) {
            PhoneDeviceType.DEVICE_TYPE_ANDROID -> {
                // Create Remote Intent to open Play Store listing of app on remote device.
                val intentAndroid = Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse(COMPANION_APP_PLAYSTORE_URL))

                RemoteIntent.startRemoteActivity(
                    applicationContext,
                    intentAndroid,
                    object : ResultReceiver(Handler()) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            if (resultCode == RemoteIntent.RESULT_OK) {
                                ConfirmationOverlay()
                                    .setFinishedAnimationListener {
                                        if( finish ) {
                                            finish()
                                        }
                                    }
                                    .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                    .setDuration(3000)
                                    .setMessage(getString(R.string.open_phone_url_android_device))
                                    .showOn(this@SettingsActivity)
                            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                                ConfirmationOverlay()
                                    .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                    .setDuration(3000)
                                    .setMessage(getString(R.string.open_phone_url_android_device_failure))
                                    .showOn(this@SettingsActivity)
                            }
                        }
                    }
                )
            }
            PhoneDeviceType.DEVICE_TYPE_IOS -> {
                Toast.makeText(this@SettingsActivity, R.string.open_phone_url_ios_device, Toast.LENGTH_LONG).show()
            }
            PhoneDeviceType.DEVICE_TYPE_ERROR_UNKNOWN -> {
                Toast.makeText(this@SettingsActivity, R.string.open_phone_url_android_device_failure, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val TAG = "SettingsActivity"
        private const val COMPLICATION_WEATHER_PERMISSION_REQUEST_CODE = 1003
        private const val COMPLICATION_BATTERY_PERMISSION_REQUEST_CODE = 1004
        private const val COMPLICATION_CONFIG_REQUEST_CODE = 1005
        private const val COMPLICATION_PHONE_BATTERY_SETUP_REQUEST_CODE = 1006
        private const val TIME_COLOR_REQUEST_CODE = 1007
        private const val BATTERY_COLOR_REQUEST_CODE = 1008
        private const val SECONDS_RING_COLOR_REQUEST_CODE = 1009
        private const val NOTIFICATIONS_SYNC_SETUP_REQUEST_CODE = 1010
        private const val NOTIFICATIONS_COLOR_REQUEST_CODE = 1011
        private const val DATE_COLOR_REQUEST_CODE = 1012
    }
}
