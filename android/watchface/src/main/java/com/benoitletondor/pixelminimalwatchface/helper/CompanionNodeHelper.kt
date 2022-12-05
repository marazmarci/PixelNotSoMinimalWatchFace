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

import android.content.Context
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

private const val QUERY_BATTERY_ACTIVATED_SYNC_PATH = "/batterySync/activate"
private const val QUERY_BATTERY_DEACTIVATED_SYNC_PATH = "/batterySync/deactivate"

private const val QUERY_NOTIFICATIONS_ACTIVATED_SYNC_PATH = "/notificationsSync/activate"
private const val QUERY_NOTIFICATIONS_DEACTIVATED_SYNC_PATH = "/notificationsSync/deactivate"

fun Set<Node>.findBestCompanionNode(): Node? {
    return firstOrNull { it.isNearby } ?: firstOrNull()
}

suspend fun Node.startPhoneBatterySync(context: Context) {
    Wearable.getMessageClient(context).sendMessage(
        id,
        QUERY_BATTERY_ACTIVATED_SYNC_PATH,
        null,
    ).await()
}

suspend fun Node.stopPhoneBatterySync(context: Context) {
    Wearable.getMessageClient(context).sendMessage(
        id,
        QUERY_BATTERY_DEACTIVATED_SYNC_PATH,
        null,
    ).await()
}

suspend fun Node.startNotificationsSync(context: Context) {
    Wearable.getMessageClient(context).sendMessage(
        id,
        QUERY_NOTIFICATIONS_ACTIVATED_SYNC_PATH,
        null,
    ).await()
}

suspend fun Node.stopNotificationsSync(context: Context) {
    Wearable.getMessageClient(context).sendMessage(
        id,
        QUERY_NOTIFICATIONS_DEACTIVATED_SYNC_PATH,
        null,
    ).await()
}