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
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * SKU premium
 */
private const val SKU_PREMIUM = "premium"

/**
 * SKUs donation
 */
const val SKU_DONATION_TIER_1 = "donation_tier_1"
const val SKU_DONATION_TIER_2 = "donation_tier_2"
const val SKU_DONATION_TIER_3 = "donation_tier_3"
const val SKU_DONATION_TIER_4 = "donation_tier_4"
const val SKU_DONATION_TIER_5 = "donation_tier_5"

class BillingImpl @Inject constructor(
    @ApplicationContext context: Context,
) : Billing, PurchasesUpdatedListener, BillingClientStateListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pendingPremiumPurchaseEventMutableFlow = MutableSharedFlow<PurchaseFlowResult>()
    private val pendingDonationEventMutableFlow = MutableSharedFlow<PurchaseFlowResult>()

    private var queryPurchasesJob: Job? = null

    private val appContext = context.applicationContext
    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    /**
     * iab check status
     */
    private var iabStatus: PremiumCheckStatus = PremiumCheckStatus.Initializing

    override val userPremiumEventStream: Flow<PremiumCheckStatus>
        get() = userPremiumEventSteamInternal

    private val userPremiumEventSteamInternal = MutableStateFlow<PremiumCheckStatus>(PremiumCheckStatus.Initializing)

    init {
        startBillingClient()
    }

    private fun startBillingClient() {
        try {
            setIabStatusAndNotify(PremiumCheckStatus.Initializing)

            billingClient.startConnection(this)
        } catch (e: Exception) {
            Log.e("BillingImpl", "Error while checking iab status", e)
            setIabStatusAndNotify(PremiumCheckStatus.Error(e))
        }
    }

    /**
     * Set the new iab status and notify the app by an event
     *
     * @param status the new status
     */
    private fun setIabStatusAndNotify(status: PremiumCheckStatus) {
        iabStatus = status
        userPremiumEventSteamInternal.value = status
    }

    /**
     * Is the user a premium user
     *
     * @return true if the user if premium, false otherwise
     */
    override fun isUserPremium(): Boolean {
        return iabStatus == PremiumCheckStatus.Premium
    }

    /**
     * Update the current IAP status if already checked
     */
    override fun updatePremiumStatusIfNeeded() {
        Log.d("BillingImpl", "updateIAPStatusIfNeeded: $iabStatus")

        if ( iabStatus == PremiumCheckStatus.NotPremium ) {
            setIabStatusAndNotify(PremiumCheckStatus.Checking)
            queryPurchases()
        } else if ( iabStatus is PremiumCheckStatus.Error) {
            startBillingClient()
        }
    }

    private fun queryPurchases() {
        queryPurchasesJob?.cancel()
        queryPurchasesJob = scope.launch {
            val result = billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build())

            Log.d("BillingImpl", "iab query inventory finished.")

            // Is it a failure?
            if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.e("BillingImpl", "Error while querying iab inventory: " + result.billingResult.responseCode)
                setIabStatusAndNotify(PremiumCheckStatus.Error(Exception("Error while querying iab inventory: " + result.billingResult.responseCode)))
                return@launch
            }

            val premium = result.purchasesList.any { it.products.contains(SKU_PREMIUM) }

            Log.d("BillingImpl", "iab query inventory was successful: $premium")

            setIabStatusAndNotify(if (premium) PremiumCheckStatus.Premium else PremiumCheckStatus.NotPremium)
        }
    }

    /**
     * Launch the premium purchase flow
     *
     * @param activity activity that started this purchase
     */
    override suspend fun launchPremiumPurchaseFlow(activity: Activity): PurchaseFlowResult {
        if ( iabStatus != PremiumCheckStatus.NotPremium ) {
            return when (iabStatus) {
                is PremiumCheckStatus.Error -> PurchaseFlowResult.Error("Unable to connect to your Google account. Please restart the app and try again")
                PremiumCheckStatus.Premium -> PurchaseFlowResult.Error("You already bought Premium with that Google account. Restart the app if you don't have access to premium features.")
                else -> PurchaseFlowResult.Error("Runtime error: $iabStatus")
            }
        }

        val skuList = listOf(
            QueryProductDetailsParams.Product
                .newBuilder()
                .setProductId(SKU_PREMIUM)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val (billingResult, skuDetailsList) = billingClient.queryProductDetails(
            QueryProductDetailsParams.newBuilder()
                .setProductList(skuList)
                .build()
        )

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                setIabStatusAndNotify(PremiumCheckStatus.Premium)
                return PurchaseFlowResult.Success
            }

            return PurchaseFlowResult.Error("Unable to connect to reach PlayStore (response code: " + billingResult.responseCode + "). Please restart the app and try again")
        }

        if (skuDetailsList == null || skuDetailsList.isEmpty()) {
            return PurchaseFlowResult.Error("Unable to fetch content from PlayStore (response code: skuDetailsList is empty). Please restart the app and try again")
        }

        val productDetailsParamsList =
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(skuDetailsList[0])
                    .build()
            )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)

        return pendingPremiumPurchaseEventMutableFlow.first()
    }

    override suspend fun getDonationsProductDetails(): List<ProductDetails> {
        if( iabStatus is PremiumCheckStatus.Initializing || iabStatus is PremiumCheckStatus.Error ) {
            throw IllegalStateException("IAB is not setup")
        }

        val skuList = listOf(
            SKU_DONATION_TIER_1,
            SKU_DONATION_TIER_2,
            SKU_DONATION_TIER_3,
            SKU_DONATION_TIER_4,
            SKU_DONATION_TIER_5,
        )

        val productList = skuList.map { productId ->
            QueryProductDetailsParams.Product
                .newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val (billingResult, skuDetailsList) = billingClient.queryProductDetails(
            QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()
        )

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            throw IllegalStateException("Unable to connect to reach PlayStore (response code: " + billingResult.responseCode + "). Please restart the app and try again")
        }

        if( skuDetailsList == null ) {
            throw IllegalStateException("Unable to get details from PlayStore. Please restart the app and try again")
        }

        return skuDetailsList
    }

    override suspend fun launchDonationPurchaseFlow(activity: Activity, sku: ProductDetails): Boolean {
        val productDetailsParamsList =
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(sku)
                    .build()
            )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)

        return when(val result = pendingDonationEventMutableFlow.first()) {
            PurchaseFlowResult.Success -> true
            PurchaseFlowResult.Cancelled -> false
            is PurchaseFlowResult.Error -> throw IllegalStateException(result.reason)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        Log.d("BillingImpl", "iab setup finished.")

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            // Oh noes, there was a problem.
            setIabStatusAndNotify(PremiumCheckStatus.Error(Exception("Error while setting-up iab: ${billingResult.responseCode}, ${billingResult.debugMessage}")))
            Log.e("BillingImpl","Error while setting-up iab: ${billingResult.responseCode}, ${billingResult.debugMessage}")
            return
        }

        setIabStatusAndNotify(PremiumCheckStatus.Checking)
        queryPurchases()
    }

    override fun onBillingServiceDisconnected() {
        Log.d("BillingImpl", "onBillingServiceDisconnected")

        scope.launch {
            broadcastPurchaseFlowResult(PurchaseFlowResult.Error("Lost connection with Google Play"))
        }

        setIabStatusAndNotify(PremiumCheckStatus.Error(IOException("Lost connection with Google Play")))
    }

    private suspend fun broadcastPurchaseFlowResult(result: PurchaseFlowResult) {
        pendingPremiumPurchaseEventMutableFlow.emit(result)
        pendingDonationEventMutableFlow.emit(result)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        scope.launch {
            Log.d("BillingImpl", "Purchase finished: " + billingResult.responseCode)

            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.e(
                    "BillingImpl",
                    "Error while purchasing premium: ${billingResult.responseCode}, ${billingResult.debugMessage}"
                )
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        broadcastPurchaseFlowResult(PurchaseFlowResult.Cancelled)
                    }
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        // Can happen only for premium SKU
                        setIabStatusAndNotify(PremiumCheckStatus.Premium)
                        pendingPremiumPurchaseEventMutableFlow.emit(PurchaseFlowResult.Success)
                        return@launch
                    }
                    else -> broadcastPurchaseFlowResult(PurchaseFlowResult.Error("An error occurred (status code: ${billingResult.responseCode}, ${billingResult.debugMessage})"))
                }

                return@launch
            }

            if (purchases.isNullOrEmpty()) {
                broadcastPurchaseFlowResult(PurchaseFlowResult.Error("No purchased item found"))
                return@launch
            }

            Log.d("BillingImpl", "Purchase successful.")

            for (purchase in purchases) {
                if (purchase.products.contains(SKU_PREMIUM)) {
                    Log.d("BillingImpl", "Acknowledging premium")

                    val ackResult = billingClient.acknowledgePurchase(
                        AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken).build()
                    )

                    Log.d("BillingImpl", "Acknowledge result: ${ackResult.responseCode}")

                    if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        pendingPremiumPurchaseEventMutableFlow.emit(PurchaseFlowResult.Error("Error when acknowledging purchase with Google (${billingResult.responseCode}, ${billingResult.debugMessage}). Please try again"))
                        return@launch
                    }

                    setIabStatusAndNotify(PremiumCheckStatus.Premium)
                    pendingPremiumPurchaseEventMutableFlow.emit(PurchaseFlowResult.Success)
                } else {
                    Log.d("BillingImpl", "Consuming donation")

                    val consumeParams =
                        ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()

                    val consumeResult = billingClient.consumePurchase(consumeParams)

                    if (consumeResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        pendingDonationEventMutableFlow.emit(PurchaseFlowResult.Success)
                    } else {
                        pendingDonationEventMutableFlow.emit(PurchaseFlowResult.Error("Error when consuming purchase with Google (${billingResult.responseCode}, ${billingResult.debugMessage}). Please try again"))
                    }
                }
            }
        }
    }
}