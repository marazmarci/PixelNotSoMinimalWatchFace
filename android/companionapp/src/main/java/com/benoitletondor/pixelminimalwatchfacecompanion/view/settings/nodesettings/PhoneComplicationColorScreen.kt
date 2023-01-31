package com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchface.common.settings.ComplicationColorScreen
import com.benoitletondor.pixelminimalwatchface.common.settings.SettingsComposeComponents
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.Platform
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings.Chip

class PhoneComplicationColorScreen : ComplicationColorScreen {

    @Composable
    fun PhoneScreen(
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
        defaultColor: ComplicationColor,
    ) {
        AppTopBarScaffold(
            navController = navController,
            showBackButton = true,
            title = "Select a color",
            content = {
                Box(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Screen(
                        modifier = Modifier.padding(top = 10.dp),
                        composeComponents = composeComponents,
                        platform = platform,
                        navController = navController,
                        defaultColor = defaultColor,
                    )
                }
            }
        )
    }


    @Composable
    override fun ColorChip(
        color: ComplicationColor,
        onClick: () -> Unit,
    ) {
        Chip(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(color.color)),
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = color.label,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}