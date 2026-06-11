package com.invenscan.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.invenscan.app.data.local.AppDao
import com.invenscan.app.data.local.entity.LogLevel
import com.invenscan.app.data.local.entity.AppLogEntity
import com.invenscan.app.data.local.entity.SyncStatus
import com.invenscan.app.data.model.StockInBulkInfoRequest
import com.invenscan.app.data.model.StockInDetailRequest
import com.invenscan.app.data.model.StockInSubmitRequest
import com.invenscan.app.data.model.StockOutDetailRequest
import com.invenscan.app.data.model.StockOutSubmitRequest
import com.invenscan.app.data.model.StockPrepPickedItem
import com.invenscan.app.data.model.StockPrepSubmitRequest
import com.invenscan.app.data.remote.ApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apiService: ApiService,
    private val appDao: AppDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            syncStockInScans()
            syncStockOutScans()
            syncPendingSubmits()
            Result.success()
        } catch (e: Exception) {
            appDao.insertLog(
                AppLogEntity(
                    level = LogLevel.ERROR,
                    tag = TAG,
                    message = "Sync failed: ${e.message}"
                )
            )
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    private suspend fun syncStockInScans() {
        val pendingScans = appDao.getStockInScanByStatus(SyncStatus.PENDING)
        if (pendingScans.isEmpty()) return

        val grouped = pendingScans.groupBy { it.docNumber }
        grouped.forEach { (docNumber, scans) ->
            val locationId = scans.first().locationId
            val notes = scans.first().notes
            val details = scans.map { scan ->
                StockInDetailRequest(
                    scannedCode = scan.scannedCode,
                    scanType = scan.scanType,
                    itemId = scan.itemId
                )
            }

            val response = apiService.submitStockIn(
                StockInSubmitRequest(
                    locationId = locationId,
                    notes = notes,
                    details = details
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                scans.forEach { scan ->
                    appDao.updateStockInScanStatus(scan.id, SyncStatus.SYNCED)
                }
            } else {
                val errorMessage = response.body()?.message ?: "HTTP ${response.code()}"
                scans.forEach { scan ->
                    appDao.updateStockInScanStatus(scan.id, SyncStatus.FAILED, errorMessage)
                }
            }
        }

        appDao.deleteStockInScanByStatus(SyncStatus.SYNCED)
    }

    private suspend fun syncStockOutScans() {
        val pendingScans = appDao.getPendingStockOutScans()
        if (pendingScans.isEmpty()) return

        val grouped = pendingScans.groupBy { it.docNumber }
        grouped.forEach { (_, scans) ->
            val locationId = scans.first().locationId
            val details = scans.map { scan ->
                StockOutDetailRequest(
                    scannedCode = scan.scannedCode,
                    scanType = scan.scanType,
                    itemId = scan.itemId
                )
            }

            val response = apiService.submitStockOut(
                StockOutSubmitRequest(
                    locationId = locationId,
                    notes = null,
                    details = details
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                scans.forEach { scan -> appDao.deleteStockOutScan(scan.id) }
            } else {
                scans.forEach { scan ->
                    appDao.updateStockOutSyncStatus(scan.id, SyncStatus.FAILED)
                }
            }
        }
    }

    private suspend fun syncPendingSubmits() {
        val pendingItems = appDao.getPendingSubmitByStatus(SyncStatus.PENDING)
        if (pendingItems.isEmpty()) return

        val grouped = pendingItems.groupBy { it.stockPrepId }
        grouped.forEach { (stockPrepId, items) ->
            val pickedItems = items.map { item ->
                StockPrepPickedItem(
                    detailId = item.stockPrepDetailId,
                    scannedCode = item.scannedCode,
                    scanType = item.scanType,
                    pickedQty = item.pickedQty
                )
            }

            val response = apiService.submitStockPrepBulk(
                StockPrepSubmitRequest(
                    stockPrepId = stockPrepId,
                    items = pickedItems
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                items.forEach { item ->
                    appDao.updatePendingSubmitStatus(item.id, SyncStatus.SYNCED)
                }
            } else {
                val errorMessage = response.body()?.message ?: "HTTP ${response.code()}"
                items.forEach { item ->
                    appDao.updatePendingSubmitStatus(item.id, SyncStatus.FAILED, errorMessage)
                }
            }
        }

        appDao.deletePendingSubmitByStatus(SyncStatus.SYNCED)
    }

    companion object {
        const val TAG = "SyncWorker"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val SYNC_INTERVAL_MINUTES = 15L

        fun buildPeriodicRequest() = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .addTag(TAG)
            .build()
    }
}
