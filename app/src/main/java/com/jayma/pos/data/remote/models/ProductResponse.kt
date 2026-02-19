package com.jayma.pos.data.remote.models

import com.google.gson.annotations.SerializedName

data class ProductListResponse(
    val products: List<ProductResponse>,
    @SerializedName("totalRows")
    val totalRows: Int
)

data class ProductResponse(
    val id: Int,
    @SerializedName("product_variant_id")
    val productVariantId: Int? = null,
    val code: String,
    val name: String,
    val barcode: String?,
    val image: String?,
    @SerializedName("qte_sale")
    val qteSale: Double,
    val qte: Double,
    @SerializedName("unitSale")
    val unitSale: String,
    @SerializedName("product_type")
    val productType: String,
    @SerializedName("Net_price")
    val netPrice: Double
)
