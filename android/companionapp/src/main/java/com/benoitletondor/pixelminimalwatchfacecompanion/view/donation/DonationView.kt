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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.donation

import android.app.Activity
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.startSupportEmailActivity
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarMoreMenuItem
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold
import com.benoitletondor.pixelminimalwatchfacecompanion.view.donation.subviews.Loaded
import com.benoitletondor.pixelminimalwatchfacecompanion.view.donation.subviews.Error
import com.benoitletondor.pixelminimalwatchfacecompanion.view.donation.subviews.Loading
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@Composable
fun Donation(navController: NavController, donationViewModel: DonationViewModel) {
    val context = LocalContext.current
    val state: DonationViewModel.State by donationViewModel.stateFlow.collectAsState(initial = donationViewModel.state)

    LaunchedEffect("events") {
        donationViewModel.donationSuccessEventFlow.collect { skuPrice ->
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.donation_success_title)
                .setMessage(context.getString(R.string.donation_success_message, skuPrice))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    LaunchedEffect("error") {
        donationViewModel.errorPayingEventFlow.collect { error ->
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.donation_error_title)
                .setMessage(context.getString(R.string.donation_error_message, error.message))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    AppTopBarScaffold(
        navController = navController,
        showBackButton = true,
        title = "Support with a tip",
        actions = {
            AppTopBarMoreMenuItem {
                DropdownMenuItem(
                    onClick = { context.startSupportEmailActivity() },
                    text = {
                        Text(stringResource(R.string.send_feedback_cta), fontSize = 18.sp)
                    },
                )
            }
        },
        content = {
            when(val currentState = state) {
                is DonationViewModel.State.ErrorLoading -> Error(currentState.error) {
                    donationViewModel.onRetryLoadSKUsButtonClicked()
                }
                is DonationViewModel.State.Loaded -> Loaded(currentState.items) { donationItem ->
                    donationViewModel.onDonateButtonClicked(donationItem.sku, context as Activity)
                }
                DonationViewModel.State.Loading -> Loading()
            }
        }
    )
}
