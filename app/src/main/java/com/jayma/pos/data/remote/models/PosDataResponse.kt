package com.jayma.pos.data.remote.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class PosDataResponse(
    @SerializedName("stripe_key")
    val stripeKey: String? = null,
    @SerializedName("brands")
    val brands: List<BrandResponse>,
    @SerializedName("defaultWarehouse")
    @JsonAdapter(IntDeserializer::class)
    val defaultWarehouse: Int,
    @SerializedName("defaultClient")
    @JsonAdapter(IntDeserializer::class)
    val defaultClient: Int,
    @SerializedName("default_client_name")
    val defaultClientName: String,
    @SerializedName("clients")
    val clients: List<ClientResponse>,
    @SerializedName("warehouses")
    val warehouses: List<WarehouseResponse>,
    @SerializedName("categories")
    val categories: List<CategoryResponse>,
    @SerializedName("accounts")
    val accounts: List<AccountResponse>? = null,
    @SerializedName("payment_methods")
    val paymentMethods: List<PaymentMethodResponse>,
    @SerializedName("products_per_page")
    val productsPerPage: Int = 28
)

data class BrandResponse(
    val id: Int,
    val name: String
)

data class ClientResponse(
    val id: Int,
    val name: String
)

data class WarehouseResponse(
    val id: Int,
    val name: String
)

data class CategoryResponse(
    val id: Int,
    val name: String
)

data class AccountResponse(
    val id: Int,
    @SerializedName("account_name")
    val accountName: String
)

data class PaymentMethodResponse(
    val id: Int,
    val name: String
)
