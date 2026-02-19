package com.jayma.pos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        androidx.room.Index(value = ["serverId"]),
        androidx.room.Index(value = ["clientId"]),
        androidx.room.Index(value = ["warehouseId"]),
        androidx.room.Index(value = ["synced"]),
        androidx.room.Index(value = ["createdAt"])
    ]
)
data class SaleEntity(
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
    val synced: Boolean = false
)

// Sale detail (line item) - separate table with foreign key
@Entity(
    tableName = "sale_details",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["localId"],
            childColumns = ["saleLocalId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["saleLocalId"]),
        androidx.room.Index(value = ["productId"])
    ]
)
data class SaleDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleLocalId: Long, // Foreign key to SaleEntity
    val productId: Int,
    val productVariantId: Int? = null,
    val saleUnitId: Int? = null,
    val quantity: Double,
    val unitPrice: Double,
    val subtotal: Double,
    val taxPercent: Double = 0.0,
    val taxMethod: String = "1",
    val discount: Double = 0.0,
    val discountMethod: String = "2",
    val imeiNumber: String? = null,
    val productName: String // Store product name for offline receipts
)

// Payment entity - separate table with foreign key
@Entity(
    tableName = "payments",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["localId"],
            childColumns = ["saleLocalId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["saleLocalId"])
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleLocalId: Long, // Foreign key to SaleEntity
    val paymentMethodId: Int,
    val amount: Double,
    val change: Double = 0.0,
    val notes: String? = null,
    val accountId: Int? = null
)
