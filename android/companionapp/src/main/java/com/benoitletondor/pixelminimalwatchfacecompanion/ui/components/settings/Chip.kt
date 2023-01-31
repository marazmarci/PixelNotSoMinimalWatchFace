package com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Chip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content : @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.DarkGray)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        content = content,
    )
}