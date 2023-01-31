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