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
package com.benoitletondor.pixelminimalwatchface

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.wearable.watchface.CanvasWatchFaceService
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AppUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if( intent?.action != "android.intent.action.MY_PACKAGE_REPLACED" ) {
            return
        }

        val storage = Injection.storage(context)
        val latestKnownVersion = storage.getAppVersion()
        if( BuildConfig.VERSION_CODE > latestKnownVersion ) {
            if( latestKnownVersion > 0 ) {
                onAppUpgrade(context, latestKnownVersion, BuildConfig.VERSION_CODE)
            }

            storage.setAppVersion(BuildConfig.VERSION_CODE)
        }
    }

    @Suppress("SameParameterValue", "UNUSED_PARAMETER")
    private fun onAppUpgrade(context: Context, oldVersion: Int, newVersion: Int) {
        if( oldVersion < 170 ) {
            val storage = Injection.storage(context)

            if( !storage.hasFeatureDropWinter2023NotificationBeenShown() ) {
                storage.setFeatureDropWinter2023NotificationShown()
                showFeatureDropNotification(context)
            }
        }
    }

    companion object {
        fun showFeatureDropNotification(context: Context) {
            try {
                // Create notification channel if needed
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    val mChannel = NotificationChannel(
                        MISC_NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.misc_notification_channel_name),
                        importance
                    )
                    mChannel.description =
                        context.getString(R.string.misc_notification_channel_description)

                    val notificationManager =
                        context.getSystemService(CanvasWatchFaceService.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(mChannel)
                }

                val activityIntent = Intent(context, FeatureDropActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    activityIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                )

                val notification = NotificationCompat.Builder(context, MISC_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(context.getString(R.string.feature_drop_notification_title))
                    .setContentText(context.getString(R.string.feature_drop_notification_message))
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(context.getString(R.string.feature_drop_notification_message))
                    )
                    .addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_baseline_new_releases,
                            context.getString(R.string.feature_drop_notification_cta),
                            pendingIntent
                        )
                    )
                    .setAutoCancel(true)
                    .build()

                NotificationManagerCompat.from(context).notify(193729, notification)
            } catch (e: Exception) {
                Log.e("AppUpdateReceiver", "Error showing feature drop notification", e)
            }
        }
    }

}
