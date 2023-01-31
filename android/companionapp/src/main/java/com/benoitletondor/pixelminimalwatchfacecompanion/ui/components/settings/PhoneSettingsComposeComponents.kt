package com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.common.settings.SettingsComposeComponents

class PhoneSettingsComposeComponents : SettingsComposeComponents {

    @Composable
    override fun AbstractPlatformLazyColumn(modifier: Modifier, content: LazyListScope.() -> Unit) {
        LazyColumn(
            modifier = modifier,
            content = content,
        )
    }

    @Composable
    override fun AbstractPlatformText(
        text: String,
        modifier: Modifier,
        color: Color,
        fontSize: TextUnit,
        fontFamily: FontFamily?,
        textAlign: TextAlign?,
        lineHeight: TextUnit
    ) {
        Text(
            text = text,
            modifier = modifier
                .padding(bottom = 10.dp),
            color = color,
            fontSize = if (fontSize == TextUnit.Unspecified) TextUnit.Unspecified else fontSize.times(1.3f),
            fontFamily = fontFamily,
            textAlign = textAlign,
            lineHeight = if (lineHeight == TextUnit.Unspecified) TextUnit.Unspecified else lineHeight.times(1.5f),
        )
    }

    @Composable
    override fun AbstractSettingSectionItem(
        modifier: Modifier,
        label: String,
        includeTopPadding: Boolean,
        includeBottomPadding: Boolean
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            modifier = modifier
                .padding(
                    top = if (includeTopPadding) 22.dp else 0.dp ,
                    bottom = if (includeBottomPadding) 18.dp else 0.dp,
                )
        )
    }

    @Composable
    override fun AbstractSettingToggleChip(
        modifier: Modifier,
        checked: Boolean,
        onCheckedChange: suspend (Boolean) -> Unit,
        label: String,
        secondaryLabel: String?,
        iconDrawable: Int?
    ) {
        PlatformSettingToggleChip(
            modifier = modifier,
            checked = checked,
            onCheckedChange = onCheckedChange,
            label = label,
            secondaryLabel = secondaryLabel,
            iconDrawable = iconDrawable,
        )
    }

    @Composable
    override fun AbstractSettingChip(
        modifier: Modifier,
        onClick: () -> Unit,
        label: String,
        secondaryLabel: String?,
        iconDrawable: Int?
    ) {
        PlatformSettingChip(
            modifier = modifier,
            onClick = onClick,
            label = label,
            secondaryLabel = secondaryLabel,
            iconDrawable = iconDrawable,
        )
    }

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
    ) {
        PlatformSettingSlider(
            iconDrawable = iconDrawable,
            onValueChange = onValueChange,
            value = value,
            title = title,
            modifier = modifier,
            minValue = minValue,
            maxValue = maxValue,
            step = step,
        )
    }

    @Composable
    override fun AbstractSettingComplicationSlot(
        modifier: Modifier,
        complicationLocation: ComplicationLocation,
        color: ComplicationColor,
    ) {
        PhoneSettingComplicationSlot(
            modifier = modifier,
            complicationLocation = complicationLocation,
            color = color,
        )
    }

    @Composable
    override fun SettingComplicationSlotContainer(
        modifier: Modifier,
        onClick: (() -> Unit)?,
        content: @Composable BoxScope.() -> Unit,
    ) {
        PhoneSettingComplicationSlotContainer(
            modifier = modifier,
            onClick = onClick,
            content = content,
        )
    }
}