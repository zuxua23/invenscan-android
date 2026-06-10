package com.invenscan.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tag_cache",
    indices = [Index(value = ["epcTag"], unique = true)]
)
data class TagCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tagId: Long,
    val epcTag: String,
    val itemId: Long,
    val itemCode: String,
    val itemName: String,
    val locationId: Long,
    val locationName: String,
    val status: String,
    val cachedAt: Long = System.currentTimeMillis()
)
