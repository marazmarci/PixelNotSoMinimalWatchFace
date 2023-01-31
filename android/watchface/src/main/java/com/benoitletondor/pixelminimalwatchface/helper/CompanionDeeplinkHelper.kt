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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.wearable.phone.PhoneDeviceType
import android.support.wearable.view.ConfirmationOverlay
import android.util.Log
import com.benoitletondor.pixelminimalwatchface.BuildConfig
import com.benoitletondor.pixelminimalwatchface.DEBUG_LOGS
import com.benoitletondor.pixelminimalwatchface.R
import com.google.android.wearable.intent.RemoteIntent
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun Activity.openCompanionAppOnPhone(deeplinkPath: String) = suspendCancellableCoroutine<Boolean> { continuation ->
    if ( PhoneDeviceType.getPhoneDeviceType(this) == PhoneDeviceType.DEVICE_TYPE_ANDROID ) {
        val intentAndroid = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse("pixelminimalwatchface://$deeplinkPath"))
            .setPackage(BuildConfig.APPLICATION_ID)

        RemoteIntent.startRemoteActivity(
            this,
            intentAndroid,
            object : ResultReceiver(Handler()) {
                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    if (DEBUG_LOGS) Log.d("openCompanionAppOnPhone", "onReceiveResult: $resultCode / $resultData")

                    if (resultCode == RemoteIntent.RESULT_OK) {
                        ConfirmationOverlay()
                            .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                            .setDuration(3000)
                            .setMessage(getString(R.string.open_phone_url_android_device))
                            .showOn(this@openCompanionAppOnPhone)

                        if (continuation.isActive) {
                            continuation.resume(true)
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                }
            }
        )
    } else {
        if (continuation.isActive) {
            continuation.resume(false)
        }
    }
}