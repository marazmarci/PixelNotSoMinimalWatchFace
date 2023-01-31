package com.benoitletondor.pixelminimalwatchface.model

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.phone.PhoneDeviceType
import android.support.wearable.view.ConfirmationOverlay
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchface.BuildConfig
import com.benoitletondor.pixelminimalwatchface.Device
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.common.settings.model.Platform
import com.benoitletondor.pixelminimalwatchface.common.settings.model.WeatherProvider
import com.benoitletondor.pixelminimalwatchface.common.settings.navigateToColorSelectionScreen
import com.benoitletondor.pixelminimalwatchface.common.settings.navigateToWidgetSelectionScreen
import com.benoitletondor.pixelminimalwatchface.getWeatherProviderInfo
import com.benoitletondor.pixelminimalwatchface.helper.isScreenRound
import com.benoitletondor.pixelminimalwatchface.helper.openCompanionAppOnPhone
import com.benoitletondor.pixelminimalwatchface.rating.FeedbackActivity
import com.benoitletondor.pixelminimalwatchface.settings.notificationssync.NotificationsSyncConfigurationActivity
import com.benoitletondor.pixelminimalwatchface.settings.phonebattery.PhoneBatteryConfigurationActivity
import com.google.android.wearable.intent.RemoteIntent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WatchPlatform(
    private val activity: ComponentActivity,
    storage: Storage,
    private val permissionRequestActivityLauncher: ActivityResultLauncher<Intent>
) : Platform, Storage by storage {
    override val isScreenRound: Boolean = activity.isScreenRound()
    override val isWearOS3: Boolean = Device.isWearOS3
    override val appVersionName: String = BuildConfig.VERSION_NAME
    override val weatherProvider: WeatherProvider = activity.getWeatherProviderInfo()
        ?.let { WeatherProvider(
            hasWeatherSupport = true,
            weatherProviderInfo = it,
        ) } ?: WeatherProvider(
            hasWeatherSupport = false,
            weatherProviderInfo = null,
        )

    private val watchFaceComponentName = ComponentName(activity, PixelMinimalWatchFace::class.java)

    private val permissionRequestResultMutableFlow = MutableSharedFlow<Boolean>()

    suspend fun onPermissionResult(granted: Boolean) {
        permissionRequestResultMutableFlow.emit(granted)
    }

    override suspend fun requestComplicationsPermission(activity: ComponentActivity): Boolean {
        permissionRequestActivityLauncher.launch(
            ComplicationHelperActivity.createPermissionRequestHelperIntent(
                this.activity,
                watchFaceComponentName,
            )
        )

        return permissionRequestResultMutableFlow.first()
    }

    override fun startPhoneBatterySyncConfigScreen(navController: NavController, activity: ComponentActivity) {
        activity.startActivity(Intent(activity, PhoneBatteryConfigurationActivity::class.java))
    }

    override fun startPhoneNotificationIconsConfigScreen(navController: NavController, activity: ComponentActivity) {
        activity.startActivity(Intent(activity, NotificationsSyncConfigurationActivity::class.java))
    }

    override fun startFeedbackScreen(navController: NavController, activity: ComponentActivity) {
        activity.startActivity(Intent(activity, FeedbackActivity::class.java))
    }

    override fun startDonationScreen(navController: NavController, activity: ComponentActivity) {
        openAppForDonationOnPhone(activity)
    }

    override suspend fun startColorSelectionScreen(navController: NavController, defaultColor: Int): ComplicationColor? {
        return navController.navigateToColorSelectionScreen(defaultColor)
    }

    override fun startWidgetConfigurationScreen(navController: NavController, complicationLocation: ComplicationLocation) {
        return navController.navigateToWidgetSelectionScreen(complicationLocation)
    }

    fun openAppOnPhone(activity: ComponentActivity) {
        activity.lifecycleScope.launch {
            if (!activity.openCompanionAppOnPhone("open")) {
                openAppInStoreOnPhone(activity)
            }
        }
    }

    private fun openAppForDonationOnPhone(activity: ComponentActivity) {
        activity.lifecycleScope.launch {
            if(!activity.openCompanionAppOnPhone("donate")) {
                openAppInStoreOnPhone(activity = activity, finish = false)
            }
        }
    }

    private fun openAppInStoreOnPhone(
        activity: ComponentActivity,
        finish: Boolean = true,
    ) {
        when (PhoneDeviceType.getPhoneDeviceType(activity)) {
            PhoneDeviceType.DEVICE_TYPE_ANDROID -> {
                // Create Remote Intent to open Play Store listing of app on remote device.
                val intentAndroid = Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse(BuildConfig.COMPANION_APP_PLAYSTORE_URL))

                RemoteIntent.startRemoteActivity(
                    activity,
                    intentAndroid,
                    object : ResultReceiver(Handler()) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            if (resultCode == RemoteIntent.RESULT_OK) {
                                ConfirmationOverlay()
                                    .setFinishedAnimationListener {
                                        if( finish ) {
                                            activity.finish()
                                        }
                                    }
                                    .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                    .setDuration(3000)
                                    .setMessage(activity.getString(R.string.open_phone_url_android_device))
                                    .showOn(activity)
                            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                                ConfirmationOverlay()
                                    .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                    .setDuration(3000)
                                    .setMessage(activity.getString(R.string.open_phone_url_android_device_failure))
                                    .showOn(activity)
                            }
                        }
                    }
                )
            }
            PhoneDeviceType.DEVICE_TYPE_IOS -> {
                Toast.makeText(activity, R.string.open_phone_url_ios_device, Toast.LENGTH_LONG).show()
            }
            PhoneDeviceType.DEVICE_TYPE_ERROR_UNKNOWN -> {
                Toast.makeText(activity, R.string.open_phone_url_android_device_failure, Toast.LENGTH_LONG).show()
            }
        }
    }
}