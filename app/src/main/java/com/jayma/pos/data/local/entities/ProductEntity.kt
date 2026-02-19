package com.jayma.pos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        androidx.room.Index(value = ["code"]),
        androidx.room.Index(value = ["barcode"]),
        androidx.room.Index(value = ["categoryId"]),
        androidx.room.Index(value = ["brandId"]),
        androidx.room.Index(value = ["synced"])
    ]
)
data class ProductEntity(
    @PrimaryKey
    val id: Int,
    val productVariantId: Int? = null,
    val code: String,
    val name: String,
    val barcode: String?,
    val image: String?,
    val qte: Double, // Quantity available
    val qteSale: Double, // Quantity for sale
    val unitSale: String,
    val productType: String, // "is_single", "is_combo", "is_service"
    val netPrice: Double, // Selling price
    val costPrice: Double? = null, // Cost price (if available)
    val categoryId: Int? = null,
    val brandId: Int? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = true
)
