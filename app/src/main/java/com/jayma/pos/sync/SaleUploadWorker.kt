package com.jayma.pos.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.jayma.pos.data.repository.SaleRepository
import com.jayma.pos.data.local.dao.SyncStatusDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager Worker for uploading unsynced sales to the server
 * Runs when network is available and there are unsynced sales
 */
@HiltWorker
class SaleUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val saleRepository: SaleRepository,
    private val syncStatusDao: SyncStatusDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get all unsynced sales
            val unsyncedSales = saleRepository.getUnsyncedSales()

            if (unsyncedSales.isEmpty()) {
                return@withContext Result.success(workDataOf("message" to "No unsynced sales"))
            }

            var successCount = 0
            var failureCount = 0

            // Upload each unsynced sale
            for (sale in unsyncedSales) {
                try {
                    val details = saleRepository.getSaleDetails(sale.localId)
                    val payments = saleRepository.getPayments(sale.localId)

                    val result = saleRepository.syncSale(sale, details, payments)

                    result.fold(
                        onSuccess = {
                            successCount++
                        },
                        onFailure = { error ->
                            failureCount++
                            // Continue with next sale even if one fails
                        }
                    )
                } catch (e: Exception) {
                    failureCount++
                    // Continue with next sale
                }
            }

            // Update last sync timestamp
            if (successCount > 0) {
                val timestamp = System.currentTimeMillis()
                syncStatusDao.updateLastSaleSync(timestamp)
            }

            // Return success if at least one sale was uploaded
            // Otherwise retry
            if (successCount > 0) {
                Result.success(
                    workDataOf(
                        "success_count" to successCount,
                        "failure_count" to failureCount
                    )
                )
            } else {
                Result.retry() // Retry if all failed
            }
        } catch (e: Exception) {
            Result.retry() // Retry on exception
        }
    }
}
