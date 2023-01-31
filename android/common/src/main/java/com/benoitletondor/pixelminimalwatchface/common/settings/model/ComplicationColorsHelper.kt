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