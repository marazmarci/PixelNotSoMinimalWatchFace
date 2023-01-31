package com.benoitletondor.pixelminimalwatchface.model

interface StorageUpdater {
    fun register(key: String, cachedValueStorage: StorageCachedValue<out Any>)
}