package com.invenscan.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_submit")
data class PendingSubmitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stockPrepId: Long,
    val stockPrepDetailId: Long,
    val tagId: String,
    val itemId: Long,
    val scannedCode: String,
    val scanType: String,
    val pickedQty: Int,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING,
    val retryCount: Int = 0,
    val lastError: String? = null
)
