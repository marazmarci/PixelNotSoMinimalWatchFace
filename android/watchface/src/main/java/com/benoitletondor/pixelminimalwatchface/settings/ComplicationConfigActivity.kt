/*
 *   Copyright 2021 Benoit LETONDOR
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
package com.benoitletondor.pixelminimalwatchface.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.wearable.phone.PhoneDeviceType
import android.support.wearable.view.ConfirmationOverlay
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.benoitletondor.pixelminimalwatchface.BuildConfig
import com.benoitletondor.pixelminimalwatchface.BuildConfig.COMPANION_APP_PLAYSTORE_URL
import com.benoitletondor.pixelminimalwatchface.Injection
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.model.Storage
import com.benoitletondor.pixelminimalwatchface.rating.FeedbackActivity
import com.benoitletondor.pixelminimalwatchface.settings.phonebattery.PhoneBatteryConfigurationActivity
import com.google.android.wearable.intent.RemoteIntent
import kotlinx.android.synthetic.main.activity_complication_config.*

class ComplicationConfigActivity : Activity() {
    private lateinit var adapter: ComplicationConfigRecyclerViewAdapter
    private lateinit var storage: Storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complication_config)

        storage = Injection.storage(this)
        adapter = ComplicationConfigRecyclerViewAdapter(this, storage, {
            openAppOnPhone()
        }, { use24hTimeFormat ->
            storage.setUse24hTimeFormat(use24hTimeFormat)
        }, {
            storage.setRatingDisplayed(true)
            startActivity(Intent(this, FeedbackActivity::class.java))
        }, { showWearOSLogo ->
            storage.setShouldShowWearOSLogo(showWearOSLogo)
        }, { showComplicationsAmbient ->
            storage.setShouldShowComplicationsInAmbientMode(showComplicationsAmbient)
        }, { showFilledTimeAmbient ->
            storage.setShouldShowFilledTimeInAmbientMode(showFilledTimeAmbient)
        }, { timeSize ->
            storage.setTimeSize(timeSize)
        }, { showSecondsRing ->
            storage.setShouldShowSecondsRing(showSecondsRing)
        }, { showWeather ->
            storage.setShouldShowWeather(showWeather)
        }, { showBattery ->
            storage.setShouldShowBattery(showBattery)
        }, { useShortDateFormat ->
            storage.setUseShortDateFormat(useShortDateFormat)
        }, { showDateAmbient ->
            storage.setShowDateInAmbient(showDateAmbient)
        }, {
            openAppForDonationOnPhone()
        }, {
            startActivityForResult(
                Intent(this, PhoneBatteryConfigurationActivity::class.java),
                COMPLICATION_PHONE_BATTERY_SETUP_REQUEST_CODE,
            )
        }, {
            startActivityForResult(
                ColorSelectionActivity.createIntent(
                    this,
                    ComplicationColor(getColor(R.color.white), getString(R.string.color_default), true)
                ),
                TIME_AND_DATE_COLOR_REQUEST_CODE
            )
        }, {
            startActivityForResult(
                ColorSelectionActivity.createIntent(
                    this,
                    ComplicationColor(getColor(R.color.white), getString(R.string.color_default), true)
                ),
                BATTERY_COLOR_REQUEST_CODE
            )
        })

        wearable_recycler_view.isEdgeItemsCenteringEnabled = true
        wearable_recycler_view.layoutManager = LinearLayoutManager(this)
        wearable_recycler_view.setHasFixedSize(true)
        wearable_recycler_view.adapter = adapter
    }

    override fun onDestroy() {
        adapter.onDestroy()

        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if( requestCode == COMPLICATION_WEATHER_PERMISSION_REQUEST_CODE ) {
            adapter.weatherComplicationPermissionFinished()
        } else if( requestCode == COMPLICATION_BATTERY_PERMISSION_REQUEST_CODE ) {
            adapter.batteryComplicationPermissionFinished()
        } else if ( requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK ) {
            adapter.updateComplications()
        } else if ( requestCode == COMPLICATION_PHONE_BATTERY_SETUP_REQUEST_CODE ) {
            adapter.updateComplications()
        } else if ( requestCode == TIME_AND_DATE_COLOR_REQUEST_CODE && resultCode == RESULT_OK ) {
            val color = data?.getParcelableExtra<ComplicationColor>(ColorSelectionActivity.RESULT_SELECTED_COLOR)
            if (color != null) {
                storage.setTimeAndDateColor(color.color)
            }
        } else if ( requestCode == BATTERY_COLOR_REQUEST_CODE && resultCode == RESULT_OK ) {
            val color = data?.getParcelableExtra<ComplicationColor>(ColorSelectionActivity.RESULT_SELECTED_COLOR)
            if (color != null) {
                storage.setBatteryIndicatorColor(color.color)
            }
        }
    }

    private fun openAppOnPhone() {
        if ( PhoneDeviceType.getPhoneDeviceType(applicationContext) == PhoneDeviceType.DEVICE_TYPE_ANDROID ) {
            // Create Remote Intent to open Play Store listing of app on remote device.
            val intentAndroid = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("pixelnotsominimalwatchface://open"))
                .setPackage(BuildConfig.APPLICATION_ID)

            RemoteIntent.startRemoteActivity(
                applicationContext,
                intentAndroid,
                object : ResultReceiver(Handler()) {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultCode == RemoteIntent.RESULT_OK) {
                            ConfirmationOverlay()
                                .setFinishedAnimationListener {
                                    finish()
                                }
                                .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                .setDuration(3000)
                                .setMessage(getString(R.string.open_phone_url_android_device))
                                .showOn(this@ComplicationConfigActivity)
                        } else {
                            openAppInStoreOnPhone()
                        }
                    }
                }
            )

            return
        }

        openAppInStoreOnPhone()
    }

    private fun openAppForDonationOnPhone() {
        if ( PhoneDeviceType.getPhoneDeviceType(applicationContext) == PhoneDeviceType.DEVICE_TYPE_ANDROID ) {
            // Create Remote Intent to open Play Store listing of app on remote device.
            val intentAndroid = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("pixelnotsominimalwatchface://donate"))
                .setPackage(BuildConfig.APPLICATION_ID)

            RemoteIntent.startRemoteActivity(
                applicationContext,
                intentAndroid,
                object : ResultReceiver(Handler()) {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultCode == RemoteIntent.RESULT_OK) {
                            ConfirmationOverlay()
                                .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                .setDuration(3000)
                                .setMessage(getString(R.string.open_phone_url_android_device))
                                .showOn(this@ComplicationConfigActivity)
                        } else {
                            openAppInStoreOnPhone(finish = false)
                        }
                    }
                }
            )

            return
        }

        openAppInStoreOnPhone(finish = false)
    }

    private fun openAppInStoreOnPhone(finish: Boolean = true) {
        when (PhoneDeviceType.getPhoneDeviceType(applicationContext)) {
            PhoneDeviceType.DEVICE_TYPE_ANDROID -> {
                // Create Remote Intent to open Play Store listing of app on remote device.
                val intentAndroid = Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse(COMPANION_APP_PLAYSTORE_URL))

                RemoteIntent.startRemoteActivity(
                    applicationContext,
                    intentAndroid,
                    object : ResultReceiver(Handler()) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            if (resultCode == RemoteIntent.RESULT_OK) {
                                ConfirmationOverlay()
                                    .setFinishedAnimationListener {
                                        if( finish ) {
                                            finish()
                                        }
                                    }
                                    .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                    .setDuration(3000)
                                    .setMessage(getString(R.string.open_phone_url_android_device))
                                    .showOn(this@ComplicationConfigActivity)
                            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                                ConfirmationOverlay()
                                    .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                    .setDuration(3000)
                                    .setMessage(getString(R.string.open_phone_url_android_device_failure))
                                    .showOn(this@ComplicationConfigActivity)
                            }
                        }
                    }
                )
            }
            PhoneDeviceType.DEVICE_TYPE_IOS -> {
                Toast.makeText(this@ComplicationConfigActivity, R.string.open_phone_url_ios_device, Toast.LENGTH_LONG).show()
            }
            PhoneDeviceType.DEVICE_TYPE_ERROR_UNKNOWN -> {
                Toast.makeText(this@ComplicationConfigActivity, R.string.open_phone_url_android_device_failure, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val COMPLICATION_WEATHER_PERMISSION_REQUEST_CODE = 1003
        const val COMPLICATION_BATTERY_PERMISSION_REQUEST_CODE = 1004
        const val COMPLICATION_CONFIG_REQUEST_CODE = 1005
        const val COMPLICATION_PHONE_BATTERY_SETUP_REQUEST_CODE = 1006
        const val TIME_AND_DATE_COLOR_REQUEST_CODE = 1007
        const val BATTERY_COLOR_REQUEST_CODE = 1008
    }
}