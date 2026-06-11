package com.invenscan.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_log")
data class AppLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val level: String,
    val tag: String,
    val message: String,
    val userId: String? = null,
    val deviceId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object LogLevel {
    const val INFO = "INFO"
    const val WARN = "WARN"
    const val ERROR = "ERROR"
    const val DEBUG = "DEBUG"
}
