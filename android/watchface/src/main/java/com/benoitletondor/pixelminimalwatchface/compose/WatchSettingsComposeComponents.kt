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
package com.benoitletondor.pixelminimalwatchface.compose

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.wear.compose.material.Text
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.common.settings.SettingsComposeComponents
import com.benoitletondor.pixelminimalwatchface.compose.component.RotatoryAwareLazyColumn
import com.benoitletondor.pixelminimalwatchface.compose.component.WatchSettingComplicationSlot
import com.benoitletondor.pixelminimalwatchface.compose.component.WatchSettingComplicationSlotContainer

class WatchSettingsComposeComponents : SettingsComposeComponents {
    @Composable
    override fun AbstractPlatformLazyColumn(modifier: Modifier, content: LazyListScope.() -> Unit) = RotatoryAwareLazyColumn(
        modifier = modifier,
        content = content,
    )

    @Composable
    override fun AbstractPlatformText(
        text: String,
        modifier: Modifier,
        color: Color,
        fontSize: TextUnit,
        fontFamily: FontFamily?,
        textAlign: TextAlign?,
        lineHeight: TextUnit
    ) = Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        textAlign = textAlign,
        lineHeight = lineHeight,
    )

    @Composable
    override fun AbstractSettingSectionItem(
        modifier: Modifier,
        label: String,
        includeTopPadding: Boolean,
        includeBottomPadding: Boolean
    ) = com.benoitletondor.pixelminimalwatchface.compose.component.SettingSectionItem(
        modifier = modifier,
        label = label,
        includeTopPadding = includeTopPadding,
        includeBottomPadding = includeBottomPadding,
    )

    @Composable
    override fun AbstractSettingToggleChip(
        modifier: Modifier,
        checked: Boolean,
        onCheckedChange: suspend (Boolean) -> Unit,
        label: String,
        secondaryLabel: String?,
        iconDrawable: Int?
    ) = com.benoitletondor.pixelminimalwatchface.compose.component.SettingToggleChip(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange,
        label = label,
        secondaryLabel = secondaryLabel,
        iconDrawable = iconDrawable,
    )

    @Composable
    override fun AbstractSettingChip(
        modifier: Modifier,
        onClick: () -> Unit,
        label: String,
        secondaryLabel: String?,
        iconDrawable: Int?
    ) = com.benoitletondor.pixelminimalwatchface.compose.component.SettingChip(
        modifier = modifier,
        onClick = onClick,
        label = label,
        secondaryLabel = secondaryLabel,
        iconDrawable = iconDrawable,
    )

    @Composable
    override fun AbstractSettingSlider(
        iconDrawable: Int,
        onValueChange: suspend (Int) -> Unit,
        value: Int,
        title: String,
        modifier: Modifier,
        minValue: Int,
        maxValue: Int,
        step: Int
    ) = com.benoitletondor.pixelminimalwatchface.compose.component.SettingSlider(
        iconDrawable = iconDrawable,
        onValueChange = onValueChange,
        value = value,
        title = title,
        modifier = modifier,
        minValue = minValue,
        maxValue = maxValue,
        step = step,
    )

    @Composable
    override fun AbstractSettingComplicationSlot(
        modifier: Modifier,
        complicationLocation: ComplicationLocation,
        color: ComplicationColor,
    ) {
        WatchSettingComplicationSlot(
            modifier = modifier,
            complicationLocation = complicationLocation,
            color = color,
        )
    }

    @Composable
    override fun SettingComplicationSlotContainer(
        modifier: Modifier,
        onClick: (() -> Unit)?,
        content: @Composable BoxScope.() -> Unit
    ) {
        WatchSettingComplicationSlotContainer(
            modifier = modifier,
            onClick = onClick,
            content = content,
        )
    }
}