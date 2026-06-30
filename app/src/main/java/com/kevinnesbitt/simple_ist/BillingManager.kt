package com.kevinnesbitt.simple_ist

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BillingManager(context: Context) {

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium

    private val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            // Handles real-time purchases if they buy it while the app is open
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                processPurchases(purchases)
            }
        }
        .enablePendingPurchases()
        .build()

    init {
        startBillingConnection()
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Connection successful! Now check if they own premium.
                    checkActivePurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to reconnect later if needed
            }
        })
    }

    fun checkActivePurchases() {
        // Query for normal one-time lifetime purchases ("inapp")
        // If your premium is a subscription, change ProductType.INAPP to ProductType.SUBS
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchasesList)
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        // Check if any of the active purchases match your premium product ID
        val hasPremium = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    purchase.products.contains("premium")
        }
        _isPremium.value = hasPremium
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        // 1. Specify the product details we want to buy
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP) // or .SUBS for subscriptions
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        // 2. Fetch details from Google Play first, then open the screen
        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList.first()

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                    )
                    .build()

                // 3. This opens up the official Google Play secure checkout window
                billingClient.launchBillingFlow(activity, billingFlowParams)
            }
        }
    }
}