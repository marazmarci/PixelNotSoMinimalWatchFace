package com.benoitletondor.pixelminimalwatchface.settings

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.wearable.complications.ComplicationHelperActivity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.helper.isComplicationsPermissionGranted
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ComplicationsPermissionRequestActivity : ComponentActivity() {
    private lateinit var permissionRequestActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionRequestActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            val granted = isComplicationsPermissionGranted()

            lifecycleScope.launch {
                permissionRequestResultMutableSharedFlow.emit(granted)
            }

            finish()
        }

        permissionRequestActivityLauncher.launch(
            ComplicationHelperActivity.createPermissionRequestHelperIntent(
                this,
                ComponentName(this, PixelMinimalWatchFace::class.java),
            )
        )
    }

    override fun onDestroy() {
        lifecycleScope.launch {
            permissionRequestResultMutableSharedFlow.emit(isComplicationsPermissionGranted())
        }

        super.onDestroy()
    }

    companion object {
        private val permissionRequestResultMutableSharedFlow = MutableSharedFlow<Boolean>()
        val permissionRequestResultFlow = permissionRequestResultMutableSharedFlow
    }
}