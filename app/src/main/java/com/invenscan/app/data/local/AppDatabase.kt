package com.invenscan.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.invenscan.app.data.local.entity.AppLogEntity
import com.invenscan.app.data.local.entity.PendingSubmitEntity
import com.invenscan.app.data.local.entity.ScanQueueEntity
import com.invenscan.app.data.local.entity.SearchItemEntity
import com.invenscan.app.data.local.entity.StockInScanEntity
import com.invenscan.app.data.local.entity.TagCacheEntity

@Database(
    entities = [
        ScanQueueEntity::class,
        PendingSubmitEntity::class,
        StockInScanEntity::class,
        SearchItemEntity::class,
        TagCacheEntity::class,
        AppLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
