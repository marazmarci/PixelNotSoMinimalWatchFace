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

import androidx.annotation.ColorInt
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.settings.screens.ComplicationColorScreenResultKey
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

const val ROUTE_START = "settings"

const val ROUTE_COLOR_DEFAULT_ARG = "default"
const val ROUTE_COLOR = "color/{$ROUTE_COLOR_DEFAULT_ARG}"

const val ROUTE_WIDGET_LOCATION_ARG = "location"
const val ROUTE_WIDGET = "widget/{$ROUTE_WIDGET_LOCATION_ARG}"

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

        val result = currentBackStackEntry?.savedStateHandle?.get<ComplicationColor>(
            ComplicationColorScreenResultKey
        )

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