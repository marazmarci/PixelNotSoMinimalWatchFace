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
package com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchfacecompanion.R

@Composable
fun PhoneSettingComplicationSlot(
    modifier: Modifier,
    complicationLocation: ComplicationLocation,
    color: ComplicationColor,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
    ) {
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_widgets_24),
                contentDescription = when(complicationLocation) {
                    ComplicationLocation.LEFT -> "Left widget"
                    ComplicationLocation.MIDDLE -> "Middle widget"
                    ComplicationLocation.RIGHT -> "Right widget"
                    ComplicationLocation.BOTTOM -> "Bottom widget"
                    ComplicationLocation.ANDROID_12_TOP_LEFT -> "Top left widget"
                    ComplicationLocation.ANDROID_12_TOP_RIGHT -> "Top right widget"
                    ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> "Bottom left widget"
                    ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> "Bottom right widget"
                },
                colorFilter = color.let { ColorFilter.tint(Color(it.color)) },
            )
        }
    }
}