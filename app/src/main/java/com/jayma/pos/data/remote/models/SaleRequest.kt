package com.jayma.pos.data.remote.models

import com.google.gson.annotations.SerializedName

data class CreateSaleRequest(
    @SerializedName("client_id")
    val clientId: Int,
    @SerializedName("warehouse_id")
    val warehouseId: Int,
    @SerializedName("tax_rate")
    val taxRate: Double = 0.0,
    @SerializedName("TaxNet")
    val taxNet: Double = 0.0,
    val discount: Double = 0.0,
    val shipping: Double = 0.0,
    @SerializedName("GrandTotal")
    val grandTotal: Double,
    val notes: String? = null,
    @SerializedName("po_number")
    val poNumber: String? = null,
    @SerializedName("account_id")
    val accountId: Int? = null,
    @SerializedName("payment_note")
    val paymentNote: String? = null,
    val details: List<SaleDetailRequest>,
    val payments: List<PaymentRequest>
)

data class SaleDetailRequest(
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("product_variant_id")
    val productVariantId: Int? = null,
    @SerializedName("sale_unit_id")
    val saleUnitId: Int? = null,
    val quantity: Double,
    @SerializedName("Unit_price")
    val unitPrice: Double,
    val subtotal: Double,
    @SerializedName("tax_percent")
    val taxPercent: Double = 0.0,
    @SerializedName("tax_method")
    val taxMethod: String = "1",
    val discount: Double = 0.0,
    @SerializedName("discount_Method")
    val discountMethod: String = "2",
    @SerializedName("imei_number")
    val imeiNumber: String? = null,
    val name: String
)

data class PaymentRequest(
    @SerializedName("payment_method_id")
    val paymentMethodId: Int,
    val amount: Double
)

data class CreateSaleResponse(
    val success: Boolean,
    val id: Int? = null
)
