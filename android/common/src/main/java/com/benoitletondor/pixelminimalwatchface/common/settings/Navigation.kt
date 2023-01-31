package com.benoitletondor.pixelminimalwatchface.common.settings

import androidx.annotation.ColorInt
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

const val ROUTE_COLOR_DEFAULT_ARG = "default"
const val ROUTE_COLOR = "color/{$ROUTE_COLOR_DEFAULT_ARG}"
const val ROUTE_WIDGET_LOCATION_ARG = "location"
const val ROUTE_WIDGET = "widget/{$ROUTE_WIDGET_LOCATION_ARG}"

suspend fun NavController.navigateToColorSelectionScreen(@ColorInt defaultColor: Int): ComplicationColor? = suspendCancellableCoroutine { continuation ->
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

fun NavController.navigateToWidgetSelectionScreen(complicationLocation: ComplicationLocation) {
    navigate(ROUTE_WIDGET.replace("{${ROUTE_WIDGET_LOCATION_ARG}}", complicationLocation.name))
}