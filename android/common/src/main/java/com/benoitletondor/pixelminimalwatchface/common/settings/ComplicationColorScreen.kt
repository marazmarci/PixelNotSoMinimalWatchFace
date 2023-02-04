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

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColorCategory
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColorsProvider
import com.benoitletondor.pixelminimalwatchface.common.settings.model.Platform
import kotlinx.coroutines.flow.StateFlow

const val ComplicationColorScreenResultKey = "ComplicationColorScreenResultKey"

interface ComplicationColorScreen {

    @Composable
    fun ColorChip(
        color: ComplicationColor,
        onClick: () -> Unit,
    )

    fun getPlatformCategories(): StateFlow<List<ComplicationColorCategory>>

    fun onPlatformColorClicked(color: ComplicationColor)

    @Composable
    fun Screen(
        modifier: Modifier = Modifier,
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
        defaultColor: ComplicationColor,
    ) {
        val availableColors = remember { ComplicationColorsProvider.getAllComplicationColors() }
        val platformColors by getPlatformCategories().collectAsState()

        LaunchedEffect("init") {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.remove<ComplicationColor>(ComplicationColorScreenResultKey)
        }

        composeComponents.PlatformLazyColumn(
            modifier = modifier,
        ){
            ColorCategory(
                composeComponents = composeComponents,
                title = "Default",
                colors = listOf(defaultColor),
                includeTopPadding = false,
                onColorClick =  { color -> navController.selectColorAndNavigateBack(color) }
            )

            platformColors.forEach { colorCategory ->
                ColorCategory(
                    composeComponents = composeComponents,
                    title = colorCategory.label,
                    colors = colorCategory.colors,
                    onColorClick = { onPlatformColorClicked(it) },
                )
            }

            availableColors.forEach { colorCategory ->
                ColorCategory(
                    composeComponents = composeComponents,
                    title = colorCategory.label,
                    colors = colorCategory.colors,
                    onColorClick =  { color -> navController.selectColorAndNavigateBack(color) }
                )
            }
        }
    }

    private fun LazyListScope.ColorCategory(
        composeComponents: SettingsComposeComponents,
        title: String,
        colors: List<ComplicationColor>,
        includeTopPadding: Boolean = true,
        onColorClick: (ComplicationColor) -> Unit,
    ) {
        item {
            composeComponents.SettingSectionItem(
                label = title,
                includeTopPadding = includeTopPadding,
            )
        }

        colors.forEach { color ->
            item {
                ColorChip(
                    color = color,
                    onClick = { onColorClick(color) },
                )
            }
        }
    }

    fun NavController.selectColorAndNavigateBack(color: ComplicationColor) {
        previousBackStackEntry
            ?.savedStateHandle
            ?.set(ComplicationColorScreenResultKey, color)

        popBackStack()
    }
}