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
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.wearable.complications.ComplicationHelperActivity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.helper.isComplicationsPermissionGranted

class ComplicationEditActivity : ComponentActivity() {
    private lateinit var permissionRequestActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var complicationSelectionLauncher: ActivityResultLauncher<Intent>

    private lateinit var complicationLocation: ComplicationLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        complicationLocation = ComplicationLocation.values()[intent.getIntExtra(complicationLocationExtraKey, 0)]

        permissionRequestActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val granted = isComplicationsPermissionGranted()

            if (granted) {
                startComplicationSelectionActivity()
            } else {
                finish()
            }
        }

        complicationSelectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            finish()
        }

        if (isComplicationsPermissionGranted()) {
            startComplicationSelectionActivity()
        } else {
            permissionRequestActivityLauncher.launch(
                ComplicationHelperActivity.createPermissionRequestHelperIntent(
                    this,
                    ComponentName(this, PixelMinimalWatchFace::class.java),
                )
            )
        }

    }

    private fun startComplicationSelectionActivity() {
        complicationSelectionLauncher.launch(ComplicationHelperActivity.createProviderChooserHelperIntent(
            this,
            ComponentName(this, PixelMinimalWatchFace::class.java),
            PixelMinimalWatchFace.getComplicationId(complicationLocation),
            *PixelMinimalWatchFace.getSupportedComplicationTypes(complicationLocation)
        ))
    }

    companion object {
        private const val complicationLocationExtraKey = "complicationLocation"

        fun createIntent(context: Context, complicationLocation: ComplicationLocation): Intent {
            return Intent(context, ComplicationEditActivity::class.java).apply {
                putExtra(complicationLocationExtraKey, complicationLocation.ordinal)
            }
        }
    }
}