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

import android.content.ComponentName
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderInfoRetriever
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.helper.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

@Composable
fun WatchSettingComplicationSlot(
    modifier: Modifier = Modifier,
    complicationLocation: ComplicationLocation,
    color: ComplicationColor?,
    iconWidth: Int = 40,
    iconHeight: Int = 40,
) {
    val context = LocalContext.current

    val providerInfoState = remember { mutableStateOf<ComplicationProviderInfo?>(null) }
    val watchFaceComponentName = remember { ComponentName(context, PixelMinimalWatchFace::class.java) }
    val providerInfoRetriever = remember { ProviderInfoRetriever(context, Dispatchers.IO.asExecutor()) }

    fun updateComplication() {
        providerInfoRetriever.retrieveProviderInfo(
            object : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {
                    providerInfoState.value = complicationProviderInfo
                }

                override fun onRetrievalFailed() {
                    super.onRetrievalFailed()
                    Log.e("SettingComplicationSlot", "Error fetching complication provider info")
                }
            },
            watchFaceComponentName,
            PixelMinimalWatchFace.getComplicationId(complicationLocation)
        )
    }

    DisposableEffect("providerInfoRetriever") {
        providerInfoRetriever.init()

        onDispose {
            providerInfoRetriever.release()
        }
    }

    // FIXME : too many redraw?
    updateComplication()

    WatchSettingComplicationSlot(
        modifier = modifier,
        complicationProviderInfo = providerInfoState.value,
        color = color,
        iconWidth = iconWidth,
        iconHeight = iconHeight,
    )
}

@Composable
fun WatchSettingComplicationSlot(
    modifier: Modifier = Modifier,
    complicationProviderInfo: ComplicationProviderInfo?,
    color: ComplicationColor?,
    iconWidth: Int = 40,
    iconHeight: Int = 40,
) {
    val context = LocalContext.current

    val iconDrawable = remember(complicationProviderInfo?.providerIcon) {
        complicationProviderInfo?.providerIcon?.loadDrawable(context)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
    ) {
        if (complicationProviderInfo == null) {
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
                        contentDescription = complicationProviderInfo?.providerName,
                        colorFilter = color?.let { ColorFilter.tint(Color(it.color)) },
                    )
                }
            }
        }
    }
}