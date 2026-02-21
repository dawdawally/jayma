package com.jayma.pos.data.repository

import com.jayma.pos.data.local.dao.ProductDao
import com.jayma.pos.data.local.entities.ProductEntity
import com.jayma.pos.data.remote.models.ProductResponse
import com.jayma.pos.data.remote.services.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val apiService: ApiService
) {
    
    // Offline-first: Always read from local database
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()
    
    suspend fun getProductById(id: Int): ProductEntity? = productDao.getProductById(id)
    
    suspend fun getProductByCode(code: String): ProductEntity? = productDao.getProductByCode(code)
    
    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts("%$query%")
    
    fun getProductsByCategory(categoryId: Int): Flow<List<ProductEntity>> = 
        productDao.getProductsByCategory(categoryId)
    
    fun getProductsByBrand(brandId: Int): Flow<List<ProductEntity>> = 
        productDao.getProductsByBrand(brandId)
    
    fun getInStockProducts(): Flow<List<ProductEntity>> = productDao.getInStockProducts()
    
    // Sync operations
    suspend fun syncProducts(warehouseId: Int, page: Int = 1): Result<Int> {
        return try {
            val response = apiService.getProducts(warehouseId = warehouseId, page = page)
            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.products.map { it.toEntity() }
                productDao.insertProducts(products)
                Result.success(products.size)
            } else {
                Result.failure(Exception("Failed to fetch products: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncAllProducts(warehouseId: Int): Result<Int> {
        var totalSynced = 0
        var page = 1
        var hasMore = true
        val productsPerPage = 15 // From API documentation
        var consecutiveFailures = 0
        val maxConsecutiveFailures = 3
        
        while (hasMore) {
            val result = syncProducts(warehouseId, page)
            result.fold(
                onSuccess = { count ->
                    consecutiveFailures = 0 // Reset failure counter on success
                    totalSynced += count
                    // If we got less than productsPerPage, we're done
                    hasMore = count >= productsPerPage
                    if (hasMore) {
                        page++
                    }
                },
                onFailure = { error ->
                    consecutiveFailures++
                    // Retry up to maxConsecutiveFailures times
                    if (consecutiveFailures >= maxConsecutiveFailures) {
                        return Result.failure(error)
                    }
                    // Continue to next page on failure (might be temporary network issue)
                    page++
                    if (page > 100) { // Safety limit
                        return Result.failure(Exception("Too many pages, possible infinite loop"))
                    }
                }
            )
        }
        
        return Result.success(totalSynced)
    }
    
    /**
     * Sync first page of products immediately for instant feedback
     * Returns the number of products synced from the first page
     */
    suspend fun syncFirstPage(warehouseId: Int): Result<Int> {
        return syncProducts(warehouseId, page = 1)
    }
    
    /**
     * Sync remaining products in background (starting from page 2)
     * This should be called after syncFirstPage to load the rest
     */
    suspend fun syncRemainingProducts(warehouseId: Int): Result<Int> {
        var totalSynced = 0
        var page = 2 // Start from page 2
        var hasMore = true
        val productsPerPage = 15
        var consecutiveFailures = 0
        val maxConsecutiveFailures = 3
        
        while (hasMore) {
            val result = syncProducts(warehouseId, page)
            result.fold(
                onSuccess = { count ->
                    consecutiveFailures = 0
                    totalSynced += count
                    hasMore = count >= productsPerPage
                    if (hasMore) {
                        page++
                    }
                },
                onFailure = { error ->
                    consecutiveFailures++
                    if (consecutiveFailures >= maxConsecutiveFailures) {
                        return Result.failure(error)
                    }
                    page++
                    if (page > 100) {
                        return Result.failure(Exception("Too many pages, possible infinite loop"))
                    }
                }
            )
        }
        
        return Result.success(totalSynced)
    }
    
    private fun ProductResponse.toEntity(): ProductEntity {
        return ProductEntity(
            id = id,
            productVariantId = productVariantId,
            code = code,
            name = name,
            barcode = barcode,
            image = image,
            qte = qte,
            qteSale = qteSale,
            unitSale = unitSale,
            productType = productType,
            netPrice = netPrice,
            synced = true
        )
    }
}
