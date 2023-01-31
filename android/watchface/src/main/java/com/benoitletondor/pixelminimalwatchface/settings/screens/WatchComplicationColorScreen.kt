package com.benoitletondor.pixelminimalwatchface.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.benoitletondor.pixelminimalwatchface.common.settings.ComplicationColorScreen
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor

class WatchComplicationColorScreen : ComplicationColorScreen {

    @Composable
    override fun ColorChip(color: ComplicationColor, onClick: () -> Unit) {
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
}