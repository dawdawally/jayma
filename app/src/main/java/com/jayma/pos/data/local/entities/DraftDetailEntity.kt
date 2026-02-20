package com.jayma.pos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Draft detail (line item) - separate table with foreign key
@Entity(
    tableName = "draft_details",
    foreignKeys = [
        ForeignKey(
            entity = DraftEntity::class,
            parentColumns = ["localId"],
            childColumns = ["draftLocalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["draftLocalId"])
    ]
)
data class DraftDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val draftLocalId: Long, // Foreign key to DraftEntity
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
    val productName: String // Store product name for offline
)
