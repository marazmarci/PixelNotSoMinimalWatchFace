package com.benoitletondor.pixelminimalwatchface.helper

import androidx.annotation.ColorInt
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColors

@ColorInt
fun ComplicationColors.getPrimaryColorForComplicationId(complicationId: Int): Int = when (complicationId) {
    PixelMinimalWatchFace.LEFT_COMPLICATION_ID -> { leftColor.color }
    PixelMinimalWatchFace.MIDDLE_COMPLICATION_ID -> { middleColor.color }
    PixelMinimalWatchFace.BOTTOM_COMPLICATION_ID -> { bottomColor.color }
    PixelMinimalWatchFace.RIGHT_COMPLICATION_ID -> { rightColor.color }
    PixelMinimalWatchFace.ANDROID_12_TOP_LEFT_COMPLICATION_ID -> { android12TopLeftColor.color }
    PixelMinimalWatchFace.ANDROID_12_TOP_RIGHT_COMPLICATION_ID -> { android12TopRightColor.color }
    PixelMinimalWatchFace.ANDROID_12_BOTTOM_LEFT_COMPLICATION_ID -> { android12BottomLeftColor.color }
    PixelMinimalWatchFace.ANDROID_12_BOTTOM_RIGHT_COMPLICATION_ID -> { android12BottomRightColor.color }
    else -> { rightColor.color }
}

@ColorInt
fun ComplicationColors.getSecondaryColorForComplicationId(complicationId: Int): Int = when (complicationId) {
    PixelMinimalWatchFace.LEFT_COMPLICATION_ID -> { leftSecondaryColor.color }
    PixelMinimalWatchFace.MIDDLE_COMPLICATION_ID -> { middleSecondaryColor.color }
    PixelMinimalWatchFace.BOTTOM_COMPLICATION_ID -> { bottomSecondaryColor.color }
    PixelMinimalWatchFace.RIGHT_COMPLICATION_ID -> { rightSecondaryColor.color }
    PixelMinimalWatchFace.ANDROID_12_TOP_LEFT_COMPLICATION_ID -> { android12TopLeftSecondaryColor.color }
    PixelMinimalWatchFace.ANDROID_12_TOP_RIGHT_COMPLICATION_ID -> { android12TopRightSecondaryColor.color }
    PixelMinimalWatchFace.ANDROID_12_BOTTOM_LEFT_COMPLICATION_ID -> { android12BottomLeftSecondaryColor.color }
    PixelMinimalWatchFace.ANDROID_12_BOTTOM_RIGHT_COMPLICATION_ID -> { android12BottomRightSecondaryColor.color }
    else -> { rightSecondaryColor.color }
}