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