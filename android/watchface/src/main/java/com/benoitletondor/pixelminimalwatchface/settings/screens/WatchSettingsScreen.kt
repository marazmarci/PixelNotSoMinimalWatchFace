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
package com.benoitletondor.pixelminimalwatchface.settings.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.common.settings.SettingsScreen
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingChip
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingSectionItem
import com.benoitletondor.pixelminimalwatchface.model.WatchPlatform

class WatchSettingsScreen(
    private val watchPlatform: WatchPlatform,
) : SettingsScreen {

    override val includeHeader: Boolean = true
    override val includeFooter: Boolean = true
    override val weatherTempScaleDisclaimer: String = "Temperature scale (°F or °C) is controlled by the Weather app."

    override fun LazyListScope.BecomePremiumSection() {
        item(key = "PremiumSection") { SettingSectionItem(label = "Premium features") }

        item(key = "PremiumCompanion") {
            val activity = LocalContext.current as ComponentActivity

            Column {
                Text(
                    text = "To setup widgets, display weather, battery indicators and notification icons you have to become a premium user.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillParentMaxWidth(),
                )

                Text(
                    text = "You can buy it from the phone companion app and sync it with your watch to setup premium features right here.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillParentMaxWidth(),
                )

                SettingChip(
                    label ="Become premium",
                    onClick = {
                        watchPlatform.openAppOnPhone(activity)
                    },
                    iconDrawable = R.drawable.ic_baseline_stars_24,
                    modifier = Modifier.padding(top = 6.dp),
                )

                Text(
                    text = "Already bought premium? Sync it from the phone app using the \"Troubleshoot\" button.",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(top = 10.dp),
                )
            }
        }
    }
}