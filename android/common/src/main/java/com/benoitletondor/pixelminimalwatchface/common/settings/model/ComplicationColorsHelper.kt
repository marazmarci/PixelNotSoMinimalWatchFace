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

fun ComplicationColors.getPrimaryColorForLocation(complicationLocation: ComplicationLocation): ComplicationColor = when (complicationLocation) {
    ComplicationLocation.LEFT -> { leftColor }
    ComplicationLocation.MIDDLE -> { middleColor }
    ComplicationLocation.BOTTOM -> { bottomColor }
    ComplicationLocation.RIGHT -> { rightColor }
    ComplicationLocation.ANDROID_12_TOP_LEFT -> { android12TopLeftColor }
    ComplicationLocation.ANDROID_12_TOP_RIGHT -> { android12TopRightColor }
    ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> { android12BottomLeftColor }
    ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> { android12BottomRightColor }
}

fun ComplicationColors.getSecondaryColorForLocation(complicationLocation: ComplicationLocation): ComplicationColor = when (complicationLocation) {
    ComplicationLocation.LEFT -> { leftSecondaryColor }
    ComplicationLocation.MIDDLE -> { middleSecondaryColor }
    ComplicationLocation.BOTTOM -> { bottomSecondaryColor }
    ComplicationLocation.RIGHT -> { rightSecondaryColor }
    ComplicationLocation.ANDROID_12_TOP_LEFT -> { android12TopLeftSecondaryColor }
    ComplicationLocation.ANDROID_12_TOP_RIGHT -> { android12TopRightSecondaryColor }
    ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> { android12BottomLeftSecondaryColor }
    ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> { android12BottomRightSecondaryColor }
}