package com.invenscan.app.data.repository

import com.invenscan.app.base.Resource
import com.invenscan.app.data.local.AppDao
import com.invenscan.app.data.local.entity.StockOutScanEntity
import com.invenscan.app.data.local.entity.SyncStatus
import com.invenscan.app.data.local.entity.TagCacheEntity
import com.invenscan.app.data.model.StockOutSubmitRequest
import com.invenscan.app.data.model.StockOutTagInfo
import com.invenscan.app.data.remote.ApiService
import com.invenscan.app.scanner.ScannerContract
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockOutRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDao: AppDao
) {

    suspend fun resolveTag(
        code: String,
        scanType: ScannerContract.ScanType
    ): Resource<StockOutTagInfo> {
        val cached = appDao.getTagCacheByEpc(code)
        if (cached != null) {
            return Resource.Success(cached.toStockOutTagInfo())
        }
        return try {
            val response = apiService.lookupStockOutTag(code, scanType.name)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                appDao.insertTagCache(data.toTagCacheEntity())
                Resource.Success(data)
            } else {
                Resource.Error(response.body()?.message ?: "Tag tidak ditemukan")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal lookup tag")
        }
    }

    suspend fun submitDirectly(request: StockOutSubmitRequest): Resource<Unit> {
        return try {
            val response = apiService.submitStockOut(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Gagal submit stock out")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal terhubung ke server")
        }
    }

    suspend fun saveToQueue(
        docNumber: String,
        locationId: Long,
        locationName: String,
        tagId: String?,
        itemId: Long?,
        itemName: String?,
        itemCode: String?,
        scannedCode: String,
        scanType: ScannerContract.ScanType
    ): Long {
        return appDao.insertStockOutScan(
            StockOutScanEntity(
                docNumber = docNumber,
                locationId = locationId,
                locationName = locationName,
                tagId = tagId,
                itemId = itemId,
                itemName = itemName,
                itemCode = itemCode,
                scannedCode = scannedCode,
                scanType = scanType.name,
                syncStatus = SyncStatus.PENDING
            )
        )
    }

    suspend fun getPendingSync(): List<StockOutScanEntity> =
        appDao.getPendingStockOutScans()

    private fun TagCacheEntity.toStockOutTagInfo() = StockOutTagInfo(
        tagId = epcTag,
        epcTag = epcTag,
        itemId = itemId ?: 0L,
        itemCode = itemCode ?: "",
        itemName = itemName ?: "",
        locationId = locationId ?: 0L,
        locationName = locationName,
        status = status
    )

    private fun StockOutTagInfo.toTagCacheEntity() = TagCacheEntity(
        tagId = 0,
        epcTag = epcTag ?: tagId,
        itemId = itemId,
        itemCode = itemCode,
        itemName = itemName,
        locationId = locationId,
        locationName = locationName ?: "",
        status = status ?: "UNKNOWN"
    )
}
