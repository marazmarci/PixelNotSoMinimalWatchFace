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
package com.benoitletondor.pixelminimalwatchface.rating

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.widget.ConfirmationOverlay
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.common.helper.getEmailAddress
import com.benoitletondor.pixelminimalwatchface.common.helper.getEmailSubject
import com.benoitletondor.pixelminimalwatchface.compose.WearTheme
import com.benoitletondor.pixelminimalwatchface.compose.component.ChipButton
import com.benoitletondor.pixelminimalwatchface.compose.component.ExplanationText
import com.benoitletondor.pixelminimalwatchface.compose.component.RotatoryAwareLazyColumn
import com.google.android.wearable.intent.RemoteIntent

class FeedbackActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val swipeDismissableNavController = rememberSwipeDismissableNavController()

            SwipeDismissableNavHost(
                navController = swipeDismissableNavController,
                startDestination = ROUTE_START,
            ) {
                composable(ROUTE_START) {
                    FirstStep(
                        onPositiveButtonPressed = {
                            swipeDismissableNavController.navigate(ROUTE_POSITIVE)
                        },
                        onNegativeButtonPressed = {
                            swipeDismissableNavController.navigate(ROUTE_NEGATIVE)
                        },
                    )
                }

                composable(ROUTE_NEGATIVE) {
                    NegativeStep()
                }

                composable(ROUTE_POSITIVE) {
                    PositiveStep()
                }
            }
        }
    }

    @Composable
    private fun FirstStep(
        onPositiveButtonPressed: () -> Unit,
        onNegativeButtonPressed: () -> Unit,
    ) {
        WearTheme {
            RotatoryAwareLazyColumn(
                horizontalPadding = 20.dp,
            ) {
                item {
                    Text(
                        text = "Give your feedback",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                }

                item {
                    Text(
                        text = "What do you think of Pixel Minimal Watch Face?",
                    )
                }

                item {
                    Column {
                        ExplanationText(
                            text = "I develop it on my free time, doing the best I can to make it look great and as optimised as possible.",
                        )

                        ExplanationText(
                            text = "There still might be things to improve so I would really appreciate your feedback.",
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                }

                item {
                    ChipButton(
                        onClick = onPositiveButtonPressed,
                        text = "I like it",
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                item {
                    ChipButton(
                        onClick = onNegativeButtonPressed,
                        text = "I don't like it",
                    )
                }
            }
        }
    }

    @Composable
    private fun NegativeStep() {
        WearTheme {
            RotatoryAwareLazyColumn(
                horizontalPadding = 20.dp,
            ) {
                item {
                    Text(
                        text = "Give your feedback",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                }

                item {
                    Text(
                        text = ":( I'm sad to hear that! What can I do better?",
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }

                item {
                    ExplanationText(
                        text = "Would you mind sending me your feedback by email using your phone?",
                    )
                }

                item {
                    ChipButton(
                        backgroundColor = MaterialTheme.colors.secondary,
                        onClick = {
                            val mail = getEmailAddress()
                            val subject = getEmailSubject()
                            val body = getString(R.string.rating_feedback_send_text)

                            val sendIntent = Intent()
                            sendIntent.action = Intent.ACTION_VIEW
                            sendIntent.addCategory(Intent.CATEGORY_BROWSABLE)
                            sendIntent.data = Uri.parse("mailto:$mail?subject=$subject&body=$body")
                            sendIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
                            sendIntent.putExtra(Intent.EXTRA_TEXT, body)
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject)

                            RemoteIntent.startRemoteActivity(
                                this@FeedbackActivity,
                                sendIntent,
                                object : ResultReceiver(Handler()) {
                                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                                        if (resultCode == RemoteIntent.RESULT_OK) {
                                            ConfirmationOverlay()
                                                .setOnAnimationFinishedListener {
                                                    finish()
                                                }
                                                .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                                .setDuration(3000)
                                                .setMessage(getString(R.string.open_phone_url_android_device) as CharSequence)
                                                .showOn(this@FeedbackActivity)
                                        } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                                            Toast.makeText(
                                                this@FeedbackActivity,
                                                getString(R.string.rating_feedback_send_error),
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            finish()
                                        }
                                    }
                                }
                            )
                        },
                        text = "Send feedback by email",
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                item {
                    ChipButton(
                        onClick = { finish() },
                        text = "No thanks",
                    )
                }
            }
        }
    }

    @Composable
    private fun PositiveStep() {
        WearTheme {
            RotatoryAwareLazyColumn(
                horizontalPadding = 20.dp,
            ) {
                item {
                    Text(
                        text = "Give your feedback",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                }

                item {
                    Text(
                        text = ":) I'm glad you're enjoying it",
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }

                item {
                    Column {
                        Text(
                            text = "Feel like helping with a good rating on the PlayStore?",
                            modifier = Modifier.padding(bottom = 6.dp),
                        )

                        ExplanationText(
                            text = "5 stars reviews really help lot, I would really appreciate it.",
                        )
                    }

                }

                item {
                    ChipButton(
                        backgroundColor = MaterialTheme.colors.secondary,
                        onClick = {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$packageName")
                                )

                                startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                                )

                                startActivity(intent)
                            }

                            finish()
                        },
                        text = "Rate on the PlayStore",
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                item {
                    ChipButton(
                        onClick = { finish() },
                        text = "No thanks",
                    )
                }
            }
        }
    }

    @Composable
    @Preview
    private fun PreviewStartStep() {
        FirstStep(
            onPositiveButtonPressed = {},
            onNegativeButtonPressed = {},
        )
    }

    @Composable
    @Preview
    private fun PreviewPositiveStep() {
        PositiveStep()
    }

    @Composable
    @Preview
    private fun PreviewNegativeStep() {
        NegativeStep()
    }

    companion object {
        private const val ROUTE_START = "start"
        private const val ROUTE_POSITIVE = "positive"
        private const val ROUTE_NEGATIVE = "negative"
    }
}