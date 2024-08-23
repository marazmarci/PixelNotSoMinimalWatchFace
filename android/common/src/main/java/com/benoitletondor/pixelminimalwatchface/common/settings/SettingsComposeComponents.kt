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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation

interface SettingsComposeComponents {
    @Composable
    fun AbstractPlatformLazyColumn(
        modifier: Modifier,
        content: LazyListScope.() -> Unit,
    )

    @Composable
    fun PlatformLazyColumn(
        modifier: Modifier,
        content: LazyListScope.() -> Unit,
    ) {
        AbstractPlatformLazyColumn(
            modifier = modifier,
            content = content,
        )
    }

    @Composable
    fun AbstractPlatformText(
        text: String,
        modifier: Modifier,
        color: Color,
        fontSize: TextUnit,
        fontFamily: FontFamily?,
        textAlign: TextAlign?,
        lineHeight: TextUnit,
    )

    @Composable
    fun AbstractSettingSectionItem(
        modifier: Modifier,
        label: String,
        includeTopPadding: Boolean,
        includeBottomPadding: Boolean,
    )

    @Composable
    fun AbstractSettingToggleChip(
        modifier: Modifier,
        checked: Boolean,
        onCheckedChange: suspend (Boolean) -> Unit,
        label: String,
        secondaryLabel: String?,
        @DrawableRes iconDrawable: Int?,
    )

    @Composable
    fun AbstractSettingChip(
        modifier: Modifier,
        onClick: () -> Unit,
        label: String,
        secondaryLabel: String?,
        @DrawableRes iconDrawable: Int?,
    )

    @Composable
    fun AbstractSettingSlider(
        @DrawableRes iconDrawable: Int,
        onValueChange: suspend (Int) -> Unit,
        value: Int,
        title: String,
        modifier: Modifier,
        minValue: Int,
        maxValue: Int,
        step: Int,
    )

    @Composable
    fun AbstractSettingComplicationSlot(
        modifier: Modifier,
        complicationLocation: ComplicationLocation,
        color: ComplicationColor,
    )

    @Composable
    fun SettingComplicationSlot(
        modifier: Modifier,
        complicationLocation: ComplicationLocation,
        color: ComplicationColor,
    ) {
        AbstractSettingComplicationSlot(
            modifier = modifier,
            complicationLocation = complicationLocation,
            color = color,
        )
    }

    @Composable
    fun SettingComplicationSlotContainer(
        modifier: Modifier,
        onClick: (() -> Unit)?,
        content: @Composable BoxScope.() -> Unit,
    )
}
