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
package com.benoitletondor.pixelminimalwatchface.drawer.digital.android12

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationText
import android.text.format.DateUtils
import com.benoitletondor.pixelminimalwatchface.common.helper.dpToPx
import com.benoitletondor.pixelminimalwatchface.helper.toBitmap
import com.benoitletondor.pixelminimalwatchface.helper.capitalize
import com.benoitletondor.pixelminimalwatchface.helper.sameAs
import java.util.*

interface DateAndWeatherDrawer {
    fun drawDateAndWeather(
        canvas: Canvas,
        weatherComplicationData: ComplicationData?,
        useShortDateFormat: Boolean,
        isUserPremium: Boolean,
        calendar: Calendar,
        datePaint: Paint,
        weatherIconPaint: Paint,
    )

    fun getWeatherDisplayRect(): Rect?
}

class DateAndWeatherDrawerImpl(
    private val context: Context,
    private val dateHeight: Int,
    private val dateYOffset: Float,
    private val centerX: Float,
) : DateAndWeatherDrawer {
    private var currentWeatherIcon: Icon? = null
    private var currentWeatherBitmap: Bitmap? = null
    private var weatherTextEndX: Float? = null

    private val weatherIconRect = Rect()

    override fun getWeatherDisplayRect(): Rect? {
        val currentWeatherIcon = currentWeatherIcon
        val currentWeatherBitmap = currentWeatherBitmap
        val weatherTextEndX = weatherTextEndX
        if( currentWeatherIcon == null || currentWeatherBitmap == null || weatherTextEndX == null ) {
            return null
        }

        return Rect(weatherIconRect).apply {
            right = weatherTextEndX.toInt()
        }
    }

    override fun drawDateAndWeather(
        canvas: Canvas,
        weatherComplicationData: ComplicationData?,
        useShortDateFormat: Boolean,
        isUserPremium: Boolean,
        calendar: Calendar,
        datePaint: Paint,
        weatherIconPaint: Paint,
    ) {
        val dateFormat = if( useShortDateFormat ) {
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_WEEKDAY or DateUtils.FORMAT_ABBREV_MONTH
        } else {
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_WEEKDAY
        }

        val dateText = DateUtils.formatDateTime(context, calendar.timeInMillis, dateFormat).capitalize()
        val dateTextLength = datePaint.measureText(dateText)
        if( isUserPremium && weatherComplicationData != null ) {
            val weatherText = weatherComplicationData.shortText
            val weatherIcon = weatherComplicationData.icon

            if( weatherText != null && weatherIcon != null ) {
                drawWeather(weatherText, weatherIcon, calendar, canvas, datePaint, weatherIconPaint)
            } else {
                currentWeatherBitmap = null
                currentWeatherIcon = null
                weatherTextEndX = null
            }
        } else {
            currentWeatherBitmap = null
            currentWeatherIcon = null
            weatherTextEndX = null
        }

        canvas.drawText(dateText, centerX - (dateTextLength / 2f), dateYOffset, datePaint)
    }

    private fun drawWeather(
        weatherText: ComplicationText,
        weatherIcon: Icon,
        calendar: Calendar,
        canvas: Canvas,
        datePaint: Paint,
        weatherIconPaint: Paint,
    ) {
        val weatherIconSize = dateHeight
        val weatherTextString = weatherText.getText(context, calendar.timeInMillis).toString()
        val weatherTextLength = datePaint.measureText(weatherTextString)
        val dateFontMetrics = datePaint.fontMetrics

        val weatherBottom = dateYOffset - weatherIconSize - context.dpToPx(2)

        weatherIconRect.left = (centerX - weatherTextLength / 2f - weatherIconSize / 2f).toInt()
        weatherIconRect.top = (weatherBottom - weatherIconSize + dateFontMetrics.descent / 2f).toInt()
        weatherIconRect.right = (centerX - weatherTextLength / 2f + weatherIconSize / 2f).toInt()
        weatherIconRect.bottom = (weatherBottom + dateFontMetrics.descent / 2f).toInt()

        val cachedWeatherIcon = this.currentWeatherIcon
        val cachedWeatherBitmap = this.currentWeatherBitmap
        val weatherIconBitmap = if ( cachedWeatherIcon != null && cachedWeatherBitmap != null && weatherIcon.sameAs(cachedWeatherIcon) ) {
            cachedWeatherBitmap
        } else {
            try {
                val bitmap = weatherIcon.loadDrawable(context)?.toBitmap(weatherIconRect.right - weatherIconRect.left, weatherIconRect.bottom - weatherIconRect.top)

                currentWeatherBitmap = bitmap
                currentWeatherIcon = weatherIcon

                bitmap
            } catch (t: Throwable) {
                currentWeatherBitmap = null
                currentWeatherIcon = null
                null
            }
        }

        val weatherTextX = centerX - weatherTextLength / 2f + weatherIconSize / 2f

        canvas.drawText(
            weatherTextString,
            weatherTextX,
            weatherBottom,
            datePaint
        )

        if( weatherIconBitmap != null ) {
            canvas.drawBitmap(
                weatherIconBitmap,
                null,
                weatherIconRect,
                weatherIconPaint
            )
        }

        weatherTextEndX = weatherTextX + weatherTextLength
    }
}