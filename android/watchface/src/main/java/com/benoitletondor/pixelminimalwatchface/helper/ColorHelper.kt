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
package com.benoitletondor.pixelminimalwatchface.helper

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.SparseArray
import androidx.annotation.ColorInt

private const val f = 0.8f

private val dimmedColorCache: SparseArray<Int> = SparseArray()
private val colorFilterCache: SparseArray<ColorFilter> = SparseArray()

@ColorInt
fun Int.dimmed(): Int {
    return dimmedColorCache[this] ?: kotlin.run {
        val dimmedValue = Color.argb(
            Color.alpha(this),
            (Color.red(this) * f).toInt(),
            (Color.green(this) * f).toInt(),
            (Color.blue(this) * f).toInt(),
        )
        dimmedColorCache[this] = dimmedValue

        dimmedValue
    }
}

fun Int.colorFilter(): ColorFilter {
    return colorFilterCache[this] ?: kotlin.run {
        val colorFilter = PorterDuffColorFilter(this, PorterDuff.Mode.SRC_IN)
        colorFilterCache[this] = colorFilter
        colorFilter
    }
}