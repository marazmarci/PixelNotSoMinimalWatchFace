package com.benoitletondor.pixelminimalwatchface.model

import android.content.Context
import android.content.SharedPreferences
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart

abstract class StorageCachedValue<T>(
    private inline val getter: () -> T,
    private inline val setter: (T) -> Unit,
) {
    private var cachedValue: MutableStateFlow<T?> = MutableStateFlow(null)

    fun get(): T = cachedValue.value ?: kotlin.run {
        val newValue = getter()
        cachedValue.value = newValue
        return@run newValue
    }

    fun set(value: T) {
        cachedValue.value = value
        setter(value)
    }

    fun watchChanges(): Flow<T> = cachedValue
        .onStart { // Init value if not init yet
            if (cachedValue.value == null) {
                get()
            }
        }
        .mapNotNull { it }
}

class StorageCachedIntValue(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Int,
) : StorageCachedValue<Int>(
    getter = { sharedPreferences.getInt(key, defaultValue) },
    setter = { sharedPreferences.edit { putInt(key, it) } },
)

class StorageCachedBoolValue(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Boolean,
) : StorageCachedValue<Boolean>(
    getter = { sharedPreferences.getBoolean(key, defaultValue) },
    setter = { sharedPreferences.edit { putBoolean(key, it) } },
)

class StorageCachedColorValue(
    private val sharedPreferences: SharedPreferences,
    private val appContext: Context,
    private val key: String,
    @ColorRes private val colorRes: Int,
) : StorageCachedValue<CachedColorValues>(
    getter = {
        val color = sharedPreferences.getInt(key, appContext.getColor(colorRes))
        val colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        CachedColorValues(color, colorFilter)
    },
    setter = { sharedPreferences.edit { putInt(key, it.color) } }
) {
    fun set(@ColorInt color: Int) {
        set(CachedColorValues(color, PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)))
    }
}

class StorageCachedResolvedColorValue(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    @ColorInt private val colorInt: Int,
) : StorageCachedValue<CachedColorValues>(
    getter = {
        val color = sharedPreferences.getInt(key, colorInt)
        val colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        CachedColorValues(color, colorFilter)
    },
    setter = { sharedPreferences.edit { putInt(key, it.color) } }
) {
    fun set(@ColorInt color: Int) {
        set(CachedColorValues(color, PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)))
    }
}

data class CachedColorValues(
    @ColorInt val color: Int,
    val colorFilter: ColorFilter,
)