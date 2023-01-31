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