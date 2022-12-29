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
package com.benoitletondor.pixelminimalwatchfacecompanion.billing

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.flow.Flow

interface Billing {
    val userPremiumEventStream: Flow<PremiumCheckStatus>

    fun isUserPremium(): Boolean
    fun updatePremiumStatusIfNeeded()
    suspend fun launchPremiumPurchaseFlow(activity: Activity): PurchaseFlowResult

    suspend fun getDonationsProductDetails(): List<ProductDetails>
    suspend fun launchDonationPurchaseFlow(activity: Activity, sku: ProductDetails): Boolean
}

sealed class PurchaseFlowResult {
    object Cancelled : PurchaseFlowResult()
    object Success : PurchaseFlowResult()
    class Error(val reason: String): PurchaseFlowResult()
}

sealed class PremiumCheckStatus {
    object Initializing : PremiumCheckStatus()
    object Checking : PremiumCheckStatus()
    class Error(val error: Throwable) : PremiumCheckStatus()
    object NotPremium : PremiumCheckStatus()
    object Premium : PremiumCheckStatus()
}