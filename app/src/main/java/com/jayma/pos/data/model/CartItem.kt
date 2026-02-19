package com.jayma.pos.data.model

import com.jayma.pos.data.local.entities.ProductEntity

data class CartItem(
    val product: ProductEntity,
    var quantity: Double,
    val unitPrice: Double = product.netPrice
) {
    val subtotal: Double
        get() = quantity * unitPrice
    
    fun updateQuantity(newQuantity: Double): CartItem {
        return copy(quantity = newQuantity.coerceAtLeast(0.0))
    }
}
