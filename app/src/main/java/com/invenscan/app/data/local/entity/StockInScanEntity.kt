package com.invenscan.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_in_scan")
data class StockInScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val docNumber: String,
    val locationId: Long,
    val scannedCode: String,
    val scanType: String,
    val itemId: Long? = null,
    val itemName: String? = null,
    val notes: String? = null,
    val createdBy: String,
    val scannedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING,
    val retryCount: Int = 0,
    val lastError: String? = null
)
