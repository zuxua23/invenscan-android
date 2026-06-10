package com.invenscan.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_queue")
data class ScanQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sttId: Long,
    val tagId: String,
    val itemId: Long,
    val action: String,
    val scannedAt: Long = System.currentTimeMillis(),
    val createdBy: String,
    val syncStatus: String = SyncStatus.PENDING,
    val retryCount: Int = 0,
    val lastError: String? = null
)

object SyncStatus {
    const val PENDING = "PENDING"
    const val SYNCED = "SYNCED"
    const val FAILED = "FAILED"
}
