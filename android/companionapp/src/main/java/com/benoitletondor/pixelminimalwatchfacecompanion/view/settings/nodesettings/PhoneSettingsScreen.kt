package com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings

import androidx.compose.foundation.lazy.LazyListScope
import com.benoitletondor.pixelminimalwatchface.common.settings.SettingsScreen

class PhoneSettingsScreen : SettingsScreen {
    override val includeHeader: Boolean = false
    override val includeFooter: Boolean = false
    override val weatherTempScaleDisclaimer: String = "Temperature scale (°F or °C) is controlled by the Weather app on your watch (not phone!)."

    override fun LazyListScope.BecomePremiumSection() = Unit
}