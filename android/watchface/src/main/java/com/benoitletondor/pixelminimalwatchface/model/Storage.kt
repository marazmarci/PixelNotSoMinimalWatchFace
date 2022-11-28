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
package com.benoitletondor.pixelminimalwatchface.model

import android.content.Context
import android.graphics.ColorFilter
import androidx.annotation.ColorInt
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.helper.DEFAULT_TIME_SIZE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val DEFAULT_APP_VERSION = -1

private const val SHARED_PREFERENCES_NAME = "pixelMinimalSharedPref"

private const val DEFAULT_COMPLICATION_COLOR = -147282
private const val KEY_COMPLICATION_COLORS = "complicationColors"
private const val KEY_LEFT_COMPLICATION_COLOR = "leftComplicationColor"
private const val KEY_MIDDLE_COMPLICATION_COLOR = "middleComplicationColor"
private const val KEY_RIGHT_COMPLICATION_COLOR = "rightComplicationColor"
private const val KEY_BOTTOM_COMPLICATION_COLOR = "bottomComplicationColor"
private const val KEY_ANDROID_12_TOP_LEFT_COMPLICATION_COLOR = "android12TopLeftComplicationColor"
private const val KEY_ANDROID_12_TOP_RIGHT_COMPLICATION_COLOR = "android12TopRightComplicationColor"
private const val KEY_ANDROID_12_BOTTOM_LEFT_COMPLICATION_COLOR = "android12BottomLeftComplicationColor"
private const val KEY_ANDROID_12_BOTTOM_RIGHT_COMPLICATION_COLOR = "android12BottomRightComplicationColor"
private const val KEY_USER_PREMIUM = "user_premium"
private const val KEY_USE_24H_TIME_FORMAT = "use24hTimeFormat"
private const val KEY_INSTALL_TIMESTAMP = "installTS"
private const val KEY_RATING_NOTIFICATION_SENT = "ratingNotificationSent"
private const val KEY_APP_VERSION = "appVersion"
private const val KEY_SHOW_WEAR_OS_LOGO = "showWearOSLogo"
private const val KEY_SHOW_COMPLICATIONS_AMBIENT = "showComplicationsAmbient"
private const val KEY_SHOW_COLORS_AMBIENT = "showColorsAmbient"
private const val KEY_USE_NORMAL_TIME_STYLE_IN_AMBIENT = "filledTimeAmbient"
private const val KEY_USE_THIN_TIME_STYLE_IN_REGULAR = "thinTimeRegularMode"
private const val KEY_TIME_SIZE = "timeSize"
private const val KEY_DATE_AND_BATTERY_SIZE = "dateSize"
private const val KEY_SECONDS_RING = "secondsRing"
private const val KEY_USE_SWEEPING_SECONDS_MOTION = "useSweepingSecondsMotion"
private const val KEY_SHOW_WEATHER = "showWeather"
private const val KEY_SHOW_WATCH_BATTERY = "showBattery"
private const val KEY_SHOW_PHONE_BATTERY = "showPhoneBattery"
private const val KEY_FEATURE_DROP_2022_NOTIFICATION = "featureDrop2022Notification_3"
private const val KEY_USE_SHORT_DATE_FORMAT = "useShortDateFormat"
private const val KEY_SHOW_DATE_AMBIENT = "showDateAmbient"
private const val KEY_TIME_COLOR = "timeAndDateColor"
private const val KEY_DATE_COLOR = "dateColor"
private const val KEY_BATTERY_COLOR = "batteryColor"
private const val KEY_USE_ANDROID_12_STYLE = "useAndroid12Style"
private const val KEY_HIDE_BATTERY_IN_AMBIENT = "hideBatteryInAmbient"
private const val KEY_SECONDS_RING_COLOR = "secondsRingColor"
private const val KEY_WIDGETS_SIZE = "widgetSize"
private const val KEY_NOTIFICATIONS_SYNC_ENABLED = "notificationsSyncEnabled"
private const val KEY_NOTIFICATION_ICONS_COLOR = "notificationIconsColor"
private const val KEY_SHOW_NOTIFICATIONS_AMBIENT = "showNotificationsAmbient"
private const val KEY_SHOW_WEAR_OS_LOGO_AMBIENT = "showWearOSLogoAmbient"
private const val KEY_BETA_NOTIFICATIONS_DISCLAIMER_SHOWN = "betaNotificationsDisclaimerBeenShown"

interface Storage {
    fun getComplicationColors(): ComplicationColors
    fun setComplicationColors(complicationColors: ComplicationColors)
    fun watchComplicationColors(): Flow<ComplicationColors>
    fun isUserPremium(): Boolean
    fun setUserPremium(premium: Boolean)
    fun watchIsUserPremium(): Flow<Boolean>
    fun setUse24hTimeFormat(use: Boolean)
    fun getUse24hTimeFormat(): Boolean
    fun watchUse24hTimeFormat(): Flow<Boolean>
    fun getInstallTimestamp(): Long
    fun hasRatingBeenDisplayed(): Boolean
    fun setRatingDisplayed(sent: Boolean)
    fun getAppVersion(): Int
    fun setAppVersion(version: Int)
    fun showWearOSLogo(): Boolean
    fun setShowWearOSLogo(shouldShowWearOSLogo: Boolean)
    fun watchShowWearOSLogo(): Flow<Boolean>
    fun showComplicationsInAmbientMode(): Boolean
    fun setShowComplicationsInAmbientMode(show: Boolean)
    fun watchShowComplicationsInAmbientMode(): Flow<Boolean>
    fun showColorsInAmbientMode(): Boolean
    fun setShowColorsInAmbientMode(show: Boolean)
    fun watchShowColorsInAmbientMode(): Flow<Boolean>
    fun useNormalTimeStyleInAmbientMode(): Boolean
    fun setUseNormalTimeStyleInAmbientMode(useNormalTime: Boolean)
    fun watchUseNormalTimeStyleInAmbientMode(): Flow<Boolean>
    fun useThinTimeStyleInRegularMode(): Boolean
    fun setUseThinTimeStyleInRegularMode(useThinTime: Boolean)
    fun watchUseThinTimeStyleInRegularMode(): Flow<Boolean>
    fun getTimeSize(): Int
    fun setTimeSize(timeSize: Int)
    fun watchTimeSize(): Flow<Int>
    fun getDateAndBatterySize(): Int
    fun setDateAndBatterySize(size: Int)
    fun watchDateAndBatterySize(): Flow<Int>
    fun showSecondsRing(): Boolean
    fun setShowSecondsRing(showSecondsRing: Boolean)
    fun watchShowSecondsRing(): Flow<Boolean>
    fun useSweepingSecondsMotion(): Boolean
    fun setUseSweepingSecondsMotion(useSweepingSecondsMotion: Boolean)
    fun watchUseSweepingSecondsMotion(): Flow<Boolean>
    fun showWeather(): Boolean
    fun setShowWeather(show: Boolean)
    fun watchShowWeather(): Flow<Boolean>
    fun showWatchBattery(): Boolean
    fun setShowWatchBattery(show: Boolean)
    fun watchShowWatchBattery(): Flow<Boolean>
    fun hasFeatureDropAutumn2022NotificationBeenShown(): Boolean
    fun setFeatureDropAutumn2022NotificationShown()
    fun getUseShortDateFormat(): Boolean
    fun setUseShortDateFormat(useShortDateFormat: Boolean)
    fun watchUseShortDateFormat(): Flow<Boolean>
    fun setShowDateInAmbient(showDateInAmbient: Boolean)
    fun getShowDateInAmbient(): Boolean
    fun watchShowDateInAmbient(): Flow<Boolean>
    fun showPhoneBattery(): Boolean
    fun setShowPhoneBattery(show: Boolean)
    fun watchShowPhoneBattery(): Flow<Boolean>
    @ColorInt fun getTimeColor(): Int
    fun getTimeColorFilter(): ColorFilter
    fun setTimeColor(@ColorInt color: Int)
    @ColorInt fun getDateColor(): Int
    fun getDateColorFilter(): ColorFilter
    fun setDateColor(@ColorInt color: Int)
    @ColorInt fun getBatteryIndicatorColor(): Int
    fun getBatteryIndicatorColorFilter(): ColorFilter
    fun setBatteryIndicatorColor(@ColorInt color: Int)
    fun useAndroid12Style(): Boolean
    fun setUseAndroid12Style(useAndroid12Style: Boolean)
    fun watchUseAndroid12Style(): Flow<Boolean>
    fun hideBatteryInAmbient(): Boolean
    fun setHideBatteryInAmbient(hide: Boolean)
    fun watchHideBatteryInAmbient(): Flow<Boolean>
    fun getSecondRingColor(): ColorFilter
    fun setSecondRingColor(@ColorInt color: Int)
    fun getWidgetsSize(): Int
    fun setWidgetsSize(widgetsSize: Int)
    fun watchWidgetsSize(): Flow<Int>
    fun isNotificationsSyncActivated(): Boolean
    fun setNotificationsSyncActivated(activated: Boolean)
    fun watchIsNotificationsSyncActivated(): Flow<Boolean>
    fun setNotificationIconsColor(@ColorInt color: Int)
    @ColorInt fun getNotificationIconsColor(): Int
    fun getNotificationIconsColorFilter(): ColorFilter
    fun getShowNotificationsInAmbient(): Boolean
    fun setShowNotificationsInAmbient(show: Boolean)
    fun watchShowNotificationsInAmbient(): Flow<Boolean>
    fun getShowWearOSLogoInAmbient(): Boolean
    fun setShowWearOSLogoInAmbient(show: Boolean)
    fun watchShowWearOSLogoInAmbient(): Flow<Boolean>
    fun hasBetaNotificationsDisclaimerBeenShown(): Boolean
    fun setBetaNotificationsDisclaimerShown()
}

