package com.jayma.pos.sync

import com.jayma.pos.data.local.dao.SyncStatusDao
import com.jayma.pos.data.local.entities.SyncStatusEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Initializes sync status and periodic sync workers
 * Should be called from MainActivity after Hilt injection is ready
 */
@Singleton
class SyncInitializer @Inject constructor(
    private val syncStatusDao: SyncStatusDao,
    private val syncManager: SyncManager
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    fun initialize() {
        scope.launch {
            // Initialize sync status if it doesn't exist
            val status = syncStatusDao.getSyncStatus()
            if (status == null) {
                syncStatusDao.insertSyncStatus(
                    SyncStatusEntity(
                        id = 1,
                        lastProductSync = 0,
                        lastSaleSync = 0,
                        lastDraftSync = 0,
                        syncInProgress = false
                    )
                )
            }
            
            // Initialize periodic sync workers
            syncManager.initializePeriodicSync()
        }
    }
}
