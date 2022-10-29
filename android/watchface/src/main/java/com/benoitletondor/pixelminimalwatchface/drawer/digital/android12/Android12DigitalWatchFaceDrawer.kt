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
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.support.wearable.complications.rendering.CustomComplicationDrawable
import android.util.Log
import android.util.SparseArray
import android.view.WindowInsets
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.benoitletondor.pixelminimalwatchface.helper.toBitmap
import com.benoitletondor.pixelminimalwatchface.*
import com.benoitletondor.pixelminimalwatchface.drawer.WatchFaceDrawer
import com.benoitletondor.pixelminimalwatchface.helper.*
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColors
import com.benoitletondor.pixelminimalwatchface.model.Storage
import com.benoitletondor.pixelminimalwatchface.model.getPrimaryColorForComplicationId
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class Android12DigitalWatchFaceDrawer(
    private val context: Context,
    private val storage: Storage,
) : WatchFaceDrawer {
    private var drawingState: Android12DrawingState = Android12DrawingState.NoScreenData

    private val hourFormatter24H = SimpleDateFormat("HH", Locale.getDefault())
    private val hourFormatter12H = SimpleDateFormat("hh", Locale.getDefault())
    private val minFormatter = SimpleDateFormat("mm", Locale.getDefault())

    private val productSansRegularFont: Typeface = ResourcesCompat.getFont(context, R.font.product_sans_regular)!!
    private val productSansThinFont: Typeface = ResourcesCompat.getFont(context, R.font.product_sans_thin)!!

    private val wearOSLogoPaint = Paint()
    private val timePaint = Paint().apply {
        typeface = productSansRegularFont
    }
    private val datePaint = Paint().apply {
        typeface = productSansRegularFont
    }
    private val weatherIconPaint = Paint()
    private val notificationsPaint = Paint()
    @ColorInt
    private val backgroundColor: Int = ContextCompat.getColor(context, R.color.face_background)
    @ColorInt
    private val timeColorDimmed: Int = ContextCompat.getColor(context, R.color.face_time_dimmed)
    @ColorInt
    private val dateAndBatteryColorDimmed: Int = ContextCompat.getColor(context, R.color.face_date_dimmed)
    @ColorInt
    private val complicationTitleColor: Int = ContextCompat.getColor(context, R.color.complication_title_color)
    private val batteryIconPaint: Paint = Paint()
    private var batteryIconSize = 0
    private val batteryLevelPaint = Paint().apply {
        typeface = productSansRegularFont
    }
    private val secondsRingPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10F
        isAntiAlias = true
    }
    private val distanceBetweenPhoneAndWatchBattery: Int = context.dpToPx(3)
    private val distanceBetweenHourAndMin: Int = context.dpToPx(4)
    private val titleSize: Int = context.resources.getDimensionPixelSize(R.dimen.complication_title_size)
    private val textSize: Int = context.resources.getDimensionPixelSize(R.dimen.complication_text_size)
    private var chinSize: Int = 0
    private var isRound: Boolean = false
    private var currentTimeSize = storage.getTimeSize()
    private var currentDateAndBatterySize = storage.getDateAndBatterySize()
    private var currentWidgetsSize = storage.getWidgetsSize()
    private var currentShowWearOSLogo = storage.showWearOSLogo()
    private var currentShowNotificationIcons = storage.isNotificationsSyncActivated()
    private val weatherAndBatteryIconColorFilterDimmed: ColorFilter = PorterDuffColorFilter(dateAndBatteryColorDimmed, PorterDuff.Mode.SRC_IN)
    private val timeOffsetX = context.dpToPx(-2)
    private val timeCharPaddingX = context.dpToPx(1)
    private var timePaddingY = 0
    private val topAndBottomMargins = context.getTopAndBottomMargins()
    private val verticalPaddingBetweenElements = context.dpToPx(7)
    private var currentShowBatteryIndicator = false

    private val complicationDrawableSparseArray: SparseArray<ComplicationDrawable> = SparseArray(ACTIVE_COMPLICATIONS.size)

    override fun initializeComplicationDrawables(drawableCallback: Drawable.Callback): IntArray {
        val topLeftComplicationDrawable = CustomComplicationDrawable(context, false, drawableCallback)
        val topRightComplicationDrawable = CustomComplicationDrawable(context, false, drawableCallback)
        val bottomLeftComplicationDrawable = CustomComplicationDrawable(context, false, drawableCallback)
        val bottomRightComplicationDrawable = CustomComplicationDrawable(context, false, drawableCallback)

        complicationDrawableSparseArray.put(PixelMinimalWatchFace.ANDROID_12_TOP_LEFT_COMPLICATION_ID, topLeftComplicationDrawable)
        complicationDrawableSparseArray.put(PixelMinimalWatchFace.ANDROID_12_TOP_RIGHT_COMPLICATION_ID, topRightComplicationDrawable)
        complicationDrawableSparseArray.put(PixelMinimalWatchFace.ANDROID_12_BOTTOM_LEFT_COMPLICATION_ID, bottomLeftComplicationDrawable)
        complicationDrawableSparseArray.put(PixelMinimalWatchFace.ANDROID_12_BOTTOM_RIGHT_COMPLICATION_ID, bottomRightComplicationDrawable)

        return ACTIVE_COMPLICATIONS
    }

    override fun onApplyWindowInsets(insets: WindowInsets) {
        chinSize = insets.systemWindowInsetBottom
        isRound = insets.isRound
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        drawingState = Android12DrawingState.NoCacheAvailable(
            width,
            height,
            width / 2f,
            height / 2f
        )
    }

    override fun onComplicationColorsUpdate(
        complicationColors: ComplicationColors,
        complicationsData: SparseArray<ComplicationData>,
        showComplicationsColorsInAmbient: Boolean,
    ) {
        ACTIVE_COMPLICATIONS.forEach { complicationId ->
            val complicationDrawable = complicationDrawableSparseArray[complicationId]
            val primaryComplicationColor = complicationColors.getPrimaryColorForComplicationId(complicationId)

            complicationDrawable.setTitleSizeActive(titleSize)
            complicationDrawable.setTitleSizeAmbient(titleSize)
            complicationDrawable.setTitleColorActive(complicationTitleColor)
            complicationDrawable.setTitleColorAmbient(complicationTitleColor)
            complicationDrawable.setIconColorActive(primaryComplicationColor)
            complicationDrawable.setIconColorAmbient(if (showComplicationsColorsInAmbient) primaryComplicationColor.dimmed() else dateAndBatteryColorDimmed)
            complicationDrawable.setTextTypefaceActive(productSansRegularFont)
            complicationDrawable.setTitleTypefaceActive(productSansRegularFont)
            complicationDrawable.setTextTypefaceAmbient(productSansRegularFont)
            complicationDrawable.setTitleTypefaceAmbient(productSansRegularFont)
            complicationDrawable.setBorderColorActive(ContextCompat.getColor(context, R.color.transparent))
            complicationDrawable.setBorderColorAmbient(ContextCompat.getColor(context, R.color.transparent))

            onComplicationDataUpdate(complicationId, complicationsData[complicationId], complicationColors, showComplicationsColorsInAmbient)
        }
    }

    override fun onComplicationDataUpdate(
        complicationId: Int,
        data: ComplicationData?,
        complicationColors: ComplicationColors,
        showComplicationsColorsInAmbient: Boolean,
    ) {
        val complicationDrawable = complicationDrawableSparseArray[complicationId] ?: kotlin.run {
            if (DEBUG_LOGS) Log.d(TAG, "Ignoring update for complicationId: $complicationId")
            return
        }

        complicationDrawable.setComplicationData(data)

        val primaryComplicationColor = complicationColors.getPrimaryColorForComplicationId(complicationId)
        val primaryComplicationColorDimmed = primaryComplicationColor.dimmed()
        if( data != null && data.icon != null ) {
            complicationDrawable.setTextColorActive(complicationTitleColor)
            complicationDrawable.setTextColorAmbient(complicationTitleColor)

            if( data.shortTitle == null ) {
                complicationDrawable.setTextSizeActive(titleSize)
                complicationDrawable.setTextSizeAmbient(titleSize)
            } else {
                complicationDrawable.setTextSizeActive(textSize)
                complicationDrawable.setTextSizeAmbient(textSize)
            }
        } else {
            complicationDrawable.setTextColorActive(primaryComplicationColor)
            complicationDrawable.setTextColorAmbient(if (showComplicationsColorsInAmbient) primaryComplicationColorDimmed else dateAndBatteryColorDimmed)
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
        if( !storage.showWeather() ||
            !storage.isUserPremium() ||
            drawingState !is Android12DrawingState.CacheAvailable ) {
            return false
        }

        val displayRect = drawingState.getWeatherDisplayRect() ?: return false
        return displayRect.contains(x, y)
    }

    override fun tapIsInCenterOfScreen(x: Int, y: Int): Boolean {
        val drawingState = drawingState as? Android12DrawingState.CacheAvailable ?: return false

        val centerRect = Rect(
            (drawingState.screenWidth * 0.33f).toInt(),
            (drawingState.screenHeight * 0.25f).toInt(),
            (drawingState.screenWidth * 0.66f).toInt(),
            (drawingState.screenHeight * 0.75f).toInt()
        )

        return centerRect.contains(x, y)
    }

    override fun tapIsOnBattery(x: Int, y: Int): Boolean {
        val drawingState = drawingState as? Android12DrawingState.CacheAvailable ?: return false

        return drawingState.tapIsOnBattery(x, y)
    }

    override fun isTapOnNotifications(x: Int, y: Int): Boolean {
        val drawingState = drawingState as? Android12DrawingState.CacheAvailable ?: return false

        return drawingState.isTapOnNotifications(x, y)
    }

    override fun draw(
        canvas: Canvas,
        calendar: Calendar,
        muteMode: Boolean,
        ambient: Boolean,
        lowBitAmbient: Boolean,
        burnInProtection: Boolean,
        weatherComplicationData: ComplicationData?,
        batteryComplicationData: ComplicationData?,
        phoneBatteryStatus: PhoneBatteryStatus?,
        notificationsState: PhoneNotifications.NotificationState?,
    ) {
        setPaintVariables(muteMode, ambient, lowBitAmbient, burnInProtection)
        drawBackground(canvas)

        val currentDrawingState = drawingState
        if( currentDrawingState is Android12DrawingState.NoCacheAvailable ) {
            drawingState = currentDrawingState.buildCache()
        } else if( currentDrawingState is Android12DrawingState.CacheAvailable &&
            (currentTimeSize != storage.getTimeSize() ||
            currentDateAndBatterySize != storage.getDateAndBatterySize() ||
            (currentShowBatteryIndicator != (storage.showPhoneBattery() || storage.showWatchBattery())) ||
            currentWidgetsSize != storage.getWidgetsSize() ||
            currentShowWearOSLogo != storage.showWearOSLogo() ||
            currentShowNotificationIcons != storage.isNotificationsSyncActivated() )) {

            currentShowNotificationIcons = storage.isNotificationsSyncActivated()
            currentShowWearOSLogo = storage.showWearOSLogo()
            currentShowBatteryIndicator = storage.showPhoneBattery() || storage.showWatchBattery()
            drawingState = currentDrawingState.buildCache()
        }

        val drawingState = drawingState
        if( drawingState is Android12DrawingState.CacheAvailable ){
            drawingState.draw(
                canvas,
                calendar,
                ambient,
                storage.isUserPremium(),
                storage.showSecondsRing(),
                storage.showWatchBattery(),
                storage.showPhoneBattery(),
                !ambient || storage.getShowDateInAmbient(),
                weatherComplicationData,
                batteryComplicationData,
                phoneBatteryStatus,
                notificationsState,
            )
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
    }

    private fun setPaintVariables(muteMode: Boolean,
                                  ambient:Boolean,
                                  lowBitAmbient: Boolean,
                                  burnInProtection: Boolean) {
        wearOSLogoPaint.isAntiAlias = !ambient

        val shouldUseThinFont = (ambient && !storage.useNormalTimeStyleInAmbientMode()) || (!ambient && storage.useThinTimeStyleInRegularMode())
        timePaint.apply {
            isAntiAlias = !(ambient && lowBitAmbient)
            color = if( ambient ) { if (storage.showColorsInAmbientMode()) storage.getTimeColor().dimmed() else timeColorDimmed } else { storage.getTimeColor() }
            typeface = if( shouldUseThinFont ) { productSansThinFont } else { productSansRegularFont }
        }

        datePaint.apply {
            isAntiAlias = !(ambient && lowBitAmbient)
            color = if( ambient ) { if (storage.showColorsInAmbientMode()) storage.getDateColor().dimmed() else dateAndBatteryColorDimmed } else { storage.getDateColor() }
        }

        weatherIconPaint.apply {
            isAntiAlias = !ambient
            colorFilter = if( ambient ) { if (storage.showColorsInAmbientMode()) storage.getDateColor().dimmed().colorFilter() else weatherAndBatteryIconColorFilterDimmed } else { storage.getDateColorFilter() }
        }

        batteryLevelPaint.apply {
            isAntiAlias = !(ambient && lowBitAmbient)
            color = if( ambient ) { if (storage.showColorsInAmbientMode()) storage.getBatteryIndicatorColor().dimmed() else dateAndBatteryColorDimmed } else { storage.getBatteryIndicatorColor() }
        }

        batteryIconPaint.apply {
            isAntiAlias = !(ambient && lowBitAmbient)
            colorFilter = if( ambient ) { if (storage.showColorsInAmbientMode()) storage.getBatteryIndicatorColor().dimmed().colorFilter() else weatherAndBatteryIconColorFilterDimmed } else { storage.getBatteryIndicatorColorFilter() }
        }

        secondsRingPaint.apply {
            colorFilter = storage.getSecondRingColor()
        }

        notificationsPaint.apply {
            isAntiAlias = !(ambient && lowBitAmbient)
            colorFilter = if( ambient ) { if (storage.showColorsInAmbientMode()) storage.getNotificationIconsColor().dimmed().colorFilter() else weatherAndBatteryIconColorFilterDimmed } else { storage.getNotificationIconsColorFilter() }
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

    private fun Android12DrawingState.NoCacheAvailable.buildCache(): Android12DrawingState.CacheAvailable {
        timePaddingY = if (storage.showWearOSLogo() || storage.isNotificationsSyncActivated()) {
            context.dpToPx(-5)
        } else {
            0
        }

        val timeSize = storage.getTimeSize()
        val dateAndBatterySize = storage.getDateAndBatterySize()
        setScaledSizes(timeSize, dateAndBatterySize)

        val timeText = "0"
        val timeTextBounds = Rect().apply {
            timePaint.getTextBounds(timeText, 0, timeText.length, this)
        }
        val timeHeight = timeTextBounds.height()
        val timeWidth = timeTextBounds.width() * 2 + timeCharPaddingX * 2

        val timeX = centerX - timeWidth / 2f + timeOffsetX

        val dateText = "May, 15"
        val dateTextHeight = Rect().apply {
            datePaint.getTextBounds(dateText, 0, dateText.length, this)
        }.height()
        val timeTopY = (centerY - timeHeight - distanceBetweenHourAndMin + timePaddingY)
        val timeBottomY = (centerY + timeHeight + distanceBetweenHourAndMin + timePaddingY)
        val dateBottomY = timeTopY - verticalPaddingBetweenElements

        val batteryHeight = Rect().apply {
            batteryLevelPaint.getTextBounds("22%", 0, 3, this)
        }.height()
        val batteryBottomY = if (storage.showWearOSLogo() || storage.isNotificationsSyncActivated()) {
            screenHeight - chinSize - topAndBottomMargins.toInt()
        } else {
            (timeBottomY + batteryHeight + context.dpToPx(1) + verticalPaddingBetweenElements).toInt()
        }

        val batteryTopY = batteryBottomY - batteryHeight

        val complicationsDrawingCache = buildComplicationDrawingCache(
            timeX = timeX,
            timeHeight = timeHeight,
            timeBottomY = timeBottomY,
            batteryTopY = if(storage.showWatchBattery() || storage.showPhoneBattery()) { batteryTopY } else { batteryBottomY },
        )

        currentTimeSize = timeSize
        currentDateAndBatterySize = dateAndBatterySize

        return Android12DrawingState.CacheAvailable(
            context,
            batteryIconSize,
            batteryBottomY,
            batteryBottomY + context.dpToPx(1),
            dateTextHeight,
            dateBottomY,
            screenWidth,
            screenHeight,
            centerX,
            centerY,
            timeHeight,
            timeX,
            timeTextBounds.width() + timeCharPaddingX,
            complicationsDrawingCache,
        )
    }

    private fun Android12DrawingState.CacheAvailable.buildCache(): Android12DrawingState.CacheAvailable {
        return Android12DrawingState.NoCacheAvailable(screenWidth, screenHeight, centerX, centerY).buildCache()
    }

    private fun Android12DrawingState.NoCacheAvailable.buildComplicationDrawingCache(
        timeX: Float,
        timeHeight: Int,
        timeBottomY: Float,
        batteryTopY: Int,
    ): ComplicationsDrawingCache {
        val wearOsImage = ContextCompat.getDrawable(context, R.drawable.ic_wear_os_logo)!!.toBitmap()

        currentWidgetsSize = storage.getWidgetsSize()
        val widgetsScaleFactor = fontDisplaySizeToScaleFactor(currentWidgetsSize, android12Layout = true)

        val complicationSize = (((screenWidth - timeX) * 0.35f) * widgetsScaleFactor).toInt()
        val wearOSLogoWidth = wearOsImage.width.toFloat()
        val wearOSLogoHeight = wearOsImage.height.toFloat()

        val leftX = (timeX / (if (isRound) 1.7f else 2f) - complicationSize / 2f).toInt()
        val rightX = (screenWidth - timeX / (if (isRound) 1.7f else 2f) - complicationSize / 2f).toInt()
        val topY = (centerY - distanceBetweenHourAndMin - timeHeight / 2f + complicationSize / 2f + context.dpToPx(3)).toInt() + timePaddingY
        val bottomY = (centerY + distanceBetweenHourAndMin + timeHeight / 2f - complicationSize / 2f + context.dpToPx(3)).toInt() + timePaddingY

        complicationDrawableSparseArray[PixelMinimalWatchFace.ANDROID_12_TOP_LEFT_COMPLICATION_ID]
            ?.setBounds(leftX, topY - complicationSize, leftX + complicationSize, topY)
        complicationDrawableSparseArray[PixelMinimalWatchFace.ANDROID_12_TOP_RIGHT_COMPLICATION_ID]
            ?.setBounds(rightX, topY - complicationSize, rightX + complicationSize, topY)
        complicationDrawableSparseArray[PixelMinimalWatchFace.ANDROID_12_BOTTOM_LEFT_COMPLICATION_ID]
            ?.setBounds(leftX, bottomY, leftX + complicationSize, bottomY + complicationSize)
        complicationDrawableSparseArray[PixelMinimalWatchFace.ANDROID_12_BOTTOM_RIGHT_COMPLICATION_ID]
            ?.setBounds(rightX, bottomY, rightX + complicationSize, bottomY + complicationSize)

        val paddingBetweenBottomLogoAndBattery = if(storage.showWatchBattery() || storage.showPhoneBattery()) {
            verticalPaddingBetweenElements
        } else {
            0
        }

        val targetWearOSLogoHeight = min(wearOSLogoHeight, (batteryTopY - paddingBetweenBottomLogoAndBattery) - (timeBottomY + verticalPaddingBetweenElements))
        val targetWearOSLogoWidth = if( targetWearOSLogoHeight < wearOSLogoHeight ) {
            wearOSLogoWidth * (targetWearOSLogoHeight / wearOSLogoHeight)
        } else {
            wearOSLogoWidth
        }

        val wearOSLogo: Bitmap = ContextCompat.getDrawable(context, R.drawable.ic_wear_os_logo)!!.toBitmap(targetWearOSLogoWidth.toInt(), targetWearOSLogoHeight.toInt())
        val wearOSLogoAmbient: Bitmap = ContextCompat.getDrawable(context, R.drawable.ic_wear_os_logo_ambient)!!.toBitmap(targetWearOSLogoWidth.toInt(), targetWearOSLogoHeight.toInt())

        return ComplicationsDrawingCache(
            wearOSLogo = wearOSLogo,
            wearOSLogoAmbient = wearOSLogoAmbient,
            wearOSLogoRect = Rect(
                (centerX - targetWearOSLogoWidth / 2f).toInt(),
                (timeBottomY + verticalPaddingBetweenElements).toInt(),
                (centerX + targetWearOSLogoWidth / 2f).toInt(),
                (timeBottomY + verticalPaddingBetweenElements + targetWearOSLogoHeight).toInt(),
            ),
            notificationsRect = Rect(
                if (isRound) { (screenWidth / 7f).toInt() } else { context.dpToPx(15) },
                (timeBottomY + verticalPaddingBetweenElements).toInt(),
                if (isRound) { screenWidth - (screenWidth / 7f).toInt() } else { screenWidth - context.dpToPx(15) },
                batteryTopY - verticalPaddingBetweenElements,
            ),
        )
    }

    private fun setScaledSizes(timeSize: Int, dateAndBatterySize: Int) {
        val scaleFactor = fontDisplaySizeToScaleFactor(timeSize, android12Layout = true)
        val dateAndBatteryScaleFactor = fontDisplaySizeToScaleFactor(dateAndBatterySize, android12Layout = true)

        timePaint.textSize = context.resources.getDimension(R.dimen.android_12_time_text_size) * scaleFactor
        val dateSize = context.resources.getDimension(R.dimen.android_12_date_text_size) * dateAndBatteryScaleFactor
        datePaint.textSize = dateSize
        batteryLevelPaint.textSize = context.resources.getDimension(R.dimen.android_12_battery_level_size) * dateAndBatteryScaleFactor
        batteryIconSize = (context.resources.getDimension(R.dimen.android_12_battery_icon_size) * dateAndBatteryScaleFactor).toInt()
    }

    private fun Android12DrawingState.CacheAvailable.draw(
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
        notificationsState: PhoneNotifications.NotificationState?,
    ) {
        val hourText = if( storage.getUse24hTimeFormat()) {
            hourFormatter24H.calendar = calendar
            hourFormatter24H.format(Date(calendar.timeInMillis))
        } else {
            hourFormatter12H.calendar = calendar
            hourFormatter12H.format(Date(calendar.timeInMillis))
        }.let { if (it == "24") "00" else it } // Make sure 24h is converted to 00

        minFormatter.calendar = calendar
        val minText = minFormatter.format(Date(calendar.timeInMillis))

        val hourFirstChar = hourText.substring(0, 1)
        val hourSecondChar = hourText.substring(1, 2)
        val minFirstChar = minText.substring(0, 1)
        val minSecondChar = minText.substring(1, 2)

        val hourFirstCharWidth = Rect().apply {
            timePaint.getTextBounds(hourFirstChar, 0, 1, this)
        }.width()

        val hourSecondCharWidth = Rect().apply {
            timePaint.getTextBounds(hourSecondChar, 0, 1, this)
        }.width()

        val minFirstCharWidth = Rect().apply {
            timePaint.getTextBounds(minFirstChar, 0, 1, this)
        }.width()

        val minSecondCharWidth = Rect().apply {
            timePaint.getTextBounds(minSecondChar, 0, 1, this)
        }.width()

        canvas.drawText(hourFirstChar, timeX + (timeCharWidth - hourFirstCharWidth) / 2.5f, centerY - distanceBetweenHourAndMin + timePaddingY, timePaint)
        canvas.drawText(hourSecondChar, timeX + timeCharWidth + (timeCharWidth - hourSecondCharWidth) / 2.5f, centerY - distanceBetweenHourAndMin + timePaddingY, timePaint)
        canvas.drawText(minFirstChar, timeX + (timeCharWidth - minFirstCharWidth) / 2.5f, centerY + timeHeight + distanceBetweenHourAndMin + timePaddingY, timePaint)
        canvas.drawText(minSecondChar, timeX + timeCharWidth + (timeCharWidth - minSecondCharWidth) / 2.5f, centerY + timeHeight + distanceBetweenHourAndMin + timePaddingY, timePaint)

        complicationsDrawingCache.drawComplications(
            canvas,
            ambient,
            calendar,
            isUserPremium,
            drawWearOsLogo = (!isUserPremium || notificationsState == null) && (!ambient || storage.getShowWearOSLogoInAmbient()),
        )

        if( drawDate ) {
            drawDateAndWeather(
                canvas,
                weatherComplicationData,
                storage.getUseShortDateFormat(),
                isUserPremium,
                calendar,
                datePaint,
                weatherIconPaint,
            )
        }

        if( drawSecondsRing && !ambient ) {
            drawSecondRing(canvas, calendar, secondsRingPaint)
        }

        if( isUserPremium && (drawBattery || drawPhoneBattery) && (!ambient || !storage.hideBatteryInAmbient()) ) {
            drawBattery(
                canvas,
                batteryLevelPaint,
                batteryIconPaint,
                distanceBetweenPhoneAndWatchBattery,
                drawBattery,
                drawPhoneBattery,
                calendar,
                batteryComplicationData,
                phoneBatteryStatus
            )
        }

        if (isUserPremium && notificationsState != null && (!ambient || storage.getShowNotificationsInAmbient())) {
            drawNotifications(canvas, notificationsPaint, notificationsState)
        }
    }

    private fun ComplicationsDrawingCache.drawComplications(
        canvas: Canvas,
        ambient: Boolean,
        calendar: Calendar,
        isUserPremium: Boolean,
        drawWearOsLogo: Boolean,
    ) {
        if( isUserPremium && (storage.showComplicationsInAmbientMode() || !ambient) ) {
            ACTIVE_COMPLICATIONS.forEach { complicationId ->
                val complicationDrawable = complicationDrawableSparseArray[complicationId]
                complicationDrawable.draw(canvas, calendar.timeInMillis)
            }
        }

        if( storage.showWearOSLogo() && drawWearOsLogo ) {
            val wearOsImage = if( ambient ) { wearOSLogoAmbient } else { wearOSLogo }
            canvas.drawBitmap(wearOsImage, null, wearOSLogoRect, wearOSLogoPaint)
        }
    }

    companion object {
        val ACTIVE_COMPLICATIONS = intArrayOf(
            PixelMinimalWatchFace.ANDROID_12_TOP_LEFT_COMPLICATION_ID,
            PixelMinimalWatchFace.ANDROID_12_TOP_RIGHT_COMPLICATION_ID,
            PixelMinimalWatchFace.ANDROID_12_BOTTOM_LEFT_COMPLICATION_ID,
            PixelMinimalWatchFace.ANDROID_12_BOTTOM_RIGHT_COMPLICATION_ID,
        )

        private const val TAG = "PixelMinimalWatchFace/Android12Drawer"
    }
}