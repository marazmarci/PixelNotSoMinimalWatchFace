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
package com.benoitletondor.pixelminimalwatchfacecompanion.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

abstract class StorageCachedValue<T>(
    initialValue: T,
    private inline val setter: suspend (T) -> Unit,
) {
    private var cachedValue: MutableStateFlow<T> = MutableStateFlow(initialValue)

    fun get(): T = cachedValue.value

    suspend fun set(value: T) {
        setter(value)
        cachedValue.value = value
    }

    fun watchChanges(): Flow<T> = cachedValue
}

class StorageCachedIntValue(
    private val syncClient: SyncSession,
    settings: Map<String, Any>,
    private val key: String,
) : StorageCachedValue<Int>(
    initialValue = settings[key] as Int,
    setter = { syncClient.setParameter(key, it) },
)

class StorageCachedBoolValue(
    private val syncClient: SyncSession,
    settings: Map<String, Any>,
    private val key: String,
) : StorageCachedValue<Boolean>(
    initialValue = settings[key] as Boolean,
    setter = { syncClient.setParameter(key, it) },
)