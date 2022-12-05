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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationText
import android.text.format.DateUtils
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
        spaceBeforeWeather: Int,
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
        spaceBeforeWeather: Int,
        weatherIconPaint: Paint,
    ) {
        val dateFormat = if( useShortDateFormat ) {
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_WEEKDAY or DateUtils.FORMAT_ABBREV_MONTH
        } else {
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_WEEKDAY
        }

        val dateText = DateUtils.formatDateTime(context, calendar.timeInMillis, dateFormat).capitalize()
        val dateTextLength = datePaint.measureText(dateText)
        val dateXOffset = if( isUserPremium && weatherComplicationData != null ) {
            val weatherText = weatherComplicationData.shortText
            val weatherIcon = weatherComplicationData.icon

            if( weatherText != null && weatherIcon != null ) {
                drawWeatherAndComputeDateXOffset(weatherText, weatherIcon, calendar, dateTextLength, canvas, spaceBeforeWeather, datePaint, weatherIconPaint)
            } else {
                currentWeatherBitmap = null
                currentWeatherIcon = null
                weatherTextEndX = null

                centerX - (dateTextLength / 2f)
            }
        } else {
            currentWeatherBitmap = null
            currentWeatherIcon = null
            weatherTextEndX = null

            centerX - (dateTextLength / 2f)
        }

        canvas.drawText(dateText, dateXOffset, dateYOffset, datePaint)
    }

    private fun drawWeatherAndComputeDateXOffset(
        weatherText: ComplicationText,
        weatherIcon: Icon,
        calendar: Calendar,
        dateTextLength: Float,
        canvas: Canvas,
        spaceBeforeWeather: Int,
        datePaint: Paint,
        weatherIconPaint: Paint,
    ): Float {
        val weatherIconSize = dateHeight
        val weatherTextString = weatherText.getText(context, calendar.timeInMillis).toString()
        val weatherTextLength = datePaint.measureText(weatherTextString)
        val dateFontMetrics = datePaint.fontMetrics

        val dateXOffset = centerX - (dateTextLength / 2f) - weatherTextLength / 2f - weatherIconSize / 2f - spaceBeforeWeather - dateFontMetrics.descent / 4f

        weatherIconRect.left = (dateXOffset + dateTextLength + spaceBeforeWeather).toInt()
        weatherIconRect.top = (dateYOffset - weatherIconSize + dateFontMetrics.descent / 2f).toInt()
        weatherIconRect.right = (dateXOffset + dateTextLength + weatherIconSize + spaceBeforeWeather + dateFontMetrics.descent / 2f).toInt()
        weatherIconRect.bottom = (dateYOffset + dateFontMetrics.descent).toInt()

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

        val weatherTextX = dateXOffset + dateTextLength + weatherIconSize + spaceBeforeWeather * 2

        canvas.drawText(
            weatherTextString,
            weatherTextX,
            dateYOffset,
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

        return dateXOffset
    }
}