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
package com.benoitletondor.pixelminimalwatchfacecompanion.sync

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import com.google.android.wearable.intent.RemoteIntent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.lang.RuntimeException
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val KEY_PREMIUM = "premium"
private const val KEY_TIMESTAMP = "ts"
private const val KEY_SYNC_ACTIVATED = "/batterySync/syncActivated"
private const val KEY_BATTERY_STATUS_PERCENT = "/batterySync/batteryStatus"
private const val KEY_NOTIFICATIONS_SYNC_STATUS = "/notificationsSync/syncStatus"

class SyncImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : Sync {
    private val capabilityClient = Wearable.getCapabilityClient(context)
    private val messageClient = Wearable.getMessageClient(context)
    private val dataClient = Wearable.getDataClient(context)

    override suspend fun sendPremiumStatus(isUserPremium: Boolean) {
        // Sending as data request
        val putDataRequest = PutDataMapRequest.create("/premium").run {
            dataMap.putBoolean(KEY_PREMIUM, isUserPremium)
            dataMap.putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            asPutDataRequest()
        }

        putDataRequest.setUrgent()
        dataClient.putDataItem(putDataRequest).await()

        // Send also as message
        val watchNodes = getConnectedWatchNodes()
        for(watchNode in watchNodes) {
            messageClient.sendMessage(
                watchNode.id,
                KEY_PREMIUM,
                byteArrayOf(if (isUserPremium) { 1 } else { 0 }),
            ).await()
        }
    }

    override suspend fun getWearableStatus(): Sync.WearableStatus {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if( nodes.isEmpty() ) {
                return Sync.WearableStatus.NotAvailable
            }

            val capabilityInfoTask = capabilityClient.getCapability(BuildConfig.WATCH_CAPABILITY, CapabilityClient.FILTER_ALL)
            val result = capabilityInfoTask.await()
            return if( result.nodes.isEmpty() ) {
                Sync.WearableStatus.AvailableAppNotInstalled
            } else {
                Sync.WearableStatus.AvailableAppInstalled
            }
        } catch (t: Throwable) {
            return Sync.WearableStatus.Error(t)
        }
    }

    override suspend fun openPlayStoreOnWatch() = suspendCancellableCoroutine<Boolean> { continuation ->
        try {
            val intentAndroid = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse(BuildConfig.WATCH_FACE_APP_PLAYSTORE_URL))

            RemoteIntent.startRemoteActivity(
                context,
                intentAndroid,
                object : ResultReceiver(Handler()) {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultCode == RemoteIntent.RESULT_OK) {
                            if( continuation.isActive ) {
                                continuation.resume(true)
                            }
                        } else {
                            if( continuation.isActive ) {
                                continuation.resumeWithException(RuntimeException("Error opening PlayStore on watch (result code: $resultCode)"))
                            }
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            continuation.resumeWithException(t)
        }
    }

    override fun subscribeToCapabilityChanges(listener: CapabilityClient.OnCapabilityChangedListener) {
        capabilityClient.addListener(listener, BuildConfig.WATCH_CAPABILITY)
    }

    override fun unsubscribeToCapabilityChanges(listener: CapabilityClient.OnCapabilityChangedListener) {
        capabilityClient.removeListener(listener)
    }

    override suspend fun sendBatterySyncStatus(syncActivated: Boolean) {
        val watchNodes = getConnectedWatchNodes()

        for(watchNode in watchNodes) {
            messageClient.sendMessage(
                watchNode.id,
                KEY_SYNC_ACTIVATED,
                byteArrayOf(if(syncActivated) { 1 } else { 0 }),
            ).await()
        }
    }

    override suspend fun sendBatteryStatus(batteryPercentage: Int) {
        val watchNodes = getConnectedWatchNodes()

        for(watchNode in watchNodes) {
            messageClient.sendMessage(
                watchNode.id,
                KEY_BATTERY_STATUS_PERCENT,
                byteArrayOf(batteryPercentage.toByte()),
            ).await()
        }
    }

    override suspend fun sendNotificationsSyncStatus(syncActivated: Sync.NotificationsSyncStatus) {
        val watchNodes = getConnectedWatchNodes()

        for(watchNode in watchNodes) {
            messageClient.sendMessage(
                watchNode.id,
                KEY_NOTIFICATIONS_SYNC_STATUS,
                byteArrayOf(syncActivated.intValue.toByte()),
            ).await()
        }
    }

    override suspend fun sendActiveNotifications(notifications: Sync.NotificationsData) {
        val notificationIconIds = ArrayList(notifications.iconIds.take(NUMBER_OF_NOTIFICATIONS_TO_SEND))

        val putDataRequest = PutDataMapRequest.create("/notifications").run {
            dataMap.putIntegerArrayList("iconIds", notificationIconIds)
            dataMap.putBoolean("hasMore", notifications.iconIds.size > NUMBER_OF_NOTIFICATIONS_TO_SEND)
            notifications.iconIdsToIcons.forEach { (iconId, icon) ->
                if (iconId !in notificationIconIds) {
                    return@forEach
                }

                val asset = icon.loadDrawable(context)
                    ?.toBitmap(NOTIFICATION_ICON_SIZE_PX, NOTIFICATION_ICON_SIZE_PX)
                    ?.createAsset() ?: return@forEach

                dataMap.putAsset(
                    "icon/$iconId",
                    asset
                )
            }
            dataMap.putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            asPutDataRequest()
        }

        putDataRequest.setUrgent()

        if (coroutineContext.isActive) {
            dataClient.putDataItem(putDataRequest).await()
        }
    }

    private fun Bitmap.createAsset(): Asset = ByteArrayOutputStream().let { byteStream ->
        compress(Bitmap.CompressFormat.PNG, 80, byteStream)
        Asset.createFromBytes(byteStream.toByteArray())
    }

    private suspend fun getConnectedWatchNodes(): Set<Node> {
        return try {
            capabilityClient.getCapability(BuildConfig.WATCH_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .await()
                .nodes
                .filterNearby()
        } catch (t: Throwable) {
            Log.e("Sync", "Unable to find watch node", t)
            emptySet()
        }
    }

}

private suspend fun <T> Task<T>.await() = suspendCancellableCoroutine<T> { continuation ->
    addOnSuccessListener { if( continuation.isActive ) { continuation.resume(it) } }
    addOnFailureListener { if( continuation.isActive ) { continuation.resumeWithException(it) } }
    addOnCanceledListener { continuation.cancel() }
}

private fun Set<Node>.filterNearby(): Set<Node> {
    return filter { it.isNearby }.toSet()
}

private const val NOTIFICATION_ICON_SIZE_PX = 32
private const val NUMBER_OF_NOTIFICATIONS_TO_SEND = 5