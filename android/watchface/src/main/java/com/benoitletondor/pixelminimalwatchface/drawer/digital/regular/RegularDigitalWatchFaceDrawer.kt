/*
 *   Copyright 2021 Benoit LETONDOR
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
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.support.wearable.complications.rendering.CustomComplicationDrawable
import android.util.SparseArray
import android.view.WindowInsets
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.benoitletondor.pixelminimalwatchface.PhoneBatteryStatus
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.drawer.WatchFaceDrawer
import com.benoitletondor.pixelminimalwatchface.helper.*
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColors
import com.benoitletondor.pixelminimalwatchface.model.Storage
import com.benoitletondor.pixelminimalwatchface.model.getPrimaryColorForComplicationId
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class RegularDigitalWatchFaceDrawer(
    private val context: Context,
    private val storage: Storage,
) : WatchFaceDrawer {
    private var drawingState: RegularDrawerDrawingState = RegularDrawerDrawingState.NoScreenData

    private val productSansRegularFont: Typeface = ResourcesCompat.getFont(context, R.font.product_sans_regular)!!
    private val wearOSLogoPaint = Paint()
    private val timePaint = Paint().apply {
        typeface = productSansRegularFont
        strokeWidth = 1.8f
    }
    private val datePaint = Paint().apply {
        typeface = productSansRegularFont
    }
    private val weatherIconPaint = Paint()
    @ColorInt
    private val backgroundColor: Int = ContextCompat.getColor(context, R.color.face_background)
    @ColorInt
    private val timeColorDimmed: Int = ContextCompat.getColor(context, R.color.face_time_dimmed)
    @ColorInt
    private val dateAndBatteryColorDimmed: Int = ContextCompat.getColor(context, R.color.face_date_dimmed)
    @ColorInt
    private val complicationTitleColor: Int = ContextCompat.getColor(context, R.color.complication_title_color)
    private val wearOSLogo: Bitmap = ContextCompat.getDrawable(context, R.drawable.ic_wear_os_logo)!!.toBitmap()
    private val wearOSLogoAmbient: Bitmap = ContextCompat.getDrawable(context, R.drawable.ic_wear_os_logo_ambient)!!.toBitmap()
    private val batteryIconPaint: Paint = Paint()
    private var batteryIconSize = 0
    private val batteryLevelPaint = Paint().apply {
        typeface = productSansRegularFont
    }
    private val distanceBetweenPhoneAndWatchBattery: Int = context.dpToPx(3)
    private val titleSize: Int = context.resources.getDimensionPixelSize(R.dimen.complication_title_size)
    private val textSize: Int = context.resources.getDimensionPixelSize(R.dimen.complication_text_size)
    private var chinSize: Int = 0
    private var isRound: Boolean = false
    private val timeFormatter24H = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val timeFormatter12H = SimpleDateFormat("h:mm", Locale.getDefault())
    private var currentTimeSize = storage.getTimeSize()
    private var currentDateAndBatterySize = storage.getDateAndBatterySize()
    private val spaceBeforeWeather = context.dpToPx(5)
    private val topAndBottomMargins = context.getTopAndBottomMargins().toInt()
    private val weatherAndBatteryIconColorFilterDimmed: ColorFilter = PorterDuffColorFilter(dateAndBatteryColorDimmed, PorterDuff.Mode.SRC_IN)

    private val complicationDrawableSparseArray: SparseArray<ComplicationDrawable> = SparseArray(ACTIVE_COMPLICATIONS.size)

    override fun initializeComplicationDrawables(drawableCallback: Drawable.Callback): IntArray {
        val leftComplicationDrawable = CustomComplicationDrawable(context, false, drawableCallback)
        val middleComplicationDrawable = CustomComplicationDrawable(context, false, drawableCallback)
        val rightComplicationDrawable = CustomComplicationDrawable(context, false, drawableCallback)
        val bottomComplicationDrawable = CustomComplicationDrawable(context, true, drawableCallback)

        complicationDrawableSparseArray.put(PixelMinimalWatchFace.LEFT_COMPLICATION_ID, leftComplicationDrawable)
        complicationDrawableSparseArray.put(PixelMinimalWatchFace.MIDDLE_COMPLICATION_ID, middleComplicationDrawable)
        complicationDrawableSparseArray.put(PixelMinimalWatchFace.RIGHT_COMPLICATION_ID, rightComplicationDrawable)
        complicationDrawableSparseArray.put(PixelMinimalWatchFace.BOTTOM_COMPLICATION_ID, bottomComplicationDrawable)

        return ACTIVE_COMPLICATIONS
    }

    override fun onApplyWindowInsets(insets: WindowInsets) {
        chinSize = insets.systemWindowInsetBottom
        isRound = insets.isRound
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        drawingState = RegularDrawerDrawingState.NoCacheAvailable(
            width,
            height,
            width / 2f,
            height / 2f
        )
    }

    override fun onComplicationColorsUpdate(complicationColors: ComplicationColors, complicationsData: SparseArray<ComplicationData>) {
        ACTIVE_COMPLICATIONS.forEach { complicationId ->
            val complicationDrawable = complicationDrawableSparseArray[complicationId]
            val primaryComplicationColor = complicationColors.getPrimaryColorForComplicationId(complicationId)

            complicationDrawable.setTitleSizeActive(titleSize)
            complicationDrawable.setTitleSizeAmbient(titleSize)
            complicationDrawable.setTitleColorActive(complicationTitleColor)
            complicationDrawable.setTitleColorAmbient(complicationTitleColor)
            complicationDrawable.setIconColorActive(primaryComplicationColor)
            complicationDrawable.setIconColorAmbient(dateAndBatteryColorDimmed)
            complicationDrawable.setTextTypefaceActive(productSansRegularFont)
            complicationDrawable.setTitleTypefaceActive(productSansRegularFont)
            complicationDrawable.setTextTypefaceAmbient(productSansRegularFont)
            complicationDrawable.setTitleTypefaceAmbient(productSansRegularFont)

            if( complicationId == PixelMinimalWatchFace.BOTTOM_COMPLICATION_ID) {
                complicationDrawable.setBorderColorActive(ContextCompat.getColor(context, R.color.transparent))
            }
            complicationDrawable.setBorderColorAmbient(ContextCompat.getColor(context, R.color.transparent))

            onComplicationDataUpdate(complicationId, complicationsData[complicationId], complicationColors)
        }
    }

    override fun onComplicationDataUpdate(complicationId: Int,
                                          data: ComplicationData?,
                                          complicationColors: ComplicationColors
    ) {
        val complicationDrawable = complicationDrawableSparseArray[complicationId]
        complicationDrawable.setComplicationData(data)

        val primaryComplicationColor = complicationColors.getPrimaryColorForComplicationId(complicationId)
        if( data != null && data.icon != null ) {
            if( complicationId == PixelMinimalWatchFace.BOTTOM_COMPLICATION_ID && ( data.longTitle != null ) ) {
                complicationDrawable.setTextColorActive(primaryComplicationColor)
                complicationDrawable.setTextColorAmbient(dateAndBatteryColorDimmed)
            } else {
                complicationDrawable.setTextColorActive(complicationTitleColor)
                complicationDrawable.setTextColorAmbient(complicationTitleColor)
            }

            if( complicationId != PixelMinimalWatchFace.BOTTOM_COMPLICATION_ID && data.shortTitle == null ) {
                complicationDrawable.setTextSizeActive(titleSize)
                complicationDrawable.setTextSizeAmbient(titleSize)
            } else {
                complicationDrawable.setTextSizeActive(textSize)
                complicationDrawable.setTextSizeAmbient(textSize)
            }
        } else {
            complicationDrawable.setTextColorActive(primaryComplicationColor)
            complicationDrawable.setTextColorAmbient(dateAndBatteryColorDimmed)
            complicationDrawable.setTextSizeActive(textSize)
            complicationDrawable.setTextSizeAmbient(textSize)
        }
    }

    override fun tapIsOnComplication(x: Int, y: Int): Boolean {
        ACTIVE_COMPLICATIONS.forEach { complicationId ->
            val complicationDrawable = complicationDrawableSparseArray.get(complicationId)

            if ( complicationDrawable.onTap(x, y) ) {
                return true
            }
        }

        return false
    }

    override fun tapIsOnWeather(x: Int, y: Int): Boolean {
        val drawingState = drawingState
        if( !storage.shouldShowWeather() ||
            !storage.isUserPremium() ||
            drawingState !is RegularDrawerDrawingState.CacheAvailable ) {
            return false
        }

        val displayRect = drawingState.getWeatherDisplayRect() ?: return false
        return displayRect.contains(x, y)
    }

    override fun tapIsInCenterOfScreen(x: Int, y: Int): Boolean {
        val drawingState = drawingState as? RegularDrawerDrawingState.CacheAvailable ?: return false

        val centerRect = Rect(
            (drawingState.screenWidth * 0.25f).toInt(),
            (drawingState.screenHeight * 0.25f).toInt(),
            (drawingState.screenWidth * 0.75f).toInt(),
            (drawingState.screenHeight * 0.75f).toInt()
        )

        return centerRect.contains(x, y)
    }

    override fun tapIsOnBattery(x: Int, y: Int): Boolean {
        val drawingState = drawingState as? RegularDrawerDrawingState.CacheAvailable ?: return false

        return drawingState.tapIsOnBattery(x, y)
    }

    override fun draw(
        canvas: Canvas,
        calendar: Calendar,
        muteMode: Boolean,
        ambient:Boolean,
        lowBitAmbient: Boolean,
        burnInProtection: Boolean,
        weatherComplicationData: ComplicationData?,
        batteryComplicationData: ComplicationData?,
        phoneBatteryStatus: PhoneBatteryStatus?,
    ) {
        setPaintVariables(muteMode, ambient, lowBitAmbient, burnInProtection)
        drawBackground(canvas)

        val currentDrawingState = drawingState
        if( currentDrawingState is RegularDrawerDrawingState.NoCacheAvailable ) {
            drawingState = currentDrawingState.buildCache()
        } else if( currentDrawingState is RegularDrawerDrawingState.CacheAvailable &&
            (currentTimeSize != storage.getTimeSize() || currentDateAndBatterySize != storage.getDateAndBatterySize()) ) {
            drawingState = currentDrawingState.buildCache()
        }

        val drawingState = drawingState
        if( drawingState is RegularDrawerDrawingState.CacheAvailable ){
            drawingState.draw(
                canvas,
                calendar,
                ambient,
                storage.isUserPremium(),
                storage.shouldShowSecondsRing(),
                storage.shouldShowBattery(),
                storage.shouldShowPhoneBattery(),
                !ambient || storage.getShowDateInAmbient(),
                weatherComplicationData,
                batteryComplicationData,
                phoneBatteryStatus,
            )
        }
    }

    private fun RegularDrawerDrawingState.NoCacheAvailable.buildCache(): RegularDrawerDrawingState.CacheAvailable {
        val timeSize = storage.getTimeSize()
        val dateAndBatterySize = storage.getDateAndBatterySize()
        setScaledSizes(timeSize, dateAndBatterySize)

        val timeText = "22:13"
        val timeTextBounds = Rect().apply {
            timePaint.getTextBounds(timeText, 0, timeText.length, this)
        }
        val timeYOffset = centerY + (timeTextBounds.height() / 2.0f ) - 5f

        val dateText = "May, 15"
        val dateTextHeight = Rect().apply {
            datePaint.getTextBounds(dateText, 0, dateText.length, this)
        }.height()
        val dateYOffset = timeYOffset + (timeTextBounds.height() / 2) - (dateTextHeight / 2.0f ) + context.dpToPx(8)

        val complicationsDrawingCache = buildComplicationDrawingCache(
            timeYOffset - timeTextBounds.height() - context.dpToPx(6),
            dateYOffset + dateTextHeight / 2,
        )

        currentTimeSize = timeSize
        currentDateAndBatterySize = dateAndBatterySize

        val batteryBottomY = screenHeight - chinSize - topAndBottomMargins

        return RegularDrawerDrawingState.CacheAvailable(
            context,
            batteryIconSize,
            batteryBottomY,
            batteryBottomY + context.dpToPx(1),
            dateTextHeight,
            dateYOffset,
            screenWidth,
            screenHeight,
            centerX,
            centerY,
            timeYOffset,
            complicationsDrawingCache,
        )
    }

    private fun RegularDrawerDrawingState.NoCacheAvailable.buildComplicationDrawingCache(
        topBottom: Float,
        bottomTop: Float,
    ): ComplicationsDrawingCache {
        val wearOsImage = wearOSLogo

        val sizeOfComplication = if( isRound ) { (screenWidth / 4.5).toInt() } else { min(topBottom.toInt() - topAndBottomMargins - context.dpToPx(2), (screenWidth / 3.5).toInt()) }
        // If watch is round, align top widgets with the top of the time, otherwise center them in the top space
        val verticalOffset = if ( isRound ) { topBottom.toInt() - sizeOfComplication } else { topBottom.toInt() / 2 - (sizeOfComplication / 2) + topAndBottomMargins }
        val distanceBetweenComplications = context.dpToPx(3)

        val maxWidth = max(sizeOfComplication, wearOsImage.width)

        val leftBounds = Rect(
            (centerX - (maxWidth / 2) - distanceBetweenComplications - sizeOfComplication).toInt(),
            verticalOffset,
            (centerX - (maxWidth / 2)  - distanceBetweenComplications).toInt(),
            (verticalOffset + sizeOfComplication)
        )

        complicationDrawableSparseArray[PixelMinimalWatchFace.LEFT_COMPLICATION_ID]?.let { leftComplicationDrawable ->
            leftComplicationDrawable.bounds = leftBounds
        }

        val middleBounds = Rect(
            (centerX - (sizeOfComplication / 2)).toInt(),
            verticalOffset,
            (centerX + (sizeOfComplication / 2)).toInt(),
            (verticalOffset + sizeOfComplication)
        )

        complicationDrawableSparseArray[PixelMinimalWatchFace.MIDDLE_COMPLICATION_ID]?.let { middleComplicationDrawable ->
            middleComplicationDrawable.bounds = middleBounds
        }

        val rightBounds = Rect(
            (centerX + (maxWidth / 2) + distanceBetweenComplications).toInt(),
            verticalOffset,
            (centerX + (maxWidth / 2)  + distanceBetweenComplications + sizeOfComplication).toInt(),
            (verticalOffset + sizeOfComplication)
        )

        complicationDrawableSparseArray[PixelMinimalWatchFace.RIGHT_COMPLICATION_ID]?.let { rightComplicationDrawable ->
            rightComplicationDrawable.bounds = rightBounds
        }

        val availableBottomSpace = screenHeight - bottomTop - chinSize - topAndBottomMargins
        val bottomComplicationHeight = min(availableBottomSpace, context.dpToPx(36).toFloat())
        val bottomComplicationTop = if( isRound ) { bottomTop.toInt() + context.dpToPx(5) } else { (bottomTop + + context.dpToPx(5) + availableBottomSpace - bottomComplicationHeight).toInt() }
        val bottomComplicationBottom = if( isRound ) { (bottomTop + bottomComplicationHeight).toInt() } else { (bottomTop + availableBottomSpace).toInt() }
        val bottomComplicationLeft = computeComplicationLeft(bottomComplicationBottom, screenHeight)
        val bottomComplicationWidth = (screenWidth - 2* bottomComplicationLeft) * 0.9
        val bottomBounds = Rect(
            (centerX - (bottomComplicationWidth / 2)).toInt(),
            bottomComplicationTop,
            (centerX + (bottomComplicationWidth / 2)).toInt(),
            bottomComplicationBottom
        )

        complicationDrawableSparseArray[PixelMinimalWatchFace.BOTTOM_COMPLICATION_ID]?.let { bottomComplicationDrawable ->
            bottomComplicationDrawable.bounds = bottomBounds
        }

        val iconXOffset = centerX - (wearOsImage.width / 2.0f)
        val iconYOffset = leftBounds.top + (leftBounds.height() / 2) - (wearOsImage.height / 2)

        return ComplicationsDrawingCache(
            iconXOffset,
            iconYOffset.toFloat()
        )
    }

    private fun computeComplicationLeft(bottomY: Int, screenHeight: Int): Int {
        return if( isRound ) {
            screenHeight / 2 - sqrt((screenHeight / 2).toDouble().pow(2) - ((bottomY - (screenHeight / 2)).toDouble().pow(2))).toInt()
        } else {
            context.dpToPx(10)
        }
    }

    private fun RegularDrawerDrawingState.CacheAvailable.draw(
        canvas: Canvas,
        calendar: Calendar,
        ambient:Boolean,
        isUserPremium: Boolean,
        drawSecondsRing: Boolean,
        drawBattery: Boolean,
        drawPhoneBattery: Boolean,
        drawDate: Boolean,
        weatherComplicationData: ComplicationData?,
        batteryComplicationData: ComplicationData?,
        phoneBatteryStatus: PhoneBatteryStatus?,
    ) {
        val timeText = if( storage.getUse24hTimeFormat()) {
            timeFormatter24H.calendar = calendar
            timeFormatter24H.format(Date(calendar.timeInMillis))
        } else {
            timeFormatter12H.calendar = calendar
            timeFormatter12H.format(Date(calendar.timeInMillis))
        }
        val timeXOffset = centerX - (timePaint.measureText(timeText) / 2f)
        canvas.drawText(timeText, timeXOffset, timeYOffset, timePaint)

        complicationsDrawingCache.drawComplications(canvas, ambient, calendar, isUserPremium)

        if( drawDate ) {
            drawDateAndWeather(
                canvas,
                weatherComplicationData,
                storage.getUseShortDateFormat(),
                isUserPremium,
                calendar,
                datePaint,
                spaceBeforeWeather,
                weatherIconPaint,
            )
        }

        if( drawSecondsRing && !ambient ) {
            drawSecondRing(canvas, calendar)
        }

        if( isUserPremium && (drawBattery || drawPhoneBattery) && (!ambient || !storage.shouldHideBatteryInAmbient()) ) {
            drawBattery(
                canvas,
                batteryLevelPaint,
                batteryIconPaint,
                distanceBetweenPhoneAndWatchBattery,
                drawBattery,
                drawPhoneBattery,
                calendar,
                batteryComplicationData,
                phoneBatteryStatus,
            )
        }
    }

    private fun ComplicationsDrawingCache.drawComplications(
        canvas: Canvas,
        ambient: Boolean,
        calendar: Calendar,
        isUserPremium: Boolean
    ) {
        if( isUserPremium && (storage.shouldShowComplicationsInAmbientMode() || !ambient) ) {
            ACTIVE_COMPLICATIONS.forEach { complicationId ->
                val complicationDrawable = complicationDrawableSparseArray[complicationId]

                if( complicationId == PixelMinimalWatchFace.MIDDLE_COMPLICATION_ID && storage.shouldShowWearOSLogo() ) {
                    return@forEach
                }

                if( complicationId == PixelMinimalWatchFace.BOTTOM_COMPLICATION_ID && (storage.shouldShowBattery() || storage.shouldShowPhoneBattery()) ) {
                    return@forEach
                }

                complicationDrawable.draw(canvas, calendar.timeInMillis)
            }
        }

        if( storage.shouldShowWearOSLogo() ) {
            val wearOsImage = if( ambient ) { wearOSLogoAmbient } else { wearOSLogo }
            canvas.drawBitmap(wearOsImage, iconXOffset, iconYOffset, wearOSLogoPaint)
        }
    }

    private fun RegularDrawerDrawingState.CacheAvailable.buildCache(): RegularDrawerDrawingState.CacheAvailable {
        return RegularDrawerDrawingState.NoCacheAvailable(screenWidth, screenHeight, centerX, centerY).buildCache()
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
    }

    private fun setPaintVariables(muteMode: Boolean,
                                  ambient:Boolean,
                                  lowBitAmbient: Boolean,
                                  burnInProtection: Boolean) {
        wearOSLogoPaint.isAntiAlias = !ambient

        timePaint.apply {
            isAntiAlias = !(ambient && lowBitAmbient)
            style = if( ambient && !storage.shouldShowFilledTimeInAmbientMode() ) { Paint.Style.STROKE } else { Paint.Style.FILL }
            color = if( ambient ) { timeColorDimmed } else { storage.getTimeAndDateColor() }
        }

        datePaint.apply {
            isAntiAlias = !(ambient && lowBitAmbient)
            color = if( ambient ) { dateAndBatteryColorDimmed } else { storage.getTimeAndDateColor() }
        }

        weatherIconPaint.apply {
            isAntiAlias = !ambient
            colorFilter = if( ambient ) { weatherAndBatteryIconColorFilterDimmed } else { storage.getTimeAndDateColorFilter() }
        }

        batteryLevelPaint.apply {
            isAntiAlias = !(ambient && lowBitAmbient)
            color = if( ambient ) { dateAndBatteryColorDimmed } else { storage.getBatteryIndicatorColor() }
        }

        batteryIconPaint.apply {
            isAntiAlias = !(ambient && lowBitAmbient)
            colorFilter = if( ambient ) { weatherAndBatteryIconColorFilterDimmed } else { storage.getBatteryIndicatorColorFilter() }
        }

        ACTIVE_COMPLICATIONS.forEach {
            complicationDrawableSparseArray[it].setLowBitAmbient(lowBitAmbient)
        }

        ACTIVE_COMPLICATIONS.forEach {
            complicationDrawableSparseArray[it].setBurnInProtection(burnInProtection)
        }

        ACTIVE_COMPLICATIONS.forEach {
            complicationDrawableSparseArray[it].setInAmbientMode(ambient)
        }
    }

    private fun setScaledSizes(timeSize: Int, dateAndBatterySize: Int) {
        val timeScaleFactor = fontDisplaySizeToScaleFactor(timeSize)
        val dateAndBatteryScaleFactor = fontDisplaySizeToScaleFactor(dateAndBatterySize)

        timePaint.textSize = context.resources.getDimension(
            if( isRound ) {
                R.dimen.time_text_size_round
            } else {
                R.dimen.time_text_size
            }
        ) * timeScaleFactor

        val dateSize = context.resources.getDimension(
            if( isRound ) {
                R.dimen.date_text_size_round
            } else {
                R.dimen.date_text_size
            }
        ) * dateAndBatteryScaleFactor

        datePaint.textSize = dateSize
        batteryLevelPaint.textSize = context.resources.getDimension(R.dimen.battery_level_size) * dateAndBatteryScaleFactor
        batteryIconSize = (context.resources.getDimension(R.dimen.battery_icon_size) * dateAndBatteryScaleFactor).toInt()
    }

    companion object {
        val ACTIVE_COMPLICATIONS = intArrayOf(
            PixelMinimalWatchFace.LEFT_COMPLICATION_ID,
            PixelMinimalWatchFace.MIDDLE_COMPLICATION_ID,
            PixelMinimalWatchFace.RIGHT_COMPLICATION_ID,
            PixelMinimalWatchFace.BOTTOM_COMPLICATION_ID,
        )
    }
}