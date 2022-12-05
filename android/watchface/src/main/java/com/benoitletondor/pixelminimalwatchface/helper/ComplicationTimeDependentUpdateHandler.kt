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

import android.os.Handler
import android.os.Message
import android.util.Log
import com.benoitletondor.pixelminimalwatchface.DEBUG_LOGS
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import java.lang.ref.WeakReference

class ComplicationTimeDependentUpdateHandler(
    private val engine: WeakReference<PixelMinimalWatchFace.Engine>,
    private var hasUpdateScheduled: Boolean = false
) : Handler() {

    override fun handleMessage(msg: Message) {
        if (DEBUG_LOGS) Log.d(TAG, "handleMessage")

        super.handleMessage(msg)

        val engine = engine.get() ?: return

        hasUpdateScheduled = false

        if( !engine.isAmbientMode() && engine.isVisible ) {
            if (DEBUG_LOGS) Log.d(TAG, "invalidate")
            engine.invalidate()
        }
    }

    fun cancelUpdate() {
        if( hasUpdateScheduled ) {
            if (DEBUG_LOGS) Log.d(TAG, "cancelUpdate: Cancelling")

            hasUpdateScheduled = false
            removeMessages(MSG_UPDATE_TIME)
        } else {
            if (DEBUG_LOGS) Log.d(TAG, "cancelUpdate: No-op")
        }
    }

    fun scheduleUpdate(delay: Long) {
        if (DEBUG_LOGS) Log.d(TAG, "scheduleUpdate")

        if( hasUpdateScheduled ) {
            cancelUpdate()
        }

        hasUpdateScheduled = true
        sendEmptyMessageDelayed(MSG_UPDATE_TIME, delay)
    }

    fun hasUpdateScheduled(): Boolean = hasUpdateScheduled

    companion object {
        private const val TAG = "TimeDependentUpdateHandler"
        private const val MSG_UPDATE_TIME = 0
    }
}