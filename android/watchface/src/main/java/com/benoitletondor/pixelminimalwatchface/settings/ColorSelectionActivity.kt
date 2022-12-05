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
package com.benoitletondor.pixelminimalwatchface.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.benoitletondor.pixelminimalwatchface.compose.WearTheme
import com.benoitletondor.pixelminimalwatchface.compose.component.RotatoryAwareLazyColumn
import com.benoitletondor.pixelminimalwatchface.compose.component.SettingSectionItem
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColorsProvider

class ColorSelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultColor = intent.getParcelableExtra<ComplicationColor>(EXTRA_DEFAULT_COLOR)!!
        val availableColors = ComplicationColorsProvider.getAllComplicationColors()

        setContent {
            WearTheme {
                RotatoryAwareLazyColumn {
                    ColorCategory(
                        title = "Default",
                        colors = listOf(defaultColor),
                        includeTopPadding = false,
                        onColorClick = { color ->
                            setResult(RESULT_OK, Intent().apply {
                                putExtra(RESULT_SELECTED_COLOR, color)
                            })

                            finish()
                        }
                    )

                    availableColors.forEach { colorCategory ->
                        ColorCategory(
                            title = colorCategory.label,
                            colors = colorCategory.colors,
                            onColorClick =  { color ->
                                setResult(RESULT_OK, Intent().apply {
                                    putExtra(RESULT_SELECTED_COLOR, color)
                                })

                                finish()
                            }
                        )
                    }
                }
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

    companion object {
        const val RESULT_SELECTED_COLOR = "resultSelectedColor"

        private const val EXTRA_DEFAULT_COLOR = "extra:defaultColor"

        fun createIntent(context: Context, defaultColor: ComplicationColor) = Intent(context, ColorSelectionActivity::class.java).apply {
            putExtra(EXTRA_DEFAULT_COLOR, defaultColor)
        }
    }
}