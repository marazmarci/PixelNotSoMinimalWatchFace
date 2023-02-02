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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.toColorInt
import kotlinx.coroutines.launch

const val CustomSelectedColorResult = "CustomSelectedColorResult"

@Composable
fun ColorSelectorScreen(
    navController: NavController,
    viewModel: ColorSelectorScreenViewModel,
) {
    LaunchedEffect("dismissPreviousResult") {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.remove<Int?>(CustomSelectedColorResult)
    }

    LaunchedEffect("eventListener") {
        launch {
            viewModel.eventFlow.collect { event ->
                when(event) {
                    is ColorSelectorScreenViewModel.Event.SendResultAndGoBack -> {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(CustomSelectedColorResult, event.colorInt)

                        navController.popBackStack()
                    }
                }
            }
        }
    }

    AppTopBarScaffold(
        navController = navController,
        showBackButton = true,
        title = "Pick a color",
        content = {
            Content(
                recentColors = viewModel.recentColors.map { HsvColor.from(Color(it)) },
                onColorSelected = viewModel::onColorSelected,
            )
        }
    )
}

@Composable
private fun Content(
    recentColors: List<HsvColor>,
    onColorSelected: (Int) -> Unit,
) {
    var currentColor by remember { mutableStateOf(HsvColor.from(Color.Blue)) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
    ) {
        ClassicColorPicker(
            modifier = Modifier.height(400.dp),
            color = currentColor,
            onColorChanged = { color: HsvColor ->
                currentColor = color
            }
        )

        if (recentColors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recents",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val boxWithConstraintsScope = this

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    for(color in recentColors) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size((boxWithConstraintsScope.maxWidth / 6).let { if (it > 50.dp) 50.dp else it })
                                .clip(CircleShape)
                                .background(color = color.toColor())
                                .clickable { currentColor = color }
                        )
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = currentColor.toColor())
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.onBackground)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                colors = blueButtonColors(),
                onClick = { onColorSelected(currentColor.toColorInt()) }
            ) {
                Text(
                    text = "Select",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}