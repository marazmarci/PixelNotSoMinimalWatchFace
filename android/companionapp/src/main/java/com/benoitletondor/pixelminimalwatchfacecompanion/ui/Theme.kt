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
package com.benoitletondor.pixelminimalwatchfacecompanion.ui

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val primaryBlue = Color(0xFF5484f8)
val primaryRed = Color(0xFFda482f)
val primaryGreen = Color(0xFF54A74C)
val orange = Color(0xFFbc5100)

@Composable
fun AppMaterialTheme(content: @Composable () -> Unit) {
    return MaterialTheme(
        colorScheme = darkColorScheme(
            primary = primaryRed,
            background = Color.Black,
            surface = Color.Black,
            onPrimary = Color.White,
            onBackground = Color.White,
            error = Color.Red,
            onSurface = Color.White,
            secondary = primaryBlue,
            onSecondary = Color.White,
            onError = Color.White,
        ),
        content = content,
    )
}

@Composable
fun blueButtonColors() = ButtonDefaults.buttonColors(
    containerColor = primaryBlue,
    contentColor = Color.White,
)

@Composable
fun whiteTextButtonColors() = ButtonDefaults.buttonColors(
    containerColor = Color.Transparent,
    contentColor = Color.White,
)