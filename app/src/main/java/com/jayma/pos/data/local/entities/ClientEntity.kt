package com.jayma.pos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val country: String? = null,
    val city: String? = null,
    val taxNumber: String? = null,
    val isDefault: Boolean = false // For "Walk-in" client
)
