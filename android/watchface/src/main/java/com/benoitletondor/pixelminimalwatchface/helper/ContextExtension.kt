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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat

fun Context.isScreenRound() = resources.configuration.isScreenRound

fun Context.isServiceAvailable(packageName: String, serviceName: String): Boolean {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SERVICES)
        val services = packageInfo.services ?: return false

        services.firstOrNull { it.name == serviceName } != null
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.openActivity(packageName: String, activityName: String) {
    try {
        startActivity(Intent().apply {
            component = ComponentName(
                packageName,
                activityName
            )
            flags = FLAG_ACTIVITY_NEW_TASK
        })
    } catch (t: Throwable) {
        Log.e("Pixel Minimal Watch Face", "Can't open activity: $activityName", t)
    }
}