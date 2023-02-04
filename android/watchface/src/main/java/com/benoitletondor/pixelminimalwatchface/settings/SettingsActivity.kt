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

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.benoitletondor.pixelminimalwatchface.*
import com.benoitletondor.pixelminimalwatchface.common.settings.ROUTE_COLOR
import com.benoitletondor.pixelminimalwatchface.common.settings.ROUTE_COLOR_DEFAULT_ARG
import com.benoitletondor.pixelminimalwatchface.common.settings.ROUTE_WIDGET
import com.benoitletondor.pixelminimalwatchface.common.settings.ROUTE_WIDGET_LOCATION_ARG
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColorsProvider
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.compose.*
import com.benoitletondor.pixelminimalwatchface.helper.isComplicationsPermissionGranted
import com.benoitletondor.pixelminimalwatchface.model.WatchPlatform
import com.benoitletondor.pixelminimalwatchface.settings.screens.WatchComplicationColorScreen
import com.benoitletondor.pixelminimalwatchface.settings.screens.WatchSettingsScreen
import com.benoitletondor.pixelminimalwatchface.settings.screens.WatchWidgetConfigurationScreen
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    private lateinit var permissionRequestActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var watchPlatform: WatchPlatform
    private lateinit var watchSettingsScreen: WatchSettingsScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionRequestActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            val granted = isComplicationsPermissionGranted()

            lifecycleScope.launch {
                watchPlatform.onPermissionResult(granted)
            }
        }

        watchPlatform = WatchPlatform(this, Injection.storage(this), permissionRequestActivityLauncher)
        watchSettingsScreen = WatchSettingsScreen(watchPlatform)

        setContent {
            val swipeDismissableNavController = rememberSwipeDismissableNavController()
            val watchSettingsComponents = remember("settingsComponents") { WatchSettingsComposeComponents() }

            WearTheme {
                SwipeDismissableNavHost(
                    navController = swipeDismissableNavController,
                    startDestination = ROUTE_START,
                ) {
                    composable(ROUTE_START) {
                        watchSettingsScreen.Screen(
                            composeComponents = watchSettingsComponents,
                            platform = watchPlatform,
                            navController = swipeDismissableNavController,
                        )
                    }

                    composable(
                        ROUTE_WIDGET,
                        arguments = listOf(navArgument(ROUTE_WIDGET_LOCATION_ARG) { type = NavType.StringType })
                    ) {
                        val complicationLocation = ComplicationLocation.valueOf( it.arguments?.getString(ROUTE_WIDGET_LOCATION_ARG)
                            ?: throw IllegalStateException("Unable to find $ROUTE_WIDGET_LOCATION_ARG arg"))

                        WatchWidgetConfigurationScreen().Screen(
                            modifier = Modifier,
                            composeComponents = watchSettingsComponents,
                            platform = watchPlatform,
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

                        WatchComplicationColorScreen().Screen(
                            composeComponents = watchSettingsComponents,
                            platform = watchPlatform,
                            navController = swipeDismissableNavController,
                            defaultColor = ComplicationColor(complicationColor, ComplicationColorsProvider.defaultColorName, true),
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val ROUTE_START = "settings"
    }
}


