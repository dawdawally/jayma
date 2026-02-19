package com.jayma.pos.data.local.dao

import androidx.room.*
import com.jayma.pos.data.local.entities.DraftDetailEntity
import com.jayma.pos.data.local.entities.DraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {
    
    // Draft queries
    @Query("SELECT * FROM drafts ORDER BY updatedAt DESC")
    fun getAllDrafts(): Flow<List<DraftEntity>>
    
    @Query("SELECT * FROM drafts WHERE localId = :localId")
    suspend fun getDraftByLocalId(localId: Long): DraftEntity?
    
    @Query("SELECT * FROM drafts WHERE synced = 0")
    suspend fun getUnsyncedDrafts(): List<DraftEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: DraftEntity): Long
    
    @Update
    suspend fun updateDraft(draft: DraftEntity)
    
    @Delete
    suspend fun deleteDraft(draft: DraftEntity)
    
    @Query("DELETE FROM drafts WHERE localId = :localId")
    suspend fun deleteDraftById(localId: Long)
    
    // Draft detail queries
    @Query("SELECT * FROM draft_details WHERE draftLocalId = :draftLocalId")
    suspend fun getDraftDetails(draftLocalId: Long): List<DraftDetailEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraftDetails(details: List<DraftDetailEntity>)
    
    @Delete
    suspend fun deleteDraftDetails(details: List<DraftDetailEntity>)
    
    @Query("DELETE FROM draft_details WHERE draftLocalId = :draftLocalId")
    suspend fun deleteDraftDetailsByDraftId(draftLocalId: Long)
}
