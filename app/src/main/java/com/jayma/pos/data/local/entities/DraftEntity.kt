package com.jayma.pos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val serverId: Int? = null, // ID from server after sync
    val clientId: Int,
    val warehouseId: Int,
    val taxRate: Double = 0.0,
    val taxNet: Double = 0.0,
    val discount: Double = 0.0,
    val shipping: Double = 0.0,
    val grandTotal: Double,
    val notes: String? = null,
    val poNumber: String? = null,
    val accountId: Int? = null,
    val paymentNote: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
