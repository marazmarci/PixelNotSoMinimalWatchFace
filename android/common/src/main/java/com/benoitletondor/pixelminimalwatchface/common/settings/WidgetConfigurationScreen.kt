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
package com.benoitletondor.pixelminimalwatchface.common.settings

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColorsProvider
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.common.settings.model.Platform
import com.benoitletondor.pixelminimalwatchface.common.settings.model.getPrimaryColorForLocation
import com.benoitletondor.pixelminimalwatchface.common.settings.model.getSecondaryColorForLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface WidgetConfigurationScreen {

    val showTitleInScreen: Boolean

    fun title(location: ComplicationLocation): String = when(location) {
        ComplicationLocation.LEFT -> "Left widget settings"
        ComplicationLocation.MIDDLE -> "Middle widget settings"
        ComplicationLocation.RIGHT -> "Right widget settings"
        ComplicationLocation.BOTTOM -> "Bottom widget settings"
        ComplicationLocation.ANDROID_12_TOP_LEFT -> "Top left widget settings"
        ComplicationLocation.ANDROID_12_TOP_RIGHT -> "Top right widget settings"
        ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> "Bottom left widget settings"
        ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> "Bottom right widget settings"
    }

    @Composable
    fun WidgetChip(
        modifier: Modifier,
        complicationLocation: ComplicationLocation,
    )

    @Composable
    fun ColorChip(
        modifier: Modifier,
        color: ComplicationColor,
        label: String,
        secondaryLabel: String,
        onClick: () -> Unit,
    )

    @Composable
    fun Screen(
        modifier: Modifier,
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
        complicationLocation: ComplicationLocation,
    ) {
        val complicationColorMutableState = remember {
            platform.watchComplicationColors()
                .map { it.getPrimaryColorForLocation(complicationLocation) }
        }.collectAsState(platform.getComplicationColors().getPrimaryColorForLocation(complicationLocation))

        val complicationSecondaryColorMutableState = remember {
            platform.watchComplicationColors()
                .map { it.getSecondaryColorForLocation(complicationLocation) }
        }.collectAsState(platform.getComplicationColors().getSecondaryColorForLocation(complicationLocation))

        composeComponents.PlatformLazyColumn(
            modifier = Modifier,
        ) {
            if (showTitleInScreen) {
                item("title") {
                    val title = remember { title(complicationLocation) }
                    composeComponents.AbstractPlatformText(
                        text = title,
                        modifier = Modifier.padding(bottom = 6.dp),
                        color = Color.Unspecified,
                        fontSize = TextUnit.Unspecified,
                        fontFamily = null,
                        textAlign = null,
                        lineHeight = TextUnit.Unspecified,
                    )
                }
            }

            item("widget") {
                WidgetChip(
                    modifier = Modifier,
                    complicationLocation = complicationLocation,
                )
            }

            item("primaryColor") {
                val activity = LocalContext.current as ComponentActivity

                ColorChip(
                    modifier = Modifier,
                    color = complicationColorMutableState.value,
                    label = "Accent color",
                    secondaryLabel = "Tap to change",
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
                            val selectedColor = platform.startColorSelectionScreen(navController, defaultColor.color)
                            if (selectedColor != null) {
                                val colors = platform.getComplicationColors()

                                try {
                                    platform.setComplicationColors(when(complicationLocation) {
                                        ComplicationLocation.LEFT -> colors.copy(leftColor = selectedColor)
                                        ComplicationLocation.MIDDLE -> colors.copy(middleColor = selectedColor)
                                        ComplicationLocation.RIGHT -> colors.copy(rightColor = selectedColor)
                                        ComplicationLocation.BOTTOM -> colors.copy(bottomColor = selectedColor)
                                        ComplicationLocation.ANDROID_12_TOP_LEFT -> colors.copy(android12TopLeftColor = selectedColor)
                                        ComplicationLocation.ANDROID_12_TOP_RIGHT -> colors.copy(android12TopRightColor = selectedColor)
                                        ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> colors.copy(android12BottomLeftColor = selectedColor)
                                        ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> colors.copy(android12BottomRightColor = selectedColor)
                                    })
                                } catch (e: Exception) {
                                    if (e is CancellationException) { throw e }

                                    Log.e(TAG, "Error while setting widget primary color", e)
                                    Toast.makeText(activity, "Unable to sync widget color", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                )
            }

            item("Secondary color") {
                val activity = LocalContext.current as ComponentActivity

                ColorChip(
                    modifier = Modifier,
                    color = complicationSecondaryColorMutableState.value,
                    label = "Secondary color",
                    secondaryLabel = "Tap to change",
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
                            val selectedColor = platform.startColorSelectionScreen(navController, defaultColor.color)
                            if (selectedColor != null) {
                                val colors = platform.getComplicationColors()

                                try {
                                    platform.setComplicationColors(when(complicationLocation) {
                                        ComplicationLocation.LEFT -> colors.copy(leftSecondaryColor = selectedColor)
                                        ComplicationLocation.MIDDLE -> colors.copy(middleSecondaryColor = selectedColor)
                                        ComplicationLocation.RIGHT -> colors.copy(rightSecondaryColor = selectedColor)
                                        ComplicationLocation.BOTTOM -> colors.copy(bottomSecondaryColor = selectedColor)
                                        ComplicationLocation.ANDROID_12_TOP_LEFT -> colors.copy(android12TopLeftSecondaryColor = selectedColor)
                                        ComplicationLocation.ANDROID_12_TOP_RIGHT -> colors.copy(android12TopRightSecondaryColor = selectedColor)
                                        ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> colors.copy(android12BottomLeftSecondaryColor = selectedColor)
                                        ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> colors.copy(android12BottomRightSecondaryColor = selectedColor)
                                    })
                                } catch (e: Exception) {
                                    if (e is CancellationException) { throw e }

                                    Log.e(TAG, "Error while setting widget secondary color", e)
                                    Toast.makeText(activity, "Unable to sync widget color", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                )
            }
        }
    }

    companion object {
        private const val TAG = "WidgetConfigurationScreen"
    }
}
