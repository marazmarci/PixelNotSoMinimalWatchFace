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
package com.benoitletondor.pixelminimalwatchface.common.helper

import android.content.Context
import com.benoitletondor.pixelminimalwatchface.common.R

const val DEFAULT_TIME_SIZE = 50

fun fontDisplaySizeToScaleFactor(timeSize: Int, android12Layout: Boolean): Float {
    return when(timeSize) {
        0 -> if ( android12Layout ) 0.70f else 0.80f
        25 -> if ( android12Layout ) 0.80f else 0.90f
        50 -> 1f
        75 -> if ( android12Layout ) 1.20f else 1.10f
        100 -> if ( android12Layout ) 1.40f else 1.20f
        else -> 1f
    }
}

fun Context.fontDisplaySizeToHumanReadableString(timeSize: Int): String {
    return when(timeSize) {
        0 -> getString(R.string.time_size_0)
        25 -> getString(R.string.time_size_25)
        50 -> getString(R.string.time_size_50)
        75 -> getString(R.string.time_size_75)
        100 -> getString(R.string.time_size_100)
        else -> getString(R.string.time_size_50)
    }
}