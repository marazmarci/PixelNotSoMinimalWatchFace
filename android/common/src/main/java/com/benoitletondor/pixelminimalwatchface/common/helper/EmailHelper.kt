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
package com.benoitletondor.pixelminimalwatchface.common.helper

import android.content.Context
import android.os.Build
import android.util.Log
import com.benoitletondor.pixelminimalwatchface.common.R

private const val PLAY_STORE_PACKAGE_NAME = "com.android.vending"

fun Context.getEmailSubject(): String {
    val isFromPlayStore = try {
        if (Build.VERSION.SDK_INT >= 30) {
            packageManager.getInstallSourceInfo(packageName).installingPackageName == PLAY_STORE_PACKAGE_NAME
        } else {
            packageManager.getInstallerPackageName(packageName) == PLAY_STORE_PACKAGE_NAME
        }
    } catch (e: Exception) {
        Log.e("PackageManager", "Error detecting installer package name", e)
        false
    }

    return if (isFromPlayStore) resources.getString(R.string.feedback_send_subject) else resources.getString(R.string.feedback_send_subject_non_play)
}

fun getEmailAddress() = "pixelwatchface@benoitletondor.com"