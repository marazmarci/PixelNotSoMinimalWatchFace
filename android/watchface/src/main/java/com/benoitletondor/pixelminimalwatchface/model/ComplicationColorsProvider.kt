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
package com.benoitletondor.pixelminimalwatchface.model

import android.graphics.Color
import androidx.annotation.ColorInt

object ComplicationColorsProvider {
    const val defaultColorName = "Default"

    private val wearOSBlueColor = Color.parseColor("#5484f8")
    private val wearOSYellowColor = Color.parseColor("#F2BD00")
    private val wearOSRedColor = Color.parseColor("#da482f")
    private val wearOSGreenColor = Color.parseColor("#54A74C")
    val defaultGrey = Color.parseColor("#AAAAAA")

    private val wearOSColors = linkedMapOf(
        wearOSBlueColor to "WearOS blue",
        wearOSYellowColor to "WearOS yellow",
        wearOSRedColor to "WearOS red",
        wearOSGreenColor to "WearOS green",
    )

    private val material1Colors: Map<Int, String> by lazy { linkedMapOf(
        Color.parseColor("#FFFFFF") to "White",
        Color.parseColor("#FFEB3B") to "Yellow",
        Color.parseColor("#FFC107") to "Amber",
        Color.parseColor("#FF9800") to "Orange",
        Color.parseColor("#FF5722") to "Deep orange",
        Color.parseColor("#F44336") to "Red",
        Color.parseColor("#E91E63") to "Pink",
        Color.parseColor("#9C27B0") to "Purple",
        Color.parseColor("#673AB7") to "Deep purple",
        Color.parseColor("#3F51B5") to "Indigo",
        Color.parseColor("#2196F3") to "Blue",
        Color.parseColor("#03A9F4") to "Light blue",
        Color.parseColor("#00BCD4") to "Cyan",
        Color.parseColor("#009688") to "Teal",
        Color.parseColor("#4CAF50") to "Green",
        Color.parseColor("#8BC34A") to "Lime green",
        Color.parseColor("#CDDC39") to "Lime",
        Color.parseColor("#607D8B") to "Blue grey",
        Color.parseColor("#9E9E9E") to "Grey",
        Color.parseColor("#795548") to "Brown",
    ) }

    private val pixelWatchColors: Map<Int, String> by lazy { linkedMapOf(
        Color.parseColor("#949494") to "Graphite",
        Color.parseColor("#E4E4E4") to "Cloud",
        Color.parseColor("#FCF7EB") to "Almond",
        Color.parseColor("#FF7B78") to "Watermelon",
        Color.parseColor("#F99586") to "Coral",
        Color.parseColor("#FFA49F") to "Pomelo",
        Color.parseColor("#FFC1BD") to "Guava",
        Color.parseColor("#FDB78F") to "Peach",
        Color.parseColor("#FFD4B6") to "Champagne",
        Color.parseColor("#D8AB77") to "Chai",
        Color.parseColor("#C4B575") to "Sand",
        Color.parseColor("#F8C67C") to "Honey",
        Color.parseColor("#FFD88A") to "Melon",
        Color.parseColor("#FFE9B9") to "Wheat",
        Color.parseColor("#FFFA86") to "Dandelion",
        Color.parseColor("#FCFFB6") to "Limoncello",
        Color.parseColor("#EBFFC3") to "Lemongrass",
        Color.parseColor("#E6FF7B") to "Lime",
        Color.parseColor("#C7FF81") to "Pear",
        Color.parseColor("#ADF7B9") to "Spearmint",
        Color.parseColor("#77D9A4") to "Fern",
        Color.parseColor("#67887B") to "Forest",
        Color.parseColor("#ABFFDF") to "Mint",
        Color.parseColor("#BEECDB") to "Jade",
        Color.parseColor("#CCE4DF") to "Sage",
        Color.parseColor("#B0E4EC") to "Stream",
        Color.parseColor("#99F3FF") to "Aqua",
        Color.parseColor("#B6DAF1") to "Sky",
        Color.parseColor("#A2C2F7") to "Ocean",
        Color.parseColor("#81ACF4") to "Sapphire",
        Color.parseColor("#AEB4FF") to "Amethyst",
        Color.parseColor("#D6C3FF") to "Lilac",
        Color.parseColor("#E4B0FD") to "Lavender",
        Color.parseColor("#FABBFF") to "Flamingo",
        Color.parseColor("#FFCAED") to "Bubble Gum",
        Color.parseColor("#FFF6E2") to "Milkshake",
        Color.parseColor("#FFCAED") to "Verbena",
        Color.parseColor("#FAE7D5") to "Salmon",
        Color.parseColor("#FFD88A") to "Amber",
        Color.parseColor("#F8EFCD") to "Cantaloupe",
        Color.parseColor("#C4B575") to "Mustard",
        Color.parseColor("#E4E4E4") to "Key Lime",
        Color.parseColor("#C7FF81") to "Spring",
        Color.parseColor("#67887B") to "Avocado",
        Color.parseColor("#E4E4E4") to "Polar",
        Color.parseColor("#A2C2F7") to "Arctic",
        Color.parseColor("#AEB4FF") to "Winter",
        Color.parseColor("#E4B0FD") to "Macaron",
        Color.parseColor("#B6DAF1") to "Sunset",
        Color.parseColor("#CCE4DF") to "Seafoam",
        Color.parseColor("#616161") to "Charcoal",
    ) }

    fun getDefaultComplicationColors(): ComplicationColors {
        return ComplicationColors(
            leftColor = ComplicationColor(wearOSBlueColor, defaultColorName, true),
            middleColor = ComplicationColor(wearOSYellowColor, defaultColorName, true),
            rightColor = ComplicationColor(wearOSRedColor, defaultColorName, true),
            bottomColor = ComplicationColor(wearOSGreenColor, defaultColorName, true),
            android12TopLeftColor = ComplicationColor(wearOSBlueColor, defaultColorName, true),
            android12TopRightColor = ComplicationColor(wearOSRedColor, defaultColorName, true),
            android12BottomLeftColor = ComplicationColor(wearOSYellowColor, defaultColorName, true),
            android12BottomRightColor = ComplicationColor(wearOSGreenColor, defaultColorName, true),
            leftSecondaryColor = ComplicationColor(defaultGrey, defaultColorName, true),
            middleSecondaryColor = ComplicationColor(defaultGrey, defaultColorName, true),
            rightSecondaryColor = ComplicationColor(defaultGrey, defaultColorName, true),
            bottomSecondaryColor = ComplicationColor(defaultGrey, defaultColorName, true),
            android12TopLeftSecondaryColor = ComplicationColor(defaultGrey, defaultColorName, true),
            android12TopRightSecondaryColor = ComplicationColor(defaultGrey, defaultColorName, true),
            android12BottomLeftSecondaryColor = ComplicationColor(defaultGrey, defaultColorName, true),
            android12BottomRightSecondaryColor = ComplicationColor(defaultGrey, defaultColorName, true),
        )
    }

    fun getLabelForColor(@ColorInt color: Int): String {
        return wearOSColors[color] ?: pixelWatchColors[color] ?: material1Colors[color] ?: defaultColorName
    }

    fun getAllComplicationColors(): List<ComplicationColorCategory> {
        val wearOSColors = wearOSColors.map { (color, name) ->
            ComplicationColor(
                color,
                name,
                false
            )
        }
        val pixelWatchColors = pixelWatchColors.map { (color, name) ->
            ComplicationColor(
                color,
                name,
                false
            )
        }
        val material1Colors = material1Colors.map { (color, name) ->
            ComplicationColor(
                color,
                name,
                false
            )
        }

        return listOf(
            ComplicationColorCategory(
                label = "WearOS",
                colors = wearOSColors,
            ),
            ComplicationColorCategory(
                label = "Pixel",
                colors = pixelWatchColors,
            ),
            ComplicationColorCategory(
                label = "Original",
                colors = material1Colors,
            )
        )
    }
}