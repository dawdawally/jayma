package com.jayma.pos.sync

import android.content.Context
import androidx.work.*
import com.jayma.pos.data.local.dao.SyncStatusDao
import com.jayma.pos.util.SharedPreferencesHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages background sync operations using WorkManager
 * Coordinates product sync and sale upload workers
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val syncStatusDao: SyncStatusDao,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) {
    
    companion object {
        private const val PRODUCT_SYNC_WORK_NAME = "product_sync_work"
        private const val SALE_UPLOAD_WORK_NAME = "sale_upload_work"
        
        // Sync intervals
        // Products: Sync every 1 hour to keep prices, stock, and new products up-to-date
        private const val PRODUCT_SYNC_INTERVAL_HOURS = 1L
        
        // Sales: Upload every 5 minutes to ensure offline sales are synced quickly
        // This ensures sales are uploaded within 5 minutes of being created offline
        private const val SALE_UPLOAD_INTERVAL_MINUTES = 5L
    }
    
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: Flow<SyncStatus> = _syncStatus.asStateFlow()
    
    /**
     * Initialize periodic sync workers
     */
    fun initializePeriodicSync() {
        setupProductSync()
        setupSaleUpload()
    }
    
    /**
     * Setup periodic product sync
     */
    private fun setupProductSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<ProductSyncWorker>(
            PRODUCT_SYNC_INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(PRODUCT_SYNC_WORK_NAME)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            PRODUCT_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    /**
     * Setup periodic sale upload
     */
    private fun setupSaleUpload() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val uploadRequest = PeriodicWorkRequestBuilder<SaleUploadWorker>(
            SALE_UPLOAD_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(SALE_UPLOAD_WORK_NAME)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SALE_UPLOAD_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            uploadRequest
        )
    }
    
    /**
     * Trigger immediate product sync
     */
    fun triggerProductSync(): Operation {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<ProductSyncWorker>()
            .setConstraints(constraints)
            .addTag(PRODUCT_SYNC_WORK_NAME)
            .build()
        
        return workManager.enqueue(syncRequest)
    }
    
    /**
     * Trigger immediate sale upload
     */
    fun triggerSaleUpload(): Operation {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val uploadRequest = OneTimeWorkRequestBuilder<SaleUploadWorker>()
            .setConstraints(constraints)
            .addTag(SALE_UPLOAD_WORK_NAME)
            .build()
        
        return workManager.enqueue(uploadRequest)
    }
    
    /**
     * Cancel all sync operations
     */
    fun cancelAllSync() {
        workManager.cancelUniqueWork(PRODUCT_SYNC_WORK_NAME)
        workManager.cancelUniqueWork(SALE_UPLOAD_WORK_NAME)
    }
    
    /**
     * Get sync status
     */
    suspend fun getSyncStatus(): SyncStatus {
        val status = syncStatusDao.getSyncStatus()
        return if (status != null) {
            SyncStatus.Syncing(
                lastProductSync = status.lastProductSync,
                lastSaleSync = status.lastSaleSync,
                inProgress = status.syncInProgress
            )
        } else {
            SyncStatus.Idle
        }
    }
}

/**
 * Represents the current sync status
 */
sealed class SyncStatus {
    object Idle : SyncStatus()
    data class Syncing(
        val lastProductSync: Long,
        val lastSaleSync: Long,
        val inProgress: Boolean
    ) : SyncStatus()
}
