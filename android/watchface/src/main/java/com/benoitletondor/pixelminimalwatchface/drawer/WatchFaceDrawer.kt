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
package com.benoitletondor.pixelminimalwatchface.drawer

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.wearable.complications.ComplicationData
import android.util.SparseArray
import android.view.WindowInsets
import com.benoitletondor.pixelminimalwatchface.PhoneBatteryStatus
import com.benoitletondor.pixelminimalwatchface.PhoneNotifications
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColors
import java.util.*

interface WatchFaceDrawer {
    fun initializeComplicationDrawables(drawableCallback: Drawable.Callback): IntArray
    fun onApplyWindowInsets(insets: WindowInsets)
    fun onSurfaceChanged(width: Int, height: Int)
    fun onComplicationColorsUpdate(
        complicationColors: ComplicationColors,
        complicationsData: SparseArray<ComplicationData>,
        showComplicationsColorsInAmbient: Boolean,
    )
    fun onComplicationDataUpdate(
        complicationId: Int,
        data: ComplicationData?,
        complicationColors: ComplicationColors,
        showComplicationsColorsInAmbient: Boolean,
    )
    fun tapIsOnComplication(x: Int, y: Int): Boolean
    fun tapIsOnWeather(x: Int, y: Int): Boolean
    fun tapIsInCenterOfScreen(x: Int, y: Int): Boolean
    fun tapIsOnBattery(x: Int, y: Int): Boolean
    fun isTapOnNotifications(x: Int, y: Int): Boolean

    fun draw(
        canvas: Canvas,
        calendar: Calendar,
        muteMode: Boolean,
        ambient:Boolean,
        lowBitAmbient: Boolean,
        burnInProtection: Boolean,
        weatherComplicationData: ComplicationData?,
        batteryComplicationData: ComplicationData?,
        phoneBatteryStatus: PhoneBatteryStatus?,
        notificationsState: PhoneNotifications.NotificationState?,
    )
}
