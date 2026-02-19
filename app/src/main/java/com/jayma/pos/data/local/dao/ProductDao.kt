package com.jayma.pos.data.local.dao

import androidx.room.*
import com.jayma.pos.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?
    
    @Query("SELECT * FROM products WHERE code = :code OR barcode = :code")
    suspend fun getProductByCode(code: String): ProductEntity?
    
    @Query("SELECT * FROM products WHERE name LIKE :query OR code LIKE :query OR barcode LIKE :query")
    fun searchProducts(query: String): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    fun getProductsByCategory(categoryId: Int): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE brandId = :brandId")
    fun getProductsByBrand(brandId: Int): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE qteSale > 0")
    fun getInStockProducts(): Flow<List<ProductEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)
    
    @Update
    suspend fun updateProduct(product: ProductEntity)
    
    @Delete
    suspend fun deleteProduct(product: ProductEntity)
    
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
    
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}
