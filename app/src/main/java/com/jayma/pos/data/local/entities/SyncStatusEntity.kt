package com.jayma.pos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_status")
data class SyncStatusEntity(
    @PrimaryKey
    val id: Int = 1, // Single row
    val lastProductSync: Long = 0,
    val lastSaleSync: Long = 0,
    val lastDraftSync: Long = 0,
    val syncInProgress: Boolean = false
)
