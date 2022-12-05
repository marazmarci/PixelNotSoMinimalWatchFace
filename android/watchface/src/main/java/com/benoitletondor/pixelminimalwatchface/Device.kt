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
package com.benoitletondor.pixelminimalwatchface

import android.content.Context
import android.os.Build

object Device {
    // https://en.wikipedia.org/wiki/Samsung_Galaxy_Watch_4
    private val galaxyWatch4ModelNumbers = setOf(
        "SM-R860",
        "SM-R865",
        "SM-R870",
        "SM-R875",
        "SM-R880",
        "SM-R885",
        "SM-R890",
        "SM-R895",
    )

    val isWearOS3: Boolean = Build.VERSION.SDK_INT >= 30
    val isSamsungGalaxyWatch: Boolean = isWearOS3 && Build.BRAND.equals("samsung", ignoreCase = true)
    fun isSamsungGalaxyWatchBigScreen(context: Context) = isSamsungGalaxyWatch && context.resources.displayMetrics.heightPixels >= 450
    val isSamsungGalaxyWatch4: Boolean = isSamsungGalaxyWatch && galaxyWatch4ModelNumbers.contains(Build.MODEL)
    val isOppoWatch: Boolean = Build.BRAND == "OPPO" && Build.MODEL == "OPPO Watch"
    val isPixelWatch: Boolean = isWearOS3 && Build.BRAND.equals("google", ignoreCase = true)
}
