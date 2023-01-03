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
package com.benoitletondor.pixelminimalwatchface.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.ColorInt
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.benoitletondor.pixelminimalwatchface.*
import com.benoitletondor.pixelminimalwatchface.compose.*
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColorsProvider
import com.benoitletondor.pixelminimalwatchface.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.settings.screens.ComplicationColorScreen
import com.benoitletondor.pixelminimalwatchface.settings.screens.ComplicationColorScreenResultKey
import com.benoitletondor.pixelminimalwatchface.settings.screens.SettingsScreen
import com.benoitletondor.pixelminimalwatchface.settings.screens.WidgetConfigurationScreen
import kotlinx.coroutines.*
import kotlin.coroutines.resume

private const val ROUTE_START = "settings"

private const val ROUTE_COLOR_DEFAULT_ARG = "default"
private const val ROUTE_COLOR = "color/{$ROUTE_COLOR_DEFAULT_ARG}"

private const val ROUTE_WIDGET_LOCATION_ARG = "location"
private const val ROUTE_WIDGET = "widget/{$ROUTE_WIDGET_LOCATION_ARG}"

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val swipeDismissableNavController = rememberSwipeDismissableNavController()

            WearTheme {
                SwipeDismissableNavHost(
                    navController = swipeDismissableNavController,
                    startDestination = ROUTE_START,
                ) {
                    composable(ROUTE_START) {
                        SettingsScreen(
                            navController = swipeDismissableNavController,
                            storage = Injection.storage(this@SettingsActivity),
                        )
                    }

                    composable(
                        ROUTE_WIDGET,
                        arguments = listOf(navArgument(ROUTE_WIDGET_LOCATION_ARG) { type = NavType.StringType })
                    ) {
                        val complicationLocation = ComplicationLocation.valueOf( it.arguments?.getString(ROUTE_WIDGET_LOCATION_ARG)
                            ?: throw IllegalStateException("Unable to find $ROUTE_WIDGET_LOCATION_ARG arg"))

                        WidgetConfigurationScreen(
                            storage = Injection.storage(this@SettingsActivity),
                            navController = swipeDismissableNavController,
                            complicationLocation = complicationLocation,
                        )
                    }

                    composable(
                        ROUTE_COLOR,
                        arguments = listOf(navArgument(ROUTE_COLOR_DEFAULT_ARG) { type = NavType.IntType })
                    ) {
                        val complicationColor: Int = it.arguments?.getInt(ROUTE_COLOR_DEFAULT_ARG)
                            ?: throw IllegalStateException("Unable to find $ROUTE_COLOR_DEFAULT_ARG arg")

                        ComplicationColorScreen(
                            navController = swipeDismissableNavController,
                            defaultColor = ComplicationColor(complicationColor, ComplicationColorsProvider.defaultColorName, true),
                        )
                    }
                }
            }
        }
    }
}

fun NavHostController.navigateToWidgetSelectionScreen(complicationLocation: ComplicationLocation) {
    navigate(ROUTE_WIDGET.replace("{${ROUTE_WIDGET_LOCATION_ARG}}", complicationLocation.name))
}

suspend fun NavHostController.navigateToColorSelectionScreen(@ColorInt defaultColor: Int): ComplicationColor? = suspendCancellableCoroutine { continuation ->
    var listener:  NavController.OnDestinationChangedListener? = null
    val destination = currentDestination
    listener = NavController.OnDestinationChangedListener { _, newDestination, _ ->
        if (newDestination != destination) {
            return@OnDestinationChangedListener
        }

        listener?.let { removeOnDestinationChangedListener(it) }

        val result = currentBackStackEntry?.savedStateHandle?.get<ComplicationColor>(ComplicationColorScreenResultKey)
        if (continuation.isActive) {
            continuation.resume(result)
        }
    }

    navigate(ROUTE_COLOR.replace("{${ROUTE_COLOR_DEFAULT_ARG}}", defaultColor.toString()))

    addOnDestinationChangedListener(listener)

    continuation.invokeOnCancellation {
        removeOnDestinationChangedListener(listener)
    }
}
