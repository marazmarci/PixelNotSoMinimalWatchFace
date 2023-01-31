package com.benoitletondor.pixelminimalwatchfacecompanion.platform

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_BATTERY_COLOR
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_DATE_AND_BATTERY_SIZE
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_DATE_COLOR
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_HIDE_BATTERY_IN_AMBIENT
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_NOTIFICATIONS_SYNC_ENABLED
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_NOTIFICATION_ICONS_COLOR
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_SECONDS_RING
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_SECONDS_RING_COLOR
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_SHOW_COLORS_AMBIENT
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_SHOW_COMPLICATIONS_AMBIENT
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_SHOW_DATE_AMBIENT
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_SHOW_NOTIFICATIONS_AMBIENT
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_SHOW_PHONE_BATTERY
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_SHOW_WATCH_BATTERY
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_SHOW_WEAR_OS_LOGO
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_SHOW_WEAR_OS_LOGO_AMBIENT
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_SHOW_WEATHER
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_TIME_COLOR
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_TIME_SIZE
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_USE_24H_TIME_FORMAT
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_USE_ANDROID_12_STYLE
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_USE_NORMAL_TIME_STYLE_IN_AMBIENT
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_USE_SHORT_DATE_FORMAT
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_USE_SWEEPING_SECONDS_RING_MOTION
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_USE_THIN_TIME_STYLE_IN_REGULAR
import com.benoitletondor.pixelminimalwatchface.common.helper.KEY_WIDGETS_SIZE
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColors
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.common.settings.model.InitialState
import com.benoitletondor.pixelminimalwatchface.common.settings.model.Platform
import com.benoitletondor.pixelminimalwatchface.common.settings.model.WeatherProvider
import com.benoitletondor.pixelminimalwatchface.common.settings.navigateToColorSelectionScreen
import com.benoitletondor.pixelminimalwatchface.common.settings.navigateToWidgetSelectionScreen
import com.benoitletondor.pixelminimalwatchfacecompanion.BatteryStatusBroadcastReceiver
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.PremiumCheckStatus
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.RatingPopup
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import com.benoitletondor.pixelminimalwatchfacecompanion.view.NAV_DONATION_ROUTE
import com.benoitletondor.pixelminimalwatchfacecompanion.view.NAV_PHONE_BATTERY_SYNC_SETTINGS_ROUTE
import com.benoitletondor.pixelminimalwatchfacecompanion.view.NAV_PHONE_NOTIFICATIONS_SYNC_SETTINGS_ROUTE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class PhonePlatform(
    private val appContext: Context,
    private val billing: Billing,
    private val syncSession: SyncSession,
    private val initialState: InitialState,
    private val storage: Storage,
) : Platform {
    override val isScreenRound: Boolean = initialState.isWatchScreenRound
    override val isWearOS3: Boolean = initialState.isWatchWearOS3
    override val appVersionName: String = BuildConfig.VERSION_NAME
    override val weatherProvider: WeatherProvider = WeatherProvider(
        hasWeatherSupport = initialState.watchSupportsWeather,
        weatherProviderInfo = null,
    )

    private var hasComplicationPermission = initialState.hasComplicationsPermission

    suspend fun editComplication(complicationLocation: ComplicationLocation) {
        syncSession.editComplication(complicationLocation)
    }

    override suspend fun requestComplicationsPermission(activity: ComponentActivity): Boolean {
        if (hasComplicationPermission) {
            return true
        }

        Toast.makeText(activity, "This feature needs the Complications permission on your watch.\n\nContinue on your watch to grant permission.", Toast.LENGTH_LONG).show()

        return try {
            val result = syncSession.requestComplicationsPermission()
            if (!result) {
                Toast.makeText(activity, "Permission to get complications data denied, can't activate this feature.", Toast.LENGTH_LONG).show()
            } else {
                hasComplicationPermission = true
            }

            result
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error while requesting complications permission")

            Toast.makeText(activity, "No response from the watch permission request, please try again.", Toast.LENGTH_LONG).show()

            false
        }
    }

    override fun startPhoneBatterySyncConfigScreen(navController: NavController, activity: ComponentActivity) {
        navController.navigate(NAV_PHONE_BATTERY_SYNC_SETTINGS_ROUTE)
    }

    override fun startPhoneNotificationIconsConfigScreen(navController: NavController, activity: ComponentActivity) {
        navController.navigate(NAV_PHONE_NOTIFICATIONS_SYNC_SETTINGS_ROUTE)
    }

    override fun startFeedbackScreen(navController: NavController, activity: ComponentActivity) {
        RatingPopup(activity).show()
    }

    override fun startDonationScreen(navController: NavController, activity: ComponentActivity) {
        navController.navigate(NAV_DONATION_ROUTE)
    }

    override suspend fun startColorSelectionScreen(
        navController: NavController,
        defaultColor: Int
    ): ComplicationColor? {
        return navController.navigateToColorSelectionScreen(defaultColor)
    }

    override fun startWidgetConfigurationScreen(
        navController: NavController,
        complicationLocation: ComplicationLocation,
    ) {
        navController.navigateToWidgetSelectionScreen(complicationLocation)
    }

    override fun isUserPremium(): Boolean = billing.isUserPremium()

    override fun watchIsUserPremium(): Flow<Boolean> = billing.userPremiumEventStream
        .filter { it is PremiumCheckStatus.Premium || it is PremiumCheckStatus.NotPremium || it is PremiumCheckStatus.Error }
        .map { it is PremiumCheckStatus.Premium }

    private val showWearOSLogoCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_SHOW_WEAR_OS_LOGO)
    override fun showWearOSLogo(): Boolean = showWearOSLogoCache.get()
    override suspend fun setShowWearOSLogo(shouldShowWearOSLogo: Boolean) = showWearOSLogoCache.set(shouldShowWearOSLogo)
    override fun watchShowWearOSLogo(): Flow<Boolean> = showWearOSLogoCache.watchChanges()

    private val use24hTimeFormat = StorageCachedBoolValue(syncSession, initialState.settings, KEY_USE_24H_TIME_FORMAT)
    override suspend fun setUse24hTimeFormat(use: Boolean) = use24hTimeFormat.set(use)
    override fun getUse24hTimeFormat(): Boolean = use24hTimeFormat.get()
    override fun watchUse24hTimeFormat(): Flow<Boolean> = use24hTimeFormat.watchChanges()

    override fun setRatingDisplayed(displayed: Boolean) = Unit

    private val showComplicationsInAmbientCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_SHOW_COMPLICATIONS_AMBIENT)
    override fun showComplicationsInAmbientMode(): Boolean = showComplicationsInAmbientCache.get()
    override suspend fun setShowComplicationsInAmbientMode(show: Boolean) = showComplicationsInAmbientCache.set(show)
    override fun watchShowComplicationsInAmbientMode(): Flow<Boolean> = showComplicationsInAmbientCache.watchChanges()

    private val showColorsInAmbientCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_SHOW_COLORS_AMBIENT)
    override fun showColorsInAmbientMode(): Boolean = showColorsInAmbientCache.get()
    override suspend fun setShowColorsInAmbientMode(show: Boolean) = showColorsInAmbientCache.set(show)
    override fun watchShowColorsInAmbientMode(): Flow<Boolean> = showColorsInAmbientCache.watchChanges()

    private val useNormalTimeStyleInAmbientCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_USE_NORMAL_TIME_STYLE_IN_AMBIENT)
    override fun useNormalTimeStyleInAmbientMode(): Boolean = useNormalTimeStyleInAmbientCache.get()
    override suspend fun setUseNormalTimeStyleInAmbientMode(useNormalTime: Boolean) = useNormalTimeStyleInAmbientCache.set(useNormalTime)
    override fun watchUseNormalTimeStyleInAmbientMode(): Flow<Boolean> = useNormalTimeStyleInAmbientCache.watchChanges()

    private val useThinTimeStyleInRegularCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_USE_THIN_TIME_STYLE_IN_REGULAR)
    override fun useThinTimeStyleInRegularMode(): Boolean = useThinTimeStyleInRegularCache.get()
    override suspend fun setUseThinTimeStyleInRegularMode(useThinTime: Boolean)= useThinTimeStyleInRegularCache.set(useThinTime)
    override fun watchUseThinTimeStyleInRegularMode(): Flow<Boolean> = useThinTimeStyleInRegularCache.watchChanges()

    private val timeSizeCache = StorageCachedIntValue(syncSession, initialState.settings, KEY_TIME_SIZE)
    override fun getTimeSize(): Int = timeSizeCache.get()
    override suspend fun setTimeSize(timeSize: Int) = timeSizeCache.set(timeSize)
    override fun watchTimeSize(): Flow<Int> = timeSizeCache.watchChanges()

    private val dateAndBatterySizeCache = StorageCachedIntValue(syncSession, initialState.settings, KEY_DATE_AND_BATTERY_SIZE)
    override fun getDateAndBatterySize(): Int = dateAndBatterySizeCache.get()
    override suspend fun setDateAndBatterySize(size: Int) = dateAndBatterySizeCache.set(size)
    override fun watchDateAndBatterySize(): Flow<Int> = dateAndBatterySizeCache.watchChanges()

    private val showSecondsRingCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_SECONDS_RING)
    override fun showSecondsRing(): Boolean = showSecondsRingCache.get()
    override suspend fun setShowSecondsRing(showSecondsRing: Boolean) = showSecondsRingCache.set(showSecondsRing)
    override fun watchShowSecondsRing(): Flow<Boolean> = showSecondsRingCache.watchChanges()

    private val useSweepingSecondsRingMotionCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_USE_SWEEPING_SECONDS_RING_MOTION)
    override fun useSweepingSecondsRingMotion(): Boolean = useSweepingSecondsRingMotionCache.get()
    override suspend fun setUseSweepingSecondsRingMotion(useSweepingSecondsRingMotion: Boolean) = useSweepingSecondsRingMotionCache.set(useSweepingSecondsRingMotion)
    override fun watchUseSweepingSecondsRingMotion(): Flow<Boolean> = useSweepingSecondsRingMotionCache.watchChanges()

    private val showWeatherCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_SHOW_WEATHER)
    override fun showWeather(): Boolean = showWeatherCache.get()
    override suspend fun setShowWeather(show: Boolean) = showWeatherCache.set(show)
    override fun watchShowWeather(): Flow<Boolean> = showWeatherCache.watchChanges()

    private val showWatchBatteryCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_SHOW_WATCH_BATTERY)
    override fun showWatchBattery(): Boolean = showWatchBatteryCache.get()
    override suspend fun setShowWatchBattery(show: Boolean) = showWatchBatteryCache.set(show)
    override fun watchShowWatchBattery(): Flow<Boolean> = showWatchBatteryCache.watchChanges()

    private val useShortDateFormatCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_USE_SHORT_DATE_FORMAT)
    override fun getUseShortDateFormat(): Boolean = useShortDateFormatCache.get()
    override suspend fun setUseShortDateFormat(useShortDateFormat: Boolean) = useShortDateFormatCache.set(useShortDateFormat)
    override fun watchUseShortDateFormat(): Flow<Boolean> = useShortDateFormatCache.watchChanges()

    private val showDateInAmbientCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_SHOW_DATE_AMBIENT)
    override suspend fun setShowDateInAmbient(showDateInAmbient: Boolean) = showDateInAmbientCache.set(showDateInAmbient)
    override fun getShowDateInAmbient(): Boolean = showDateInAmbientCache.get()
    override fun watchShowDateInAmbient(): Flow<Boolean> = showDateInAmbientCache.watchChanges()

    private val showPhoneBatteryCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_SHOW_PHONE_BATTERY)
    override fun showPhoneBattery(): Boolean = showPhoneBatteryCache.get()
    override suspend fun setShowPhoneBattery(show: Boolean) {
        storage.setBatterySyncActivated(show)
        if (show) {
            BatteryStatusBroadcastReceiver.subscribeToUpdates(appContext)
        } else {
            BatteryStatusBroadcastReceiver.unsubscribeFromUpdates(appContext)
        }
        showPhoneBatteryCache.set(show)
    }
    override fun watchShowPhoneBattery(): Flow<Boolean> = showPhoneBatteryCache.watchChanges()

    override suspend fun setTimeColor(color: Int) = syncSession.setParameter(KEY_TIME_COLOR, color)

    override suspend fun setDateColor(color: Int) = syncSession.setParameter(KEY_DATE_COLOR, color)

    override suspend fun setBatteryIndicatorColor(color: Int) = syncSession.setParameter(KEY_BATTERY_COLOR, color)

    private val useAndroid12StyleCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_USE_ANDROID_12_STYLE)
    override fun useAndroid12Style(): Boolean = useAndroid12StyleCache.get()
    override suspend fun setUseAndroid12Style(useAndroid12Style: Boolean) = useAndroid12StyleCache.set(useAndroid12Style)
    override fun watchUseAndroid12Style(): Flow<Boolean> = useAndroid12StyleCache.watchChanges()

    private val hideBatteryInAmbientCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_HIDE_BATTERY_IN_AMBIENT)
    override fun hideBatteryInAmbient(): Boolean = hideBatteryInAmbientCache.get()
    override suspend fun setHideBatteryInAmbient(hide: Boolean) = hideBatteryInAmbientCache.set(hide)
    override fun watchHideBatteryInAmbient(): Flow<Boolean> = hideBatteryInAmbientCache.watchChanges()

    override suspend fun setSecondRingColor(color: Int) = syncSession.setParameter(KEY_SECONDS_RING_COLOR, color)

    private val isNotificationsSyncEnabledCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_NOTIFICATIONS_SYNC_ENABLED)
    override fun isNotificationsSyncActivated(): Boolean = isNotificationsSyncEnabledCache.get()
    override suspend fun setNotificationsSyncActivated(activated: Boolean) {
        storage.setNotificationsSyncActivated(activated)
        isNotificationsSyncEnabledCache.set(activated)
    }
    override fun watchIsNotificationsSyncActivated(): Flow<Boolean> = isNotificationsSyncEnabledCache.watchChanges()

    override suspend fun setNotificationIconsColor(color: Int) = syncSession.setParameter(KEY_NOTIFICATION_ICONS_COLOR, color)

    private val showNotificationsInAmbientCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_SHOW_NOTIFICATIONS_AMBIENT)
    override fun getShowNotificationsInAmbient(): Boolean = showNotificationsInAmbientCache.get()
    override suspend fun setShowNotificationsInAmbient(show: Boolean) = showNotificationsInAmbientCache.set(show)
    override fun watchShowNotificationsInAmbient(): Flow<Boolean> = showNotificationsInAmbientCache.watchChanges()

    private val showWearOSLogoInAmbientCache = StorageCachedBoolValue(syncSession, initialState.settings, KEY_SHOW_WEAR_OS_LOGO_AMBIENT)
    override fun getShowWearOSLogoInAmbient(): Boolean = showWearOSLogoInAmbientCache.get()
    override suspend fun setShowWearOSLogoInAmbient(show: Boolean) = showWearOSLogoInAmbientCache.set(show)
    override fun watchShowWearOSLogoInAmbient(): Flow<Boolean> = showWearOSLogoInAmbientCache.watchChanges()

    private val widgetSizeCache = StorageCachedIntValue(syncSession, initialState.settings, KEY_WIDGETS_SIZE)
    override fun getWidgetsSize(): Int = widgetSizeCache.get()
    override suspend fun setWidgetsSize(widgetsSize: Int) = widgetSizeCache.set(widgetsSize)
    override fun watchWidgetsSize(): Flow<Int> = widgetSizeCache.watchChanges()

    private val complicationColorsCache = object : StorageCachedValue<ComplicationColors>(initialState.complicationColors, setter = { colors ->
        syncSession.setComplicationColors(colors)
    }) {}
    override fun getComplicationColors(): ComplicationColors = complicationColorsCache.get()
    override suspend fun setComplicationColors(complicationColors: ComplicationColors) = complicationColorsCache.set(complicationColors)
    override fun watchComplicationColors(): Flow<ComplicationColors> = complicationColorsCache.watchChanges()

    companion object {
        private const val TAG = "PhonePlatform"
    }
}