package com.jayma.pos.data.local.dao

import androidx.room.*
import com.jayma.pos.data.local.entities.SyncStatusEntity

@Dao
interface SyncStatusDao {
    
    @Query("SELECT * FROM sync_status WHERE id = 1")
    suspend fun getSyncStatus(): SyncStatusEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncStatus(syncStatus: SyncStatusEntity)
    
    @Update
    suspend fun updateSyncStatus(syncStatus: SyncStatusEntity)
    
    @Query("UPDATE sync_status SET lastProductSync = :timestamp WHERE id = 1")
    suspend fun updateLastProductSync(timestamp: Long)
    
    @Query("UPDATE sync_status SET lastSaleSync = :timestamp WHERE id = 1")
    suspend fun updateLastSaleSync(timestamp: Long)
    
    @Query("UPDATE sync_status SET lastDraftSync = :timestamp WHERE id = 1")
    suspend fun updateLastDraftSync(timestamp: Long)
    
    @Query("UPDATE sync_status SET syncInProgress = :inProgress WHERE id = 1")
    suspend fun setSyncInProgress(inProgress: Boolean)
}
