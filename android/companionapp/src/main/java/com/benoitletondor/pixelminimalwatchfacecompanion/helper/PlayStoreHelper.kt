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
package com.benoitletondor.pixelminimalwatchfacecompanion.helper

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig

fun Activity.openPlayStore(): Boolean {
    return try {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(BuildConfig.WATCH_FACE_APP_PLAYSTORE_URL)
            setPackage("com.android.vending")
        })
        true
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}")))
        true
    } catch (e: Exception) {
        Log.e("OpenPlayStore", "Error while opening PlayStore", e)
        false
    }
}