package com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchface.common.settings.SettingsComposeComponents
import com.benoitletondor.pixelminimalwatchface.common.settings.WidgetConfigurationScreen
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.common.settings.model.Platform
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.platform.PhonePlatform
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings.Chip
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings.PlatformSettingChip
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class PhoneWidgetConfigurationScreen(
    private val phonePlatform: PhonePlatform,
) : WidgetConfigurationScreen {
    override val showTitleInScreen: Boolean = false

    @Composable
    override fun WidgetChip(
        modifier: Modifier,
        complicationLocation: ComplicationLocation,
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        PlatformSettingChip(
            modifier = modifier.fillMaxWidth()
                .padding(bottom = 8.dp),
            onClick = {
                coroutineScope.launch {
                    try {
                        Toast.makeText(context, "Continue on watch", Toast.LENGTH_LONG).show()
                        phonePlatform.editComplication(complicationLocation)
                    } catch (e: Exception) {
                        if (e is CancellationException) { throw e }

                        Log.e(TAG, "Error while starting edit complication", e)
                        Toast.makeText(context, "Unable to start widget edition on watch", Toast.LENGTH_LONG).show()
                    }
                }
            },
            label = "Edit widget on watch",
            secondaryLabel = "Tap to edit widget on your watch",
            iconDrawable = R.drawable.baseline_widgets_24,
        )
    }

    @Composable
    override fun ColorChip(
        modifier: Modifier,
        color: ComplicationColor,
        label: String,
        secondaryLabel: String,
        onClick: () -> Unit
    ) {
        Chip(
            modifier = modifier.fillMaxWidth(),
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

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = secondaryLabel,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

            }
        }
    }

    @Composable
    fun PhoneScreen(
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
        complicationLocation: ComplicationLocation,
    ) {
        AppTopBarScaffold(
            navController = navController,
            showBackButton = true,
            title = title(complicationLocation),
            content = {
                Box(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Screen(
                        modifier = Modifier.padding(top = 10.dp),
                        composeComponents = composeComponents,
                        platform = platform,
                        navController = navController,
                        complicationLocation = complicationLocation,
                    )
                }
            }
        )
    }

    companion object {
        private const val TAG = "PhoneWidgetConfScreen"
    }
}