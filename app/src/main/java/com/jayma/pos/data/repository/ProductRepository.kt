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
        val productsPerPage = 28 // From API documentation
        
        while (hasMore) {
            val result = syncProducts(warehouseId, page)
            result.fold(
                onSuccess = { count ->
                    totalSynced += count
                    // If we got less than productsPerPage, we're done
                    hasMore = count >= productsPerPage
                    if (hasMore) {
                        page++
                    }
                },
                onFailure = { error ->
                    return Result.failure(error)
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
