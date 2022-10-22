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