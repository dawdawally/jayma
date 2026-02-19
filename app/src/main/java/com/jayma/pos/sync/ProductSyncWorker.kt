package com.jayma.pos.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.jayma.pos.data.repository.ProductRepository
import com.jayma.pos.data.local.dao.SyncStatusDao
import com.jayma.pos.util.SharedPreferencesHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager Worker for syncing products from API to local database
 * Runs periodically when network is available
 */
@HiltWorker
class ProductSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val productRepository: ProductRepository,
    private val syncStatusDao: SyncStatusDao,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val warehouseId = sharedPreferencesHelper.getDefaultWarehouse()
                ?: return Result.failure(workDataOf("error" to "No warehouse configured"))

            // Mark sync as in progress
            syncStatusDao.setSyncInProgress(true)

            // Sync all products
            val result = productRepository.syncAllProducts(warehouseId)

            result.fold(
                onSuccess = { count ->
                    // Update last sync timestamp
                    val timestamp = System.currentTimeMillis()
                    syncStatusDao.updateLastProductSync(timestamp)
                    sharedPreferencesHelper.saveLastSyncTimestamp(timestamp)
                    syncStatusDao.setSyncInProgress(false)

                    Result.success(
                        workDataOf(
                            "synced_count" to count,
                            "timestamp" to timestamp
                        )
                    )
                },
                onFailure = { error ->
                    syncStatusDao.setSyncInProgress(false)
                    Result.retry() // Retry on failure
                }
            )
        } catch (e: Exception) {
            syncStatusDao.setSyncInProgress(false)
            Result.retry() // Retry on exception
        }
    }
}
