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

    fun refresh() {
        cachedValue.value = getter()
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
    storageUpdater: StorageUpdater,
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Int,
) : StorageCachedValue<Int>(
    getter = { sharedPreferences.getInt(key, defaultValue) },
    setter = { sharedPreferences.edit { putInt(key, it) } },
) {
    init {
        storageUpdater.register(key, this)
    }
}

class StorageCachedBoolValue(
    storageUpdater: StorageUpdater,
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Boolean,
) : StorageCachedValue<Boolean>(
    getter = { sharedPreferences.getBoolean(key, defaultValue) },
    setter = { sharedPreferences.edit { putBoolean(key, it) } },
) {
    init {
        storageUpdater.register(key, this)
    }
}

class StorageCachedColorValue(
    storageUpdater: StorageUpdater,
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

    init {
        storageUpdater.register(key, this)
    }
}

class StorageCachedResolvedColorValue(
    storageUpdater: StorageUpdater,
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

    init {
        storageUpdater.register(key, this)
    }
}

data class CachedColorValues(
    @ColorInt val color: Int,
    val colorFilter: ColorFilter,
)