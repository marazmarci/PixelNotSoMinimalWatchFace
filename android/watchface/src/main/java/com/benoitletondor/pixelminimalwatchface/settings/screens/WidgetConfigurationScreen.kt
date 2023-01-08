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

import android.app.Activity
import android.content.ComponentName
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderChooserIntent
import android.support.wearable.complications.ProviderInfoRetriever
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.compose.component.RotatoryAwareLazyColumn
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingComplicationSlot
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColorsProvider
import com.benoitletondor.pixelminimalwatchface.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.model.Storage
import com.benoitletondor.pixelminimalwatchface.settings.navigateToColorSelectionScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch

@Composable
fun WidgetConfigurationScreen(
    storage: Storage,
    navController: NavHostController,
    complicationLocation: ComplicationLocation,
) {
    val context = LocalContext.current

    val complicationColorMutableState = remember { mutableStateOf(when(complicationLocation) {
        ComplicationLocation.LEFT -> storage.getComplicationColors().leftColor
        ComplicationLocation.MIDDLE -> storage.getComplicationColors().middleColor
        ComplicationLocation.RIGHT -> storage.getComplicationColors().rightColor
        ComplicationLocation.BOTTOM -> storage.getComplicationColors().bottomColor
        ComplicationLocation.ANDROID_12_TOP_LEFT -> storage.getComplicationColors().android12TopLeftColor
        ComplicationLocation.ANDROID_12_TOP_RIGHT -> storage.getComplicationColors().android12TopRightColor
        ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> storage.getComplicationColors().android12BottomLeftColor
        ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> storage.getComplicationColors().android12BottomRightColor
    }) }

    val complicationSecondaryColorMutableState = remember { mutableStateOf(when(complicationLocation) {
        ComplicationLocation.LEFT -> storage.getComplicationColors().leftSecondaryColor
        ComplicationLocation.MIDDLE -> storage.getComplicationColors().middleSecondaryColor
        ComplicationLocation.RIGHT -> storage.getComplicationColors().rightSecondaryColor
        ComplicationLocation.BOTTOM -> storage.getComplicationColors().bottomSecondaryColor
        ComplicationLocation.ANDROID_12_TOP_LEFT -> storage.getComplicationColors().android12TopLeftSecondaryColor
        ComplicationLocation.ANDROID_12_TOP_RIGHT -> storage.getComplicationColors().android12TopRightSecondaryColor
        ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> storage.getComplicationColors().android12BottomLeftSecondaryColor
        ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> storage.getComplicationColors().android12BottomRightSecondaryColor
    }) }

    val title = remember { when(complicationLocation) {
        ComplicationLocation.LEFT -> context.getString(R.string.config_left_complication)
        ComplicationLocation.MIDDLE -> context.getString(R.string.config_middle_complication)
        ComplicationLocation.RIGHT -> context.getString(R.string.config_right_complication)
        ComplicationLocation.BOTTOM -> context.getString(R.string.config_bottom_complication)
        ComplicationLocation.ANDROID_12_TOP_LEFT -> context.getString(R.string.config_android_12_top_left_complication)
        ComplicationLocation.ANDROID_12_TOP_RIGHT -> context.getString(R.string.config_android_12_top_right_complication)
        ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> context.getString(R.string.config_android_12_bottom_left_complication)
        ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> context.getString(R.string.config_android_12_bottom_right_complication)
    } }

    val complicationProviderMutableState = remember { mutableStateOf<ComplicationProviderInfo?>(null) }

    val providerInfoRetriever = remember { ProviderInfoRetriever(context, Dispatchers.IO.asExecutor()) }
    val watchFaceComponentName = remember { ComponentName(context, PixelMinimalWatchFace::class.java) }

    DisposableEffect("providerInfoRetrieverWidget") {
        providerInfoRetriever.init()

        providerInfoRetriever.retrieveProviderInfo(
            object : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {
                    complicationProviderMutableState.value = complicationProviderInfo
                }
            },
            watchFaceComponentName,
            PixelMinimalWatchFace.getComplicationId(complicationLocation)
        )

        onDispose {
            providerInfoRetriever.release()
        }
    }

    RotatoryAwareLazyColumn {
        item {
            Text(
                text = title,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        item {
            val widgetSelectionActivityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if (activityResult.resultCode == Activity.RESULT_OK) {
                    val complicationProviderInfo: ComplicationProviderInfo? =
                        activityResult.data?.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO)

                    complicationProviderMutableState.value = complicationProviderInfo
                }
            }

            Chip(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 70.dp),
                label = {
                    Text(
                        text = "Widget",
                        fontWeight = FontWeight.Normal,
                    )
                },
                secondaryLabel = {
                    Text(
                        text = if (complicationProviderMutableState.value != null) {
                            "Tap to change/remove"
                        } else {
                            "Tap to setup"
                        },
                        fontWeight = FontWeight.Normal,
                        color = Color.LightGray,
                    )
                },
                icon = {
                    SettingComplicationSlot(
                        providerInfo = complicationProviderMutableState.value,
                        color = null,
                        onClick = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .width(40.dp)
                            .height(40.dp)
                    )
                },
                onClick = {
                    widgetSelectionActivityLauncher.launch(
                        ComplicationHelperActivity.createProviderChooserHelperIntent(
                            context,
                            watchFaceComponentName,
                            PixelMinimalWatchFace.getComplicationId(complicationLocation),
                            *PixelMinimalWatchFace.getSupportedComplicationTypes(complicationLocation)
                        )
                    )
                },
                colors = ChipDefaults.primaryChipColors(
                    backgroundColor = MaterialTheme.colors.surface,
                ),
            )
        }

        if (complicationProviderMutableState.value != null) {
            item {
                val activity = LocalContext.current as ComponentActivity

                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "Accent color",
                            fontWeight = FontWeight.Normal,
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = "Tap to change",
                            fontWeight = FontWeight.Normal,
                            color = Color.LightGray,
                        )
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .width(40.dp)
                                .height(40.dp)
                                .background(Color(complicationColorMutableState.value.color))
                        )
                    },
                    onClick = {
                        val defaultColor = when(complicationLocation) {
                            ComplicationLocation.LEFT -> ComplicationColorsProvider.getDefaultComplicationColors().leftColor
                            ComplicationLocation.MIDDLE -> ComplicationColorsProvider.getDefaultComplicationColors().middleColor
                            ComplicationLocation.RIGHT -> ComplicationColorsProvider.getDefaultComplicationColors().rightColor
                            ComplicationLocation.BOTTOM -> ComplicationColorsProvider.getDefaultComplicationColors().bottomColor
                            ComplicationLocation.ANDROID_12_TOP_LEFT -> ComplicationColorsProvider.getDefaultComplicationColors().android12TopLeftColor
                            ComplicationLocation.ANDROID_12_TOP_RIGHT -> ComplicationColorsProvider.getDefaultComplicationColors().android12TopRightColor
                            ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> ComplicationColorsProvider.getDefaultComplicationColors().android12BottomLeftColor
                            ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> ComplicationColorsProvider.getDefaultComplicationColors().android12BottomRightColor
                        }

                        activity.lifecycleScope.launch {
                            val selectedColor = navController.navigateToColorSelectionScreen(defaultColor.color)
                            if (selectedColor != null) {
                                val colors = storage.getComplicationColors()
                                storage.setComplicationColors(when(complicationLocation) {
                                    ComplicationLocation.LEFT -> colors.copy(leftColor = selectedColor)
                                    ComplicationLocation.MIDDLE -> colors.copy(middleColor = selectedColor)
                                    ComplicationLocation.RIGHT -> colors.copy(rightColor = selectedColor)
                                    ComplicationLocation.BOTTOM -> colors.copy(bottomColor = selectedColor)
                                    ComplicationLocation.ANDROID_12_TOP_LEFT -> colors.copy(android12TopLeftColor = selectedColor)
                                    ComplicationLocation.ANDROID_12_TOP_RIGHT -> colors.copy(android12TopRightColor = selectedColor)
                                    ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> colors.copy(android12BottomLeftColor = selectedColor)
                                    ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> colors.copy(android12BottomRightColor = selectedColor)
                                })

                                complicationColorMutableState.value = selectedColor
                            }
                        }
                    },
                    colors = ChipDefaults.primaryChipColors(
                        backgroundColor = MaterialTheme.colors.surface,
                    ),
                )
            }

            item {
                val activity = LocalContext.current as ComponentActivity

                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "Secondary color",
                            fontWeight = FontWeight.Normal,
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = "Tap to change",
                            fontWeight = FontWeight.Normal,
                            color = Color.LightGray,
                        )
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .width(40.dp)
                                .height(40.dp)
                                .background(Color(complicationSecondaryColorMutableState.value.color))
                        )
                    },
                    onClick = {
                        val defaultColor = when(complicationLocation) {
                            ComplicationLocation.LEFT -> ComplicationColorsProvider.getDefaultComplicationColors().leftSecondaryColor
                            ComplicationLocation.MIDDLE -> ComplicationColorsProvider.getDefaultComplicationColors().middleSecondaryColor
                            ComplicationLocation.RIGHT -> ComplicationColorsProvider.getDefaultComplicationColors().rightSecondaryColor
                            ComplicationLocation.BOTTOM -> ComplicationColorsProvider.getDefaultComplicationColors().bottomSecondaryColor
                            ComplicationLocation.ANDROID_12_TOP_LEFT -> ComplicationColorsProvider.getDefaultComplicationColors().android12TopLeftSecondaryColor
                            ComplicationLocation.ANDROID_12_TOP_RIGHT -> ComplicationColorsProvider.getDefaultComplicationColors().android12TopRightSecondaryColor
                            ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> ComplicationColorsProvider.getDefaultComplicationColors().android12BottomLeftSecondaryColor
                            ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> ComplicationColorsProvider.getDefaultComplicationColors().android12BottomRightSecondaryColor
                        }
                        activity.lifecycleScope.launch {
                            val selectedColor = navController.navigateToColorSelectionScreen(defaultColor.color)
                            if (selectedColor != null) {
                                val colors = storage.getComplicationColors()
                                storage.setComplicationColors(when(complicationLocation) {
                                    ComplicationLocation.LEFT -> colors.copy(leftSecondaryColor = selectedColor)
                                    ComplicationLocation.MIDDLE -> colors.copy(middleSecondaryColor = selectedColor)
                                    ComplicationLocation.RIGHT -> colors.copy(rightSecondaryColor = selectedColor)
                                    ComplicationLocation.BOTTOM -> colors.copy(bottomSecondaryColor = selectedColor)
                                    ComplicationLocation.ANDROID_12_TOP_LEFT -> colors.copy(android12TopLeftSecondaryColor = selectedColor)
                                    ComplicationLocation.ANDROID_12_TOP_RIGHT -> colors.copy(android12TopRightSecondaryColor = selectedColor)
                                    ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> colors.copy(android12BottomLeftSecondaryColor = selectedColor)
                                    ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> colors.copy(android12BottomRightSecondaryColor = selectedColor)
                                })

                                complicationSecondaryColorMutableState.value = selectedColor
                            }
                        }
                    },
                    colors = ChipDefaults.primaryChipColors(
                        backgroundColor = MaterialTheme.colors.surface,
                    ),
                )
            }
        }
    }
}