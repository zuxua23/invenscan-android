package com.invenscan.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.invenscan.app.data.local.entity.AppLogEntity
import com.invenscan.app.data.local.entity.PendingSubmitEntity
import com.invenscan.app.data.local.entity.ScanQueueEntity
import com.invenscan.app.data.local.entity.SearchItemEntity
import com.invenscan.app.data.local.entity.StockInScanEntity
import com.invenscan.app.data.local.entity.StockOutScanEntity
import com.invenscan.app.data.local.entity.TagCacheEntity

@Database(
    entities = [
        ScanQueueEntity::class,
        PendingSubmitEntity::class,
        StockInScanEntity::class,
        StockOutScanEntity::class,
        SearchItemEntity::class,
        TagCacheEntity::class,
        AppLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `stock_out_scan` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `docNumber` TEXT NOT NULL,
                        `locationId` INTEGER NOT NULL,
                        `locationName` TEXT NOT NULL,
                        `tagId` TEXT,
                        `itemId` INTEGER,
                        `itemName` TEXT,
                        `itemCode` TEXT,
                        `scannedCode` TEXT NOT NULL,
                        `scanType` TEXT NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