class StorageImpl(
    context: Context,
) : Storage {
    private val appContext = context.applicationContext
    private val sharedPreferences = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    // Those values will be called up to 60 times a minute when not in ambient mode
    // SharedPreferences uses a map so we cache the values to avoid map lookups
    private val timeSizeCache = StorageCachedIntValue(sharedPreferences, KEY_TIME_SIZE, DEFAULT_TIME_SIZE)
    private val dateAndBatterySizeCache = StorageCachedIntValue(sharedPreferences, KEY_DATE_AND_BATTERY_SIZE, getTimeSize())
    private val isPremiumUserCache = StorageCachedBoolValue(sharedPreferences, KEY_USER_PREMIUM, false)
    private val use24hFormatCache = StorageCachedBoolValue(sharedPreferences, KEY_USE_24H_TIME_FORMAT, true)
    private val showWearOSLogoCache = StorageCachedBoolValue(sharedPreferences, KEY_SHOW_WEAR_OS_LOGO, true)
    private val showComplicationsInAmbientModeCache = StorageCachedBoolValue(sharedPreferences, KEY_SHOW_COMPLICATIONS_AMBIENT, false)
    private val showColorsInAmbientModeCache = StorageCachedBoolValue(sharedPreferences, KEY_SHOW_COLORS_AMBIENT, false)
    private val showSecondsRingCache = StorageCachedBoolValue(sharedPreferences, KEY_SECONDS_RING, false)
    private val useSweepingSecondsMotionCache = StorageCachedBoolValue(sharedPreferences, KEY_USE_SWEEPING_SECONDS_MOTION, false)
    private val showWeatherCache = StorageCachedBoolValue(sharedPreferences, KEY_SHOW_WEATHER, false)
    private val showWatchBattery = StorageCachedBoolValue(sharedPreferences, KEY_SHOW_WATCH_BATTERY, false)
    private val useShortDateFormatCache = StorageCachedBoolValue(sharedPreferences, KEY_USE_SHORT_DATE_FORMAT, false)
    private val showDateInAmbientCache = StorageCachedBoolValue(sharedPreferences, KEY_SHOW_DATE_AMBIENT, true)
    private val showPhoneBatteryCache = StorageCachedBoolValue(sharedPreferences, KEY_SHOW_PHONE_BATTERY, false)
    private val timeColorCache = StorageCachedColorValue(sharedPreferences, appContext, KEY_TIME_COLOR, R.color.white)
    private val dateColorCache = StorageCachedResolvedColorValue(sharedPreferences, KEY_DATE_COLOR, timeColorCache.get().color)
    private val batteryIndicatorColorCache = StorageCachedColorValue(sharedPreferences, appContext, KEY_BATTERY_COLOR, R.color.white)
    private val cacheComplicationsColorMutableFlow = MutableStateFlow(loadComplicationColors())
    private val useAndroid12StyleCache = StorageCachedBoolValue(sharedPreferences, KEY_USE_ANDROID_12_STYLE, false)
    private val hideBatteryInAmbientCache = StorageCachedBoolValue(sharedPreferences, KEY_HIDE_BATTERY_IN_AMBIENT, false)
    private val secondRingColorCache = StorageCachedColorValue(sharedPreferences, appContext, KEY_SECONDS_RING_COLOR, R.color.white)
    private val widgetsSizeCache = StorageCachedIntValue(sharedPreferences, KEY_WIDGETS_SIZE, DEFAULT_TIME_SIZE)
    private val useNormalTimeStyleInAmbientModeCache = StorageCachedBoolValue(sharedPreferences, KEY_USE_NORMAL_TIME_STYLE_IN_AMBIENT, false)
    private val useThinTimeStyleInNormalModeCache = StorageCachedBoolValue(sharedPreferences, KEY_USE_THIN_TIME_STYLE_IN_REGULAR, false)
    private val hasRatingBeenDisplayedCache = StorageCachedBoolValue(sharedPreferences, KEY_RATING_NOTIFICATION_SENT, false)
    private val notificationsSyncEnabledCache = StorageCachedBoolValue(sharedPreferences, KEY_NOTIFICATIONS_SYNC_ENABLED, false)
    private val notificationIconsColorCache = StorageCachedColorValue(sharedPreferences, appContext, KEY_NOTIFICATION_ICONS_COLOR, R.color.white)
    private val showNotificationsInAmbientCache = StorageCachedBoolValue(sharedPreferences, KEY_SHOW_NOTIFICATIONS_AMBIENT, false)
    private val showWearOSLogoInAmbientCache = StorageCachedBoolValue(sharedPreferences, KEY_SHOW_WEAR_OS_LOGO_AMBIENT, true)
    private val betaNotificationsDisclaimerShownCache = StorageCachedBoolValue(sharedPreferences, KEY_BETA_NOTIFICATIONS_DISCLAIMER_SHOWN, false)

    init {
        if( getInstallTimestamp() < 0 ) {
            sharedPreferences.edit().putLong(KEY_INSTALL_TIMESTAMP, System.currentTimeMillis()).apply()
        }
    }

    private fun loadComplicationColors(): ComplicationColors {
        val baseColor = sharedPreferences.getInt(
            KEY_COMPLICATION_COLORS,
            DEFAULT_COMPLICATION_COLOR
        )

        val leftColor = sharedPreferences.getInt(
            KEY_LEFT_COMPLICATION_COLOR,
            baseColor
        )

        val middleColor = sharedPreferences.getInt(
            KEY_MIDDLE_COMPLICATION_COLOR,
            baseColor
        )

        val rightColor = sharedPreferences.getInt(
            KEY_RIGHT_COMPLICATION_COLOR,
            baseColor
        )

        val bottomColor = sharedPreferences.getInt(
            KEY_BOTTOM_COMPLICATION_COLOR,
            baseColor
        )

        val android12TopLeftColor = sharedPreferences.getInt(
            KEY_ANDROID_12_TOP_LEFT_COMPLICATION_COLOR,
            baseColor
        )

        val android12TopRightColor = sharedPreferences.getInt(
            KEY_ANDROID_12_TOP_RIGHT_COMPLICATION_COLOR,
            baseColor
        )

        val android12BottomLeftColor = sharedPreferences.getInt(
            KEY_ANDROID_12_BOTTOM_LEFT_COMPLICATION_COLOR,
            baseColor
        )

        val android12BottomRightColor = sharedPreferences.getInt(
            KEY_ANDROID_12_BOTTOM_RIGHT_COMPLICATION_COLOR,
            baseColor
        )

        val defaultColors = ComplicationColorsProvider.getDefaultComplicationColors()

        return ComplicationColors(
            if( leftColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.leftColor } else { ComplicationColor(leftColor, ComplicationColorsProvider.getLabelForColor(leftColor),false) },
            if( middleColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.middleColor } else { ComplicationColor(middleColor, ComplicationColorsProvider.getLabelForColor(middleColor),false) },
            if( rightColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.rightColor } else { ComplicationColor(rightColor, ComplicationColorsProvider.getLabelForColor(rightColor),false) },
            if( bottomColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.bottomColor } else { ComplicationColor(bottomColor, ComplicationColorsProvider.getLabelForColor(bottomColor),false) },
            if( android12TopLeftColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.android12TopLeftColor } else { ComplicationColor(android12TopLeftColor, ComplicationColorsProvider.getLabelForColor(android12TopLeftColor),false) },
            if( android12TopRightColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.android12TopRightColor } else { ComplicationColor(android12TopRightColor, ComplicationColorsProvider.getLabelForColor(android12TopRightColor),false) },
            if( android12BottomLeftColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.android12BottomLeftColor } else { ComplicationColor(android12BottomLeftColor, ComplicationColorsProvider.getLabelForColor(android12BottomLeftColor),false) },
            if( android12BottomRightColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.android12BottomRightColor } else { ComplicationColor(android12BottomRightColor, ComplicationColorsProvider.getLabelForColor(android12BottomRightColor),false) },
        )
    }

    override fun getComplicationColors(): ComplicationColors = cacheComplicationsColorMutableFlow.value

    override fun setComplicationColors(complicationColors: ComplicationColors) {
        cacheComplicationsColorMutableFlow.value = complicationColors
        sharedPreferences.edit()
            .putInt(
                KEY_LEFT_COMPLICATION_COLOR,
                if( complicationColors.leftColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.leftColor.color }
            )
            .putInt(
                KEY_MIDDLE_COMPLICATION_COLOR,
                if( complicationColors.middleColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.middleColor.color }
            )
            .putInt(
                KEY_RIGHT_COMPLICATION_COLOR,
                if( complicationColors.rightColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.rightColor.color }
            )
            .putInt(
                KEY_BOTTOM_COMPLICATION_COLOR,
                if( complicationColors.bottomColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.bottomColor.color }
            )
            .putInt(
                KEY_ANDROID_12_BOTTOM_LEFT_COMPLICATION_COLOR,
                if( complicationColors.android12BottomLeftColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.android12BottomLeftColor.color }
            )
            .putInt(
                KEY_ANDROID_12_TOP_LEFT_COMPLICATION_COLOR,
                if( complicationColors.android12TopLeftColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.android12TopLeftColor.color }
            )
            .putInt(
                KEY_ANDROID_12_TOP_RIGHT_COMPLICATION_COLOR,
                if( complicationColors.android12TopRightColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.android12TopRightColor.color }
            )
            .putInt(
                KEY_ANDROID_12_BOTTOM_RIGHT_COMPLICATION_COLOR,
                if( complicationColors.android12BottomRightColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.android12BottomRightColor.color }
            )
            .apply()
    }

    override fun watchComplicationColors(): StateFlow<ComplicationColors> = cacheComplicationsColorMutableFlow

    override fun isUserPremium(): Boolean = isPremiumUserCache.get()

    override fun setUserPremium(premium: Boolean) = isPremiumUserCache.set(premium)

    override fun watchIsUserPremium(): Flow<Boolean> = isPremiumUserCache.watchChanges()

    override fun setUse24hTimeFormat(use: Boolean) = use24hFormatCache.set(use)

    override fun getUse24hTimeFormat(): Boolean = use24hFormatCache.get()

    override fun watchUse24hTimeFormat(): Flow<Boolean> = use24hFormatCache.watchChanges()

    override fun getInstallTimestamp(): Long {
        return sharedPreferences.getLong(KEY_INSTALL_TIMESTAMP, -1)
    }

    override fun hasRatingBeenDisplayed(): Boolean = hasRatingBeenDisplayedCache.get()

    override fun setRatingDisplayed(sent: Boolean) = hasRatingBeenDisplayedCache.set(sent)

    override fun getAppVersion(): Int {
        return sharedPreferences.getInt(KEY_APP_VERSION, DEFAULT_APP_VERSION)
    }

    override fun setAppVersion(version: Int) {
        sharedPreferences.edit().putInt(KEY_APP_VERSION, version).apply()
    }

    override fun showWearOSLogo(): Boolean = showWearOSLogoCache.get()

    override fun setShowWearOSLogo(shouldShowWearOSLogo: Boolean) = showWearOSLogoCache.set(shouldShowWearOSLogo)

    override fun watchShowWearOSLogo(): Flow<Boolean> = showWearOSLogoCache.watchChanges()

    override fun showComplicationsInAmbientMode(): Boolean = showComplicationsInAmbientModeCache.get()

    override fun setShowComplicationsInAmbientMode(show: Boolean) = showComplicationsInAmbientModeCache.set(show)

    override fun watchShowComplicationsInAmbientMode(): Flow<Boolean> = showComplicationsInAmbientModeCache.watchChanges()

    override fun showColorsInAmbientMode(): Boolean = showColorsInAmbientModeCache.get()

    override fun setShowColorsInAmbientMode(show: Boolean) = showColorsInAmbientModeCache.set(show)

    override fun watchShowColorsInAmbientMode(): Flow<Boolean> = showColorsInAmbientModeCache.watchChanges()

    override fun useNormalTimeStyleInAmbientMode(): Boolean = useNormalTimeStyleInAmbientModeCache.get()

    override fun setUseNormalTimeStyleInAmbientMode(useNormalTime: Boolean) = useNormalTimeStyleInAmbientModeCache.set(useNormalTime)

    override fun watchUseNormalTimeStyleInAmbientMode(): Flow<Boolean> = useNormalTimeStyleInAmbientModeCache.watchChanges()

    override fun useThinTimeStyleInRegularMode(): Boolean = useThinTimeStyleInNormalModeCache.get()

    override fun setUseThinTimeStyleInRegularMode(useThinTime: Boolean) = useThinTimeStyleInNormalModeCache.set(useThinTime)

    override fun watchUseThinTimeStyleInRegularMode(): Flow<Boolean> = useThinTimeStyleInNormalModeCache.watchChanges()

    override fun getTimeSize(): Int = timeSizeCache.get()

    override fun setTimeSize(timeSize: Int) = timeSizeCache.set(timeSize)

    override fun watchTimeSize(): Flow<Int> = timeSizeCache.watchChanges()

    override fun getDateAndBatterySize(): Int = dateAndBatterySizeCache.get()

    override fun setDateAndBatterySize(size: Int) = dateAndBatterySizeCache.set(size)

    override fun watchDateAndBatterySize(): Flow<Int> = dateAndBatterySizeCache.watchChanges()

    override fun showSecondsRing(): Boolean = showSecondsRingCache.get()

    override fun setShowSecondsRing(showSecondsRing: Boolean) = showSecondsRingCache.set(showSecondsRing)

    override fun watchShowSecondsRing(): Flow<Boolean> = showSecondsRingCache.watchChanges()

    override fun useSweepingSecondsMotion() = useSweepingSecondsMotionCache.get()

    override fun setUseSweepingSecondsMotion(useSweepingSecondsMotion: Boolean) {
        useSweepingSecondsMotionCache.set(useSweepingSecondsMotion)
    }

    override fun watchUseSweepingSecondsMotion(): Flow<Boolean> {
        return useSweepingSecondsMotionCache.watchChanges()
    }

    override fun showWeather(): Boolean = showWeatherCache.get()

    override fun setShowWeather(show: Boolean) = showWeatherCache.set(show)

    override fun watchShowWeather(): Flow<Boolean> = showWeatherCache.watchChanges()

    override fun showWatchBattery(): Boolean = showWatchBattery.get()

    override fun setShowWatchBattery(show: Boolean) = showWatchBattery.set(show)

    override fun watchShowWatchBattery(): Flow<Boolean> = showWatchBattery.watchChanges()

    override fun showPhoneBattery(): Boolean = showPhoneBatteryCache.get()

    override fun setShowPhoneBattery(show: Boolean) = showPhoneBatteryCache.set(show)

    override fun watchShowPhoneBattery(): Flow<Boolean> = showPhoneBatteryCache.watchChanges()

    override fun getTimeColor(): Int = timeColorCache.get().color

    override fun getTimeColorFilter(): ColorFilter = timeColorCache.get().colorFilter

    override fun setTimeColor(color: Int) = timeColorCache.set(color)

    override fun getDateColor(): Int = dateColorCache.get().color

    override fun getDateColorFilter(): ColorFilter = dateColorCache.get().colorFilter

    override fun setDateColor(color: Int) = dateColorCache.set(color)

    override fun getBatteryIndicatorColor(): Int = batteryIndicatorColorCache.get().color

    override fun getBatteryIndicatorColorFilter(): ColorFilter = batteryIndicatorColorCache.get().colorFilter

    override fun setBatteryIndicatorColor(color: Int) = batteryIndicatorColorCache.set(color)

    override fun useAndroid12Style(): Boolean = useAndroid12StyleCache.get()

    override fun setUseAndroid12Style(useAndroid12Style: Boolean) = useAndroid12StyleCache.set(useAndroid12Style)

    override fun watchUseAndroid12Style(): Flow<Boolean> = useAndroid12StyleCache.watchChanges()

    override fun hideBatteryInAmbient(): Boolean = hideBatteryInAmbientCache.get()

    override fun setHideBatteryInAmbient(hide: Boolean) = hideBatteryInAmbientCache.set(hide)

    override fun watchHideBatteryInAmbient(): Flow<Boolean> = hideBatteryInAmbientCache.watchChanges()

    override fun getSecondRingColor(): ColorFilter = secondRingColorCache.get().colorFilter

    override fun setSecondRingColor(@ColorInt color: Int) = secondRingColorCache.set(color)

    override fun getWidgetsSize(): Int = widgetsSizeCache.get()

    override fun setWidgetsSize(widgetsSize: Int) = widgetsSizeCache.set(widgetsSize)

    override fun watchWidgetsSize(): Flow<Int> = widgetsSizeCache.watchChanges()

    override fun isNotificationsSyncActivated(): Boolean = notificationsSyncEnabledCache.get()

    override fun setNotificationsSyncActivated(activated: Boolean) = notificationsSyncEnabledCache.set(activated)

    override fun watchIsNotificationsSyncActivated(): Flow<Boolean> = notificationsSyncEnabledCache.watchChanges()

    override fun setNotificationIconsColor(@ColorInt color: Int) = notificationIconsColorCache.set(color)

    @ColorInt
    override fun getNotificationIconsColor(): Int = notificationIconsColorCache.get().color

    override fun getNotificationIconsColorFilter(): ColorFilter = notificationIconsColorCache.get().colorFilter

    override fun getShowNotificationsInAmbient(): Boolean = showNotificationsInAmbientCache.get()

    override fun setShowNotificationsInAmbient(show: Boolean) = showNotificationsInAmbientCache.set(show)

    override fun watchShowNotificationsInAmbient(): Flow<Boolean> = showNotificationsInAmbientCache.watchChanges()

    override fun getShowWearOSLogoInAmbient(): Boolean = showWearOSLogoInAmbientCache.get()

    override fun setShowWearOSLogoInAmbient(show: Boolean) = showWearOSLogoInAmbientCache.set(show)

    override fun watchShowWearOSLogoInAmbient(): Flow<Boolean> = showWearOSLogoInAmbientCache.watchChanges()

    override fun hasBetaNotificationsDisclaimerBeenShown(): Boolean = betaNotificationsDisclaimerShownCache.get()

    override fun setBetaNotificationsDisclaimerShown() = betaNotificationsDisclaimerShownCache.set(true)

    override fun hasFeatureDropAutumn2022NotificationBeenShown(): Boolean {
        return sharedPreferences.getBoolean(KEY_FEATURE_DROP_2022_NOTIFICATION, false)
    }

    override fun setFeatureDropAutumn2022NotificationShown() {
        sharedPreferences.edit().putBoolean(KEY_FEATURE_DROP_2022_NOTIFICATION, true).apply()
    }

    override fun getUseShortDateFormat(): Boolean = useShortDateFormatCache.get()

    override fun setUseShortDateFormat(useShortDateFormat: Boolean) = useShortDateFormatCache.set(useShortDateFormat)

    override fun watchUseShortDateFormat(): Flow<Boolean> = useShortDateFormatCache.watchChanges()

    override fun setShowDateInAmbient(showDateInAmbient: Boolean) = showDateInAmbientCache.set(showDateInAmbient)

    override fun getShowDateInAmbient(): Boolean = showDateInAmbientCache.get()

    override fun watchShowDateInAmbient(): Flow<Boolean> = showDateInAmbientCache.watchChanges()
}
