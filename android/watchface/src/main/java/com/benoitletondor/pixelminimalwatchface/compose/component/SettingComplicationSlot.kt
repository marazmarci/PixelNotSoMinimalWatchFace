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
package com.benoitletondor.pixelminimalwatchface.compose.component

import android.support.wearable.complications.ComplicationProviderInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.helper.toBitmap
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColor

@Composable
fun SettingComplicationSlot(
    modifier: Modifier = Modifier,
    providerInfo: ComplicationProviderInfo?,
    color: ComplicationColor?,
    onClick : (() -> Unit)?,
    iconWidth: Int = 40,
    iconHeight: Int = 40,
) {
    val context = LocalContext.current
    val iconDrawable = remember(providerInfo?.providerIcon) {
        providerInfo?.providerIcon?.loadDrawable(context)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .let {
                if(onClick != null) {
                    it.clickable(onClick = onClick)
                } else {
                    it
                }
            }
    ) {
        if (providerInfo == null) {
            Image(
                painter = painterResource(id = R.drawable.add_complication),
                contentDescription = "Add widget",
            )
        } else {
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.added_complication),
                    contentDescription = "Widget",
                )

                if (iconDrawable != null) {
                    Image(
                        bitmap = iconDrawable.toBitmap(iconWidth, iconHeight).asImageBitmap(),
                        contentDescription = providerInfo.providerName,
                        colorFilter = color?.let { ColorFilter.tint(Color(it.color)) },
                    )
                }

            }
        }
    }
}