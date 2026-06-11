package com.invenscan.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.invenscan.app.data.local.entity.AppLogEntity
import com.invenscan.app.data.local.entity.PendingSubmitEntity
import com.invenscan.app.data.local.entity.ScanQueueEntity
import com.invenscan.app.data.local.entity.SearchItemEntity
import com.invenscan.app.data.local.entity.StockInScanEntity
import com.invenscan.app.data.local.entity.StockOutScanEntity
import com.invenscan.app.data.local.entity.SyncStatus
import com.invenscan.app.data.local.entity.TagCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ── ScanQueue (Stock Taking offline) ─────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanQueue(entity: ScanQueueEntity): Long

    @Query("SELECT * FROM scan_queue WHERE syncStatus = :status ORDER BY scannedAt ASC")
    suspend fun getScanQueueByStatus(status: String = SyncStatus.PENDING): List<ScanQueueEntity>

    @Query("UPDATE scan_queue SET syncStatus = :status, lastError = :error, retryCount = retryCount + 1 WHERE id = :id")
    suspend fun updateScanQueueStatus(id: Long, status: String, error: String? = null)

    @Query("DELETE FROM scan_queue WHERE syncStatus = :status")
    suspend fun deleteScanQueueByStatus(status: String = SyncStatus.SYNCED)

    @Query("SELECT COUNT(*) FROM scan_queue WHERE syncStatus = :status")
    fun getScanQueueCountByStatus(status: String): Flow<Int>

    // ── PendingSubmit (Stock Prep offline) ────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSubmit(entity: PendingSubmitEntity): Long

    @Query("SELECT * FROM pending_submit WHERE syncStatus = :status ORDER BY createdAt ASC")
    suspend fun getPendingSubmitByStatus(status: String = SyncStatus.PENDING): List<PendingSubmitEntity>

    @Query("UPDATE pending_submit SET syncStatus = :status, lastError = :error, retryCount = retryCount + 1 WHERE id = :id")
    suspend fun updatePendingSubmitStatus(id: Long, status: String, error: String? = null)

    @Query("DELETE FROM pending_submit WHERE syncStatus = :status")
    suspend fun deletePendingSubmitByStatus(status: String = SyncStatus.SYNCED)

    @Query("SELECT COUNT(*) FROM pending_submit WHERE syncStatus = :status")
    fun getPendingSubmitCountByStatus(status: String): Flow<Int>

    // ── StockInScan (Stock In offline) ────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockInScan(entity: StockInScanEntity): Long

    @Query("SELECT * FROM stock_in_scan WHERE syncStatus = :status ORDER BY scannedAt ASC")
    suspend fun getStockInScanByStatus(status: String = SyncStatus.PENDING): List<StockInScanEntity>

    @Query("UPDATE stock_in_scan SET syncStatus = :status, lastError = :error, retryCount = retryCount + 1 WHERE id = :id")
    suspend fun updateStockInScanStatus(id: Long, status: String, error: String? = null)

    @Query("DELETE FROM stock_in_scan WHERE syncStatus = :status")
    suspend fun deleteStockInScanByStatus(status: String = SyncStatus.SYNCED)

    @Query("SELECT * FROM stock_in_scan WHERE docNumber = :docNumber ORDER BY scannedAt DESC")
    fun getStockInScansByDocument(docNumber: String): Flow<List<StockInScanEntity>>

    // ── StockOutScan (Stock Out offline) ──────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockOutScan(entity: StockOutScanEntity): Long

    @Query("SELECT * FROM stock_out_scan WHERE syncStatus = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingStockOutScans(): List<StockOutScanEntity>

    @Query("UPDATE stock_out_scan SET syncStatus = :status WHERE id = :id")
    suspend fun updateStockOutSyncStatus(id: Long, status: String)

    @Query("DELETE FROM stock_out_scan WHERE id = :id")
    suspend fun deleteStockOutScan(id: Long)

    @Query("SELECT * FROM stock_out_scan WHERE docNumber = :docNumber ORDER BY createdAt DESC")
    suspend fun getStockOutScansByDoc(docNumber: String): List<StockOutScanEntity>

    // ── SearchItemCache ────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchItems(items: List<SearchItemEntity>)

    @Query("SELECT * FROM search_item_cache WHERE itemCode LIKE '%' || :query || '%' OR itemName LIKE '%' || :query || '%' LIMIT 50")
    suspend fun searchItemCache(query: String): List<SearchItemEntity>

    @Query("SELECT * FROM search_item_cache WHERE itemCode = :code LIMIT 1")
    suspend fun getSearchItemByCode(code: String): SearchItemEntity?

    @Query("DELETE FROM search_item_cache WHERE cachedAt < :expiryTimestamp")
    suspend fun evictExpiredSearchItems(expiryTimestamp: Long)

    @Query("DELETE FROM search_item_cache")
    suspend fun clearSearchItemCache()

    // ── TagCache ──────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTagCache(entity: TagCacheEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTagCacheList(tags: List<TagCacheEntity>)

    @Query("SELECT * FROM tag_cache WHERE epcTag = :epc LIMIT 1")
    suspend fun getTagCacheByEpc(epc: String): TagCacheEntity?

    @Query("DELETE FROM tag_cache WHERE cachedAt < :expiryTimestamp")
    suspend fun evictExpiredTagCache(expiryTimestamp: Long)

    @Query("DELETE FROM tag_cache")
    suspend fun clearTagCache()

    // ── AppLog ────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(entity: AppLogEntity): Long

    @Query("SELECT * FROM app_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 200): Flow<List<AppLogEntity>>

    @Query("SELECT * FROM app_log WHERE level = :level ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByLevel(level: String, limit: Int = 100): Flow<List<AppLogEntity>>

    @Query("DELETE FROM app_log WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldLogs(beforeTimestamp: Long)
}
