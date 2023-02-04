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
package com.benoitletondor.pixelminimalwatchface.common.settings.model

import androidx.activity.ComponentActivity
import androidx.annotation.ColorInt
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow

interface Platform {
    val isScreenRound: Boolean
    val isWearOS3: Boolean
    val appVersionName: String
    val weatherProvider: WeatherProvider

    suspend fun requestComplicationsPermission(activity: ComponentActivity): Boolean

    fun startPhoneBatterySyncConfigScreen(navController: NavController, activity: ComponentActivity)
    fun startPhoneNotificationIconsConfigScreen(navController: NavController, activity: ComponentActivity)
    fun startFeedbackScreen(navController: NavController, activity: ComponentActivity)
    fun startDonationScreen(navController: NavController, activity: ComponentActivity)
    suspend fun startColorSelectionScreen(navController: NavController, @ColorInt defaultColor: Int): ComplicationColor?
    fun startWidgetConfigurationScreen(navController: NavController, complicationLocation: ComplicationLocation)

    fun isUserPremium(): Boolean
    fun watchIsUserPremium(): Flow<Boolean>

    fun showWearOSLogo(): Boolean
    suspend fun setShowWearOSLogo(shouldShowWearOSLogo: Boolean)
    fun watchShowWearOSLogo(): Flow<Boolean>
    suspend fun setUse24hTimeFormat(use: Boolean)
    fun getUse24hTimeFormat(): Boolean
    fun watchUse24hTimeFormat(): Flow<Boolean>
    fun setRatingDisplayed(displayed: Boolean)
    fun showComplicationsInAmbientMode(): Boolean
    suspend fun setShowComplicationsInAmbientMode(show: Boolean)
    fun watchShowComplicationsInAmbientMode(): Flow<Boolean>
    fun showColorsInAmbientMode(): Boolean
    suspend fun setShowColorsInAmbientMode(show: Boolean)
    fun watchShowColorsInAmbientMode(): Flow<Boolean>
    fun useNormalTimeStyleInAmbientMode(): Boolean
    suspend fun setUseNormalTimeStyleInAmbientMode(useNormalTime: Boolean)
    fun watchUseNormalTimeStyleInAmbientMode(): Flow<Boolean>
    fun useThinTimeStyleInRegularMode(): Boolean
    suspend fun setUseThinTimeStyleInRegularMode(useThinTime: Boolean)
    fun watchUseThinTimeStyleInRegularMode(): Flow<Boolean>
    fun getTimeSize(): Int
    suspend fun setTimeSize(timeSize: Int)
    fun watchTimeSize(): Flow<Int>
    fun getDateAndBatterySize(): Int
    suspend fun setDateAndBatterySize(size: Int)
    fun watchDateAndBatterySize(): Flow<Int>
    fun showSecondsRing(): Boolean
    suspend fun setShowSecondsRing(showSecondsRing: Boolean)
    fun watchShowSecondsRing(): Flow<Boolean>
    fun useSweepingSecondsRingMotion(): Boolean
    suspend fun setUseSweepingSecondsRingMotion(useSweepingSecondsRingMotion: Boolean)
    fun watchUseSweepingSecondsRingMotion(): Flow<Boolean>
    fun showWeather(): Boolean
    suspend fun setShowWeather(show: Boolean)
    fun watchShowWeather(): Flow<Boolean>
    fun showWatchBattery(): Boolean
    suspend fun setShowWatchBattery(show: Boolean)
    fun watchShowWatchBattery(): Flow<Boolean>
    fun getUseShortDateFormat(): Boolean
    suspend fun setUseShortDateFormat(useShortDateFormat: Boolean)
    fun watchUseShortDateFormat(): Flow<Boolean>
    suspend fun setShowDateInAmbient(showDateInAmbient: Boolean)
    fun getShowDateInAmbient(): Boolean
    fun watchShowDateInAmbient(): Flow<Boolean>
    fun showPhoneBattery(): Boolean
    suspend fun setShowPhoneBattery(show: Boolean)
    fun watchShowPhoneBattery(): Flow<Boolean>
    suspend fun setTimeColor(@ColorInt color: Int)
    suspend fun setDateColor(@ColorInt color: Int)
    suspend fun setBatteryIndicatorColor(@ColorInt color: Int)
    fun useAndroid12Style(): Boolean
    suspend fun setUseAndroid12Style(useAndroid12Style: Boolean)
    fun watchUseAndroid12Style(): Flow<Boolean>
    fun hideBatteryInAmbient(): Boolean
    suspend fun setHideBatteryInAmbient(hide: Boolean)
    fun watchHideBatteryInAmbient(): Flow<Boolean>
    suspend fun setSecondRingColor(@ColorInt color: Int)
    fun isNotificationsSyncActivated(): Boolean
    suspend fun setNotificationsSyncActivated(activated: Boolean)
    fun watchIsNotificationsSyncActivated(): Flow<Boolean>
    suspend fun setNotificationIconsColor(@ColorInt color: Int)
    fun getShowNotificationsInAmbient(): Boolean
    suspend fun setShowNotificationsInAmbient(show: Boolean)
    fun watchShowNotificationsInAmbient(): Flow<Boolean>
    fun getShowWearOSLogoInAmbient(): Boolean
    suspend fun setShowWearOSLogoInAmbient(show: Boolean)
    fun watchShowWearOSLogoInAmbient(): Flow<Boolean>
    fun getWidgetsSize(): Int
    suspend fun setWidgetsSize(widgetsSize: Int)
    fun watchWidgetsSize(): Flow<Int>
    fun getComplicationColors(): ComplicationColors
    suspend fun setComplicationColors(complicationColors: ComplicationColors)
    fun watchComplicationColors(): Flow<ComplicationColors>
}