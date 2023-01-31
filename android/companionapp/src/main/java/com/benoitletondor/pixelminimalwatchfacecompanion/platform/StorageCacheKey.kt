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