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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.benoitletondor.pixelminimalwatchface.compose.component.RotatoryAwareLazyColumn
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingSectionItem
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColorsProvider

const val ComplicationColorScreenResultKey = "ComplicationColorScreenResultKey"

@Composable
fun ComplicationColorScreen(
    navController: NavHostController,
    defaultColor: ComplicationColor,
) {
    val availableColors = remember { ComplicationColorsProvider.getAllComplicationColors() }

    LaunchedEffect("init") {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.remove<ComplicationColor>(ComplicationColorScreenResultKey)
    }

    RotatoryAwareLazyColumn {
        ColorCategory(
            title = "Default",
            colors = listOf(defaultColor),
            includeTopPadding = false,
            onColorClick = { color ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(ComplicationColorScreenResultKey, color)

                navController.popBackStack()
            }
        )

        availableColors.forEach { colorCategory ->
            ColorCategory(
                title = colorCategory.label,
                colors = colorCategory.colors,
                onColorClick =  { color ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(ComplicationColorScreenResultKey, color)

                    navController.popBackStack()
                }
            )
        }
    }
}

private fun LazyListScope.ColorCategory(
    title: String,
    colors: List<ComplicationColor>,
    includeTopPadding: Boolean = true,
    onColorClick: (ComplicationColor) -> Unit,
) {
    item {
        SettingSectionItem(
            label = title,
            includeTopPadding = includeTopPadding,
        )
    }

    colors.forEach { color ->
        item {
            val callback = remember("colorCallback${color.color}") { { onColorClick(color) } }

            ColorItem(
                color = color,
                onClick = callback,
            )
        }
    }
}

@Composable
private fun ColorItem(
    color: ComplicationColor,
    onClick: () -> Unit,
) {
    Chip(
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = color.label,
                fontWeight = FontWeight.Normal,
            )
        },
        icon = {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .width(40.dp)
                    .height(40.dp)
                    .background(Color(color.color))
            )
        },
        onClick = onClick,
        colors = ChipDefaults.primaryChipColors(
            backgroundColor = MaterialTheme.colors.surface,
        ),
    )
}