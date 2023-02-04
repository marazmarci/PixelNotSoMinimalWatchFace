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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings

import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchface.common.settings.ComplicationColorScreen
import com.benoitletondor.pixelminimalwatchface.common.settings.SettingsComposeComponents
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColorCategory
import com.benoitletondor.pixelminimalwatchface.common.settings.model.Platform
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings.Chip
import com.benoitletondor.pixelminimalwatchfacecompanion.view.NAV_COLOR_SELECTOR_SCREEN_ROUTE
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PhoneComplicationColorScreen(
    private val viewModel: PhoneComplicationColorScreenViewModel,
) : ComplicationColorScreen {

    override fun getPlatformCategories(): StateFlow<List<ComplicationColorCategory>>
        = viewModel.colorCategoriesStateFlow

    override fun onPlatformColorClicked(color: ComplicationColor) {
        viewModel.onColorClicked(color)
    }

    @Composable
    fun PhoneScreen(
        composeComponents: SettingsComposeComponents,
        platform: Platform,
        navController: NavController,
        defaultColor: ComplicationColor,
    ) {
        val activity = LocalContext.current as ComponentActivity

        LaunchedEffect("eventListener") {
            launch {
                viewModel.eventFlow.collect { event ->
                    when(event) {
                        is PhoneComplicationColorScreenViewModel.Event.SelectColorEvent -> {
                            navController.selectColorAndNavigateBack(event.color)
                        }
                        is PhoneComplicationColorScreenViewModel.Event.ShowMaterialUColorAlert -> {
                            MaterialAlertDialogBuilder(activity)
                                .setTitle("Material You color")
                                .setMessage("You'll apply the current Material You color.\n\nPlease note that if you change it in your system settings later this color will not be updated automatically, you'll need to select it again.")
                                .setPositiveButton("Ok") { _, _ -> viewModel.onMaterialUAlertDismissed(event.color) }
                                .setNegativeButton("Cancel") { _, _ -> }
                                .show()
                        }
                        PhoneComplicationColorScreenViewModel.Event.StartColorPicker -> {
                            navController.navigate(NAV_COLOR_SELECTOR_SCREEN_ROUTE)
                        }
                    }
                }
            }
        }

        LaunchedEffect("colorSelectorResult") {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getStateFlow(CustomSelectedColorResult, 0)
                ?.collect { colorIntOr0 ->
                    if (colorIntOr0 != 0){
                        navController.currentBackStackEntry?.savedStateHandle?.remove<Int>(
                            CustomSelectedColorResult
                        )

                        navController.selectColorAndNavigateBack(ComplicationColor(
                            color = colorIntOr0,
                            label = "Custom",
                            isDefault = false,
                        ))
                    }
                }
        }

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