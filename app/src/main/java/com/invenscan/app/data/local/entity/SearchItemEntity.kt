package com.invenscan.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_item_cache",
    indices = [Index(value = ["itemCode"], unique = true)]
)
data class SearchItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val itemCode: String,
    val itemName: String,
    val description: String? = null,
    val unit: String? = null,
    val minStock: Int = 0,
    val locationName: String? = null,
    val status: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
