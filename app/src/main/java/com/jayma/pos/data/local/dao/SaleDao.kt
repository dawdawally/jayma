package com.jayma.pos.data.local.dao

import androidx.room.*
import com.jayma.pos.data.local.entities.PaymentEntity
import com.jayma.pos.data.local.entities.SaleDetailEntity
import com.jayma.pos.data.local.entities.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    
    // Sale queries
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<SaleEntity>>
    
    @Query("SELECT * FROM sales WHERE localId = :localId")
    suspend fun getSaleByLocalId(localId: Long): SaleEntity?
    
    @Query("SELECT * FROM sales WHERE serverId = :serverId")
    suspend fun getSaleByServerId(serverId: Int): SaleEntity?
    
    @Query("SELECT * FROM sales WHERE synced = 0")
    suspend fun getUnsyncedSales(): List<SaleEntity>
    
    @Query("SELECT * FROM sales WHERE synced = 0")
    fun getUnsyncedSalesFlow(): Flow<List<SaleEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long
    
    @Update
    suspend fun updateSale(sale: SaleEntity)
    
    @Delete
    suspend fun deleteSale(sale: SaleEntity)
    
    // Sale detail queries
    @Query("SELECT * FROM sale_details WHERE saleLocalId = :saleLocalId")
    suspend fun getSaleDetails(saleLocalId: Long): List<SaleDetailEntity>
    
    @Query("SELECT * FROM sale_details WHERE saleLocalId IN (:saleLocalIds)")
    suspend fun getSaleDetailsForSales(saleLocalIds: List<Long>): List<SaleDetailEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleDetails(details: List<SaleDetailEntity>)
    
    @Delete
    suspend fun deleteSaleDetails(details: List<SaleDetailEntity>)
    
    // Payment queries
    @Query("SELECT * FROM payments WHERE saleLocalId = :saleLocalId")
    suspend fun getPayments(saleLocalId: Long): List<PaymentEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)
    
    @Delete
    suspend fun deletePayments(payments: List<PaymentEntity>)
}
