/*
 *   Copyright 2023 Benoit LETONDOR
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

package com.benoitletondor.pixelminimalwatchfacecompanion.helper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.benoitletondor.pixelminimalwatchface.common.helper.getEmailAddress
import com.benoitletondor.pixelminimalwatchface.common.helper.getEmailSubject
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
//import com.google.android.play.core.review.ReviewManagerFactory

class RatingPopup(private val activity: Activity) {

//    private val reviewManager = ReviewManagerFactory.create(activity.applicationContext)

    fun show() {
        val dialog = buildStep1()
        dialog.show()
    }

    private fun buildStep1(): AlertDialog {
        val builder = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.rating_popup_question_title)
            .setMessage(R.string.rating_popup_question_message)
            .setNegativeButton(R.string.rating_popup_question_cta_negative) { _, _ ->
                buildNegativeStep().show()
            }
            .setPositiveButton(R.string.rating_popup_question_cta_positive) { _, _ ->
                buildPositiveStep().show()
            }

        return builder.create()
    }

    private fun buildNegativeStep(): AlertDialog {
        return MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.rating_popup_negative_title)
            .setMessage(R.string.rating_popup_negative_message)
            .setNegativeButton(R.string.rating_popup_negative_cta_negative) { _, _ -> }
            .setPositiveButton(R.string.rating_popup_negative_cta_positive) { _, _ ->
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SENDTO
                sendIntent.data = Uri.parse("mailto:") // only email apps should handle this
                sendIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getEmailAddress()))
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getEmailSubject())

                if ( sendIntent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(sendIntent)
                } else {
                    Toast.makeText(activity, activity.resources.getString(R.string.rating_feedback_send_error), Toast.LENGTH_SHORT).show()
                }
            }
            .create()
    }

    private fun buildPositiveStep(): AlertDialog {
        return MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.rating_popup_positive_title)
            .setMessage(R.string.rating_popup_positive_message)
            .setNegativeButton(R.string.rating_popup_positive_cta_negative) { _, _ -> }
            .setPositiveButton(R.string.rating_popup_positive_cta_positive) { _, _ ->
                /*reviewManager.requestReviewFlow()
                    .addOnCompleteListener { request ->
                        if (request.isSuccessful) {
                            val reviewInfo = request.result
                            reviewManager.launchReviewFlow(activity, reviewInfo)
                                .addOnCompleteListener { result ->
                                    if( !result.isSuccessful ) {
                                        activity.openPlayStore()
                                    }
                                }
                        } else {
                            activity.openPlayStore()
                        }
                    }*/
            }
            .create()
    }
}
