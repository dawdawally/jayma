package com.jayma.pos.util

import com.jayma.pos.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Helper utility for stock management and alerts
 */
object StockAlertHelper {
    
    /**
     * Default low stock threshold (10% of available stock or minimum 5 units)
     */
    private const val DEFAULT_LOW_STOCK_THRESHOLD = 0.1
    
    /**
     * Check if product is low in stock
     */
    fun isLowStock(product: ProductEntity, threshold: Double = DEFAULT_LOW_STOCK_THRESHOLD): Boolean {
        // Consider low stock if quantity is below threshold
        // For now, use a simple check: less than 10 units
        return product.qteSale < 10.0
    }
    
    /**
     * Get low stock products from a list
     */
    fun getLowStockProducts(products: List<ProductEntity>): List<ProductEntity> {
        return products.filter { isLowStock(it) }
    }
    
    /**
     * Filter low stock products from a Flow
     */
    fun filterLowStockProducts(productsFlow: Flow<List<ProductEntity>>): Flow<List<ProductEntity>> {
        return productsFlow.map { products -> getLowStockProducts(products) }
    }
    
    /**
     * Get stock status message
     */
    fun getStockStatusMessage(product: ProductEntity): String {
        return when {
            product.qteSale <= 0 -> "Out of Stock"
            isLowStock(product) -> "Low Stock (${product.qteSale.toInt()} remaining)"
            else -> "In Stock (${product.qteSale.toInt()} available)"
        }
    }
}
