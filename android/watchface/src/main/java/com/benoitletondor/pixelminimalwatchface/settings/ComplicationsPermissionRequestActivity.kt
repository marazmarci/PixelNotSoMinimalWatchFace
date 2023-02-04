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