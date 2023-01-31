package com.benoitletondor.pixelminimalwatchface.settings.screens

import android.app.Activity
import android.content.ComponentName
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderChooserIntent
import android.support.wearable.complications.ProviderInfoRetriever
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.common.settings.WidgetConfigurationScreen
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.compose.component.WatchSettingComplicationSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

class WatchWidgetConfigurationScreen : WidgetConfigurationScreen {

    override val showTitleInScreen: Boolean = true

    @Composable
    override fun WidgetChip(
        modifier: Modifier,
        complicationLocation: ComplicationLocation,
    ) {
        val context = LocalContext.current

        val providerInfoRetriever = remember("chipRetriever") { ProviderInfoRetriever(context, Dispatchers.IO.asExecutor()) }
        val watchFaceComponentName = remember("chipName") { ComponentName(context, PixelMinimalWatchFace::class.java) }
        val complicationProviderInfoState = remember("chipProvider") { mutableStateOf<ComplicationProviderInfo?>(null) }

        val widgetSelectionActivityLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val complicationProviderInfo: ComplicationProviderInfo? =
                    activityResult.data?.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO)

                complicationProviderInfoState.value = complicationProviderInfo
            }
        }

        DisposableEffect("providerInfoRetrieverWidget") {
            providerInfoRetriever.init()

            providerInfoRetriever.retrieveProviderInfo(
                object : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                    override fun onProviderInfoReceived(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {
                        complicationProviderInfoState.value = complicationProviderInfo
                    }
                },
                watchFaceComponentName,
                PixelMinimalWatchFace.getComplicationId(complicationLocation)
            )

            onDispose {
                providerInfoRetriever.release()
            }
        }

        Chip(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 70.dp),
            label = {
                Text(
                    text = "Widget",
                    fontWeight = FontWeight.Normal,
                )
            },
            secondaryLabel = {
                Text(
                    text = if (complicationProviderInfoState.value != null) {
                        "Tap to change/remove"
                    } else {
                        "Tap to setup"
                    },
                    fontWeight = FontWeight.Normal,
                    color = Color.LightGray,
                )
            },
            icon = {
                WatchSettingComplicationSlot(
                    complicationProviderInfo = complicationProviderInfoState.value,
                    color = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .width(40.dp)
                        .height(40.dp)
                )
            },
            onClick = {
                widgetSelectionActivityLauncher.launch(
                    ComplicationHelperActivity.createProviderChooserHelperIntent(
                        context,
                        watchFaceComponentName,
                        PixelMinimalWatchFace.getComplicationId(complicationLocation),
                        *PixelMinimalWatchFace.getSupportedComplicationTypes(complicationLocation)
                    )
                )
            },
            colors = ChipDefaults.primaryChipColors(
                backgroundColor = MaterialTheme.colors.surface,
            ),
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
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = label,
                    fontWeight = FontWeight.Normal,
                )
            },
            secondaryLabel = {
                Text(
                    text = secondaryLabel,
                    fontWeight = FontWeight.Normal,
                    color = Color.LightGray,
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