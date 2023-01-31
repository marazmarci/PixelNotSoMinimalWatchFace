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
package com.benoitletondor.pixelminimalwatchface

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.benoitletondor.pixelminimalwatchface.common.compose.productSansFontFamily
import com.benoitletondor.pixelminimalwatchface.compose.WearTheme
import com.benoitletondor.pixelminimalwatchface.compose.component.ChipButton
import com.benoitletondor.pixelminimalwatchface.compose.component.RotatoryAwareLazyColumn
import com.benoitletondor.pixelminimalwatchface.helper.isScreenRound
import com.benoitletondor.pixelminimalwatchface.settings.SettingsActivity

class FeatureDropActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FeatureDropScreen()
        }
    }

    @Composable
    private fun FeatureDropScreen() {
        WearTheme {
            val context = LocalContext.current
            val isActive = remember { PixelMinimalWatchFace.isActive(context) }
            val isScreenRound = remember { context.isScreenRound() }

            RotatoryAwareLazyColumn(
                horizontalPadding = 20.dp,
            ) {
                Items(isActive, isScreenRound)
            }
        }
    }

    private fun LazyListScope.Items(
        isActive: Boolean,
        isScreenRound: Boolean,
    ) {
        item(key = "Title") {
            Text(
                text = "Pixel Minimal Watch Face",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillParentMaxWidth(),
                fontSize = 16.sp,
                fontFamily = productSansFontFamily,
            )
        }

        item(key = "Subtitle") {
            Text(
                text = "Winter feature drop",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillParentMaxWidth(),
                fontSize = 16.sp,
                fontFamily = productSansFontFamily,
            )
        }

        item(key = "Intro") {
            Text(
                text = "New options to play with:",
                modifier = Modifier.padding(
                    top = 16.dp,
                    bottom = 8.dp,
                ),
            )
        }

        if (isScreenRound) {
            item(key = "Item1") {
                Text(
                    text = "- Seconds ring: New option to use smooth animation",
                )
            }
        }

        item(key = "Item2") {
            Text(
                text = "- Customize the widgets secondary color",
            )
        }

        item(key = "Item3") {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "- Filter apps from phone notifications sync. On the phone companion app, tap the \"Troubleshoot\" button and \"Setup notification icons sync\"",
            )
        }

        item(key = "Item4") {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "- Bug fixes and optimisations",
            )
        }

        if (isActive) {
            item(key = "isActiveText") {
                Text(
                    text = "To setup those new options, tap this button to go to the watch face settings:",
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            item(key = "isActiveCTA") {
                ChipButton(
                    text = "Open watch face settings",
                    modifier = Modifier.padding(top = 6.dp),
                    onClick = {
                        finish()
                        applicationContext.startActivity(
                            Intent(
                                this@FeatureDropActivity,
                                SettingsActivity::class.java
                            ).apply {
                                flags = FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                    },
                )
            }
        } else {
            item(key = "isInactiveText") {
                Text(
                    text = "To setup those new options, use Pixel Minimal Watch Face as your watch face and long press on the time to access the Configure button.",
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

        item(key = "Outro") {
            Text(
                text = "Thank you for using Pixel Minimal Watch Face :)",
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }

    @Preview(widthDp = 200, heightDp = 200)
    @Composable
    private fun ActivePreview() {
        LazyColumn {
            Items(isActive = true, isScreenRound = false)
        }
    }

    @Preview(widthDp = 200, heightDp = 200)
    @Composable
    private fun ActiveRoundPreview() {
        LazyColumn {
            Items(isActive = true, isScreenRound = true)
        }
    }

    @Preview(widthDp = 200, heightDp = 200)
    @Composable
    private fun InactivePreview() {
        LazyColumn {
            Items(isActive = false, isScreenRound = false)
        }
    }

    @Preview(widthDp = 200, heightDp = 200)
    @Composable
    private fun InactiveRoundPreview() {
        LazyColumn {
            Items(isActive = false, isScreenRound = true)
        }
    }
}