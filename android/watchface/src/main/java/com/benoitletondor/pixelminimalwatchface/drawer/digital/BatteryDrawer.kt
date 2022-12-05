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
package com.benoitletondor.pixelminimalwatchface.drawer.digital

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.wearable.complications.ComplicationData
import android.util.Log
import androidx.core.content.ContextCompat
import com.benoitletondor.pixelminimalwatchface.helper.toBitmap
import com.benoitletondor.pixelminimalwatchface.PhoneBatteryStatus
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.getBatteryText
import java.util.*

interface BatteryDrawer {
    fun drawBattery(
        canvas: Canvas,
        batteryLevelPaint: Paint,
        batteryIconPaint: Paint,
        distanceBetweenPhoneAndWatchBattery: Int,
        drawBattery: Boolean,
        drawPhoneBattery: Boolean,
        calendar: Calendar,
        batteryComplicationData: ComplicationData?,
        phoneBatteryStatus: PhoneBatteryStatus?,
    )

    fun tapIsOnBattery(x: Int, y: Int): Boolean
}

class BatteryDrawerImpl(
    private val context: Context,
    private val centerX: Float,
    private val screenWidth: Int,
    private val batteryIconSize: Int,
    private val batteryLevelBottomY: Int,
    private val batteryIconBottomY: Int,
) : BatteryDrawer {
    private val battery100Icon: Bitmap = ContextCompat.getDrawable(context, R.drawable.battery_100)!!.toBitmap(batteryIconSize, batteryIconSize)
    private val battery80Icon: Bitmap = ContextCompat.getDrawable(context, R.drawable.battery_80)!!.toBitmap(batteryIconSize, batteryIconSize)
    private val battery60Icon: Bitmap = ContextCompat.getDrawable(context, R.drawable.battery_60)!!.toBitmap(batteryIconSize, batteryIconSize)
    private val battery50Icon: Bitmap = ContextCompat.getDrawable(context, R.drawable.battery_50)!!.toBitmap(batteryIconSize, batteryIconSize)
    private val battery40Icon: Bitmap = ContextCompat.getDrawable(context, R.drawable.battery_40)!!.toBitmap(batteryIconSize, batteryIconSize)
    private val battery20Icon: Bitmap = ContextCompat.getDrawable(context, R.drawable.battery_20)!!.toBitmap(batteryIconSize, batteryIconSize)
    private val battery10Icon: Bitmap = ContextCompat.getDrawable(context, R.drawable.battery_10)!!.toBitmap(batteryIconSize, batteryIconSize)
    private val watchBatteryIcon: Bitmap = ContextCompat.getDrawable(context, R.drawable.ic_watch)!!.toBitmap(batteryIconSize, batteryIconSize)
    private val phoneBatteryIcon: Bitmap = ContextCompat.getDrawable(context, R.drawable.ic_phone)!!.toBitmap(batteryIconSize, batteryIconSize)

    override fun drawBattery(
        canvas: Canvas,
        batteryLevelPaint: Paint,
        batteryIconPaint: Paint,
        distanceBetweenPhoneAndWatchBattery: Int,
        drawBattery: Boolean,
        drawPhoneBattery: Boolean,
        calendar: Calendar,
        batteryComplicationData: ComplicationData?,
        phoneBatteryStatus: PhoneBatteryStatus?,
    ) {
        val batteryText = if (drawBattery) {
            batteryComplicationData?.shortText?.getText(context, calendar.timeInMillis)
                ?.toString()?.filter { it.isDigit() }?.plus("%")
        } else {
            null
        }
        val phoneBatteryText = if (drawPhoneBattery) {
            phoneBatteryStatus?.getBatteryText(calendar.timeInMillis)
        } else {
            null
        }

        if (phoneBatteryText != null || batteryText != null) {
            val batteryTextLength = if (batteryText != null) {
                batteryLevelPaint.measureText(batteryText)
            } else {
                0f
            }
            val phoneBatteryTextLength = if (phoneBatteryText != null) {
                batteryLevelPaint.measureText(phoneBatteryText)
            } else {
                0f
            }

            var numberOfIcons = 0
            if (phoneBatteryText != null) {
                numberOfIcons++
            }
            if (batteryText != null) {
                numberOfIcons++
            }

            var left = (centerX - ((batteryTextLength + phoneBatteryTextLength) / 2) - ((numberOfIcons * batteryIconSize) / 2))
            if (numberOfIcons == 2) {
                left -= (distanceBetweenPhoneAndWatchBattery.toFloat() / 2f)
            }

            if (batteryText != null) {
                val icon = if (phoneBatteryText == null ) { getBatteryIcon(batteryText) } else { watchBatteryIcon }

                canvas.drawBitmap(
                    icon,
                    null,
                    Rect(
                        left.toInt(),
                        batteryIconBottomY - batteryIconSize,
                        left.toInt() + batteryIconSize,
                        batteryIconBottomY
                    ),
                    batteryIconPaint
                )

                left+=batteryIconSize

                canvas.drawText(
                    batteryText,
                    left,
                    (batteryLevelBottomY).toFloat(),
                    batteryLevelPaint
                )

                left+=batteryTextLength
            }

            if (phoneBatteryText != null) {
                if (batteryText != null) {
                    left += distanceBetweenPhoneAndWatchBattery
                }

                canvas.drawBitmap(
                    phoneBatteryIcon,
                    null,
                    Rect(
                        left.toInt(),
                        batteryIconBottomY - batteryIconSize,
                        left.toInt() + batteryIconSize,
                        batteryIconBottomY
                    ),
                    batteryIconPaint
                )

                left+=batteryIconSize

                canvas.drawText(
                    phoneBatteryText,
                    left,
                    (batteryLevelBottomY).toFloat(),
                    batteryLevelPaint
                )
            }
        }
    }

    override fun tapIsOnBattery(x: Int, y: Int): Boolean {
        val batteryIndicatorRect = Rect(
            (screenWidth * 0.25f).toInt(),
            (batteryIconBottomY - batteryIconSize),
            (screenWidth * 0.75f).toInt(),
            (batteryIconBottomY)
        )

        return batteryIndicatorRect.contains(x, y)
    }

    private fun getBatteryIcon(batteryText: String): Bitmap {
        val batteryPercent = try {
            Integer.parseInt(batteryText.replace("%", ""))
        } catch (t: Throwable) {
            Log.e("WatchFaceDrawer", "Error parsing battery data", t)
            50
        }

        return when {
            batteryPercent <= 10 -> { battery10Icon }
            batteryPercent <= 25 -> { battery20Icon }
            batteryPercent <= 40 -> { battery40Icon }
            batteryPercent <= 50 -> { battery50Icon }
            batteryPercent <= 70 -> { battery60Icon }
            batteryPercent <= 90 -> { battery80Icon }
            else -> { battery100Icon }
        }
    }
}