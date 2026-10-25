package com.riccardopinato.pocketguard.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.riccardopinato.pocketguard.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BillingUiState(
    val isPro: Boolean = false,
    val ownedByPlay: Boolean = false,
    val isPreview: Boolean = BuildConfig.PRELAUNCH_PRO_PREVIEW,
    val isReady: Boolean = false,
    val isPending: Boolean = false,
    val priceText: String? = null,
    val message: String? = null
)

class BillingRepository(
    private val context: Context
) : PurchasesUpdatedListener {

    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        BillingUiState(
            isPro =
                BuildConfig.PRELAUNCH_PRO_PREVIEW ||
                    prefs.getBoolean(KEY_PRO_OWNED, false),
            ownedByPlay = prefs.getBoolean(KEY_PRO_OWNED, false)
        )
    )
    val state: StateFlow<BillingUiState> = _state.asStateFlow()

    private var productDetails: ProductDetails? = null
    private var offerToken: String? = null

    private val billingClient =
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()

    init {
        connect()
    }

    fun refresh() {
        if (billingClient.isReady) {
            queryProducts()
            queryPurchases()
        } else {
            connect()
        }
    }

    fun launchPurchase(activity: Activity) {
        if (BuildConfig.PRELAUNCH_PRO_PREVIEW) {
            _state.value =
                _state.value.copy(
                    message = "Pro Preview attiva durante la fase di test."
                )
            return
        }

        val details = productDetails
        val token = offerToken

        if (!billingClient.isReady || details == null || token.isNullOrBlank()) {
            _state.value =
                _state.value.copy(
                    message = "Acquisto non disponibile. Riprova tra poco."
                )
            refresh()
            return
        }

        val productParams =
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(token)
                .build()

        val params =
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))
                .setIsOfferPersonalized(false)
                .build()

        val result = billingClient.launchBillingFlow(activity, params)

        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _state.value =
                _state.value.copy(
                    message = userMessage(result)
                )
        }
    }

    fun restorePurchases() {
        queryPurchases()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                processPurchases(purchases.orEmpty(), authoritative = false)

            BillingClient.BillingResponseCode.USER_CANCELED ->
                _state.value =
                    _state.value.copy(message = "Acquisto annullato.")

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                queryPurchases()

            else ->
                _state.value =
                    _state.value.copy(message = userMessage(billingResult))
        }
    }

    private fun connect() {
        if (billingClient.isReady) {
            queryProducts()
            queryPurchases()
            return
        }

        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(
                    billingResult: BillingResult
                ) {
                    if (
                        billingResult.responseCode ==
                        BillingClient.BillingResponseCode.OK
                    ) {
                        _state.value =
                            _state.value.copy(
                                isReady = true,
                                message = null
                            )
                        queryProducts()
                        queryPurchases()
                    } else {
                        _state.value =
                            _state.value.copy(
                                isReady = false,
                                message = userMessage(billingResult)
                            )
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _state.value =
                        _state.value.copy(isReady = false)
                }
            }
        )
    }

    private fun queryProducts() {
        val product =
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_PRO_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

        val params =
            QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(product))
                .build()

        billingClient.queryProductDetailsAsync(params) {
                billingResult,
                queryResult ->

            if (
                billingResult.responseCode !=
                BillingClient.BillingResponseCode.OK
            ) {
                _state.value =
                    _state.value.copy(
                        message = userMessage(billingResult)
                    )
                return@queryProductDetailsAsync
            }

            val details =
                queryResult.productDetailsList
                    .firstOrNull { it.productId == PRODUCT_PRO_LIFETIME }

            val offer =
                details
                    ?.oneTimePurchaseOfferDetailsList
                    ?.minByOrNull { it.priceAmountMicros }

            productDetails = details
            offerToken = offer?.offerToken

            _state.value =
                _state.value.copy(
                    priceText = offer?.formattedPrice,
                    isReady = true
                )
        }
    }

    private fun queryPurchases() {
        if (!billingClient.isReady) {
            connect()
            return
        }

        val params =
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

        billingClient.queryPurchasesAsync(params) {
                billingResult,
                purchases ->

            if (
                billingResult.responseCode ==
                BillingClient.BillingResponseCode.OK
            ) {
                processPurchases(
                    purchases = purchases,
                    authoritative = true
                )
            } else {
                _state.value =
                    _state.value.copy(
                        message = userMessage(billingResult)
                    )
            }
        }
    }

    private fun processPurchases(
        purchases: List<Purchase>,
        authoritative: Boolean
    ) {
        val relevant =
            purchases.filter {
                PRODUCT_PRO_LIFETIME in it.products
            }

        val purchased =
            relevant.firstOrNull {
                it.purchaseState == Purchase.PurchaseState.PURCHASED
            }

        val pending =
            relevant.any {
                it.purchaseState == Purchase.PurchaseState.PENDING
            }

        if (purchased != null) {
            setOwnedByPlay(true)

            if (!purchased.isAcknowledged) {
                val params =
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchased.purchaseToken)
                        .build()

                billingClient.acknowledgePurchase(params) { result ->
                    if (
                        result.responseCode !=
                        BillingClient.BillingResponseCode.OK
                    ) {
                        _state.value =
                            _state.value.copy(
                                message =
                                    "Pro attivo, ma la conferma acquisto verrà riprovata."
                            )
                    }
                }
            }

            _state.value =
                _state.value.copy(
                    isPro = true,
                    ownedByPlay = true,
                    isPending = false,
                    message = "PockSecure Pro è attivo."
                )
            return
        }

        if (authoritative && !pending) {
            setOwnedByPlay(false)
        }

        _state.value =
            _state.value.copy(
                isPro =
                    BuildConfig.PRELAUNCH_PRO_PREVIEW ||
                        prefs.getBoolean(KEY_PRO_OWNED, false),
                ownedByPlay = prefs.getBoolean(KEY_PRO_OWNED, false),
                isPending = pending,
                message =
                    if (pending) {
                        "Pagamento in attesa di conferma."
                    } else {
                        _state.value.message
                    }
            )
    }

    private fun setOwnedByPlay(value: Boolean) {
        prefs.edit()
            .putBoolean(KEY_PRO_OWNED, value)
            .apply()
    }

    private fun userMessage(result: BillingResult): String =
        when (result.responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                "Google Play Billing non è disponibile su questo dispositivo."
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
                "PockSecure Pro non è ancora disponibile sul tuo account."
            BillingClient.BillingResponseCode.NETWORK_ERROR ->
                "Connessione non disponibile. Riprova."
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
                "Google Play non è raggiungibile. Riprova."
            else ->
                "Acquisto non completato. Riprova."
        }

    companion object {
        const val PRODUCT_PRO_LIFETIME = "pro_lifetime"

        private const val PREFS_NAME = "pocksecure_entitlement"
        private const val KEY_PRO_OWNED = "pro_owned"
    }
}
