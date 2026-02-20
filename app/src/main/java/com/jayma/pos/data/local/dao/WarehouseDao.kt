package com.jayma.pos.data.local.dao

import androidx.room.*
import com.jayma.pos.data.local.entities.WarehouseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {
    
    @Query("SELECT * FROM warehouses ORDER BY name ASC")
    fun getAllWarehouses(): Flow<List<WarehouseEntity>>
    
    @Query("SELECT * FROM warehouses WHERE id = :id")
    suspend fun getWarehouseById(id: Int): WarehouseEntity?
    
    @Query("SELECT * FROM warehouses WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultWarehouse(): WarehouseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: WarehouseEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouses(warehouses: List<WarehouseEntity>)
    
    @Update
    suspend fun updateWarehouse(warehouse: WarehouseEntity)
    
    @Delete
    suspend fun deleteWarehouse(warehouse: WarehouseEntity)
    
    @Query("DELETE FROM warehouses")
    suspend fun deleteAllWarehouses()
}
