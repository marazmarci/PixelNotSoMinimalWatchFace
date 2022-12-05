package com.benoitletondor.pixelminimalwatchface

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.collection.LruCache
import com.benoitletondor.pixelminimalwatchface.helper.await
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream
import java.util.*

class PhoneNotifications(
    private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val iconIdsToBitmapCache = LruCache<Int, Bitmap>(10)

    private val notificationsMutableStateFlow = MutableStateFlow<NotificationState>(NotificationState.Unknown())
    val notificationsStateFlow: StateFlow<NotificationState> = notificationsMutableStateFlow

    fun onDestroy() {
        scope.cancel()
    }

    fun onNewData(dataMap: DataMap) {
        scope.launch {
            try {
                if (DEBUG_LOGS) Log.d(TAG, "onNewData: $dataMap")

                val iconIds = dataMap.getIntegerArrayList("iconIds") ?: throw IllegalArgumentException("Missing iconIds parameter")
                val icons = mutableListOf<Bitmap>()

                for(iconId in iconIds) {
                    val existingBitmap = iconIdsToBitmapCache.get(iconId)
                    if (existingBitmap != null) {
                        icons.add(existingBitmap)
                        continue
                    }

                    val asset = dataMap.getAsset("icon/$iconId") ?: throw IllegalArgumentException("Missing icon/$iconId asset")
                    val bitmap = asset.toBitmap() ?: throw RuntimeException("Failed to decode icon/$iconId asset")
                    iconIdsToBitmapCache.put(iconId, bitmap)

                    icons.add(bitmap)
                }

                if (DEBUG_LOGS) Log.d(TAG, "dataReceived: ${icons.size} icons")
                notificationsMutableStateFlow.value = NotificationState.DataReceived(
                    icons = icons,
                    hasMore = dataMap.getBoolean("hasMore"),
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error while parsing new data", e)
            }
        }
    }

    sealed class NotificationState {
        class Unknown(private val createdAt: Date = Date()) : NotificationState() {
            fun isStale(currentTimestamp: Long): Boolean = currentTimestamp - createdAt.time > STALE_LIMIT_MS
        }

        class DataReceived(
            val icons: List<Bitmap>,
            val hasMore: Boolean,
        ) : NotificationState()

        companion object {
            private const val STALE_LIMIT_MS = 1000*60 // 1min
        }
    }

    private suspend fun Asset.toBitmap(): Bitmap? {
        val assetInputStream: InputStream? = Wearable.getDataClient(context).getFdForAsset(this).await().inputStream

        return assetInputStream?.let { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: run {
            Log.e(TAG, "Requested an unknown Asset.")
            null
        }
    }

    companion object {
        private const val TAG = "PhoneNotifications"
    }
}