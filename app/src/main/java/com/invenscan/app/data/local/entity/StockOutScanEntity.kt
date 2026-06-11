package com.invenscan.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_out_scan")
data class StockOutScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val docNumber: String,
    val locationId: Long,
    val locationName: String,
    val tagId: String? = null,
    val itemId: Long? = null,
    val itemName: String? = null,
    val itemCode: String? = null,
    val scannedCode: String,
    val scanType: String,
    val syncStatus: String = SyncStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
