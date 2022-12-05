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
package com.benoitletondor.pixelminimalwatchface.drawer.digital.regular

import android.content.Context
import android.graphics.Rect
import com.benoitletondor.pixelminimalwatchface.drawer.digital.*

sealed class RegularDrawerDrawingState {
    object NoScreenData : RegularDrawerDrawingState()
    data class NoCacheAvailable(
        val screenWidth: Int,
        val screenHeight: Int,
        val centerX: Float,
        val centerY: Float,
    ) : RegularDrawerDrawingState()
    data class CacheAvailable(
        private val context: Context,
        private val batteryIconSize: Int,
        private val batteryLevelBottomY: Int,
        private val batteryIconBottomY: Int,
        private val dateHeight: Int,
        private val dateYOffset: Float,
        val screenWidth: Int,
        val screenHeight: Int,
        val centerX: Float,
        val centerY: Float,
        val timeYOffset: Float,
        val complicationsDrawingCache: ComplicationsDrawingCache,
    ) : RegularDrawerDrawingState(),
        BatteryDrawer by BatteryDrawerImpl(context, centerX, screenWidth, batteryIconSize, batteryLevelBottomY, batteryIconBottomY),
        SecondsRingDrawer by SecondRingDrawerImpl(screenWidth, screenHeight),
        DateAndWeatherDrawer by DateAndWeatherDrawerImpl(context, dateHeight, dateYOffset, centerX),
        NotificationsDrawer by NotificationsDrawerImpl(context, centerX, complicationsDrawingCache.notificationsRect)
}

data class ComplicationsDrawingCache(
    val iconXOffset: Float,
    val iconYOffset: Float,
    val notificationsRect: Rect,
)
