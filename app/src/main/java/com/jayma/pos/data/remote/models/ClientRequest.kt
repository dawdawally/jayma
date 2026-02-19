package com.jayma.pos.data.remote.models

import com.google.gson.annotations.SerializedName

data class CreateClientRequest(
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val adresse: String? = null,
    val country: String? = null,
    val city: String? = null,
    @SerializedName("tax_number")
    val taxNumber: String? = null
)

data class CreateClientResponse(
    val success: Boolean
)
