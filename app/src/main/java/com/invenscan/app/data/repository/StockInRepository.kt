package com.invenscan.app.data.repository

import com.invenscan.app.base.Resource
import com.invenscan.app.data.local.AppDao
import com.invenscan.app.data.local.entity.StockInScanEntity
import com.invenscan.app.data.local.entity.SyncStatus
import com.invenscan.app.data.local.entity.TagCacheEntity
import com.invenscan.app.data.model.StockInSubmitRequest
import com.invenscan.app.data.model.StockInTagInfo
import com.invenscan.app.data.remote.ApiService
import com.invenscan.app.scanner.ScannerContract
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockInRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDao: AppDao
) {

    suspend fun resolveTag(
        code: String,
        scanType: ScannerContract.ScanType
    ): Resource<StockInTagInfo> {
        val cached = appDao.getTagCacheByEpc(code)
        if (cached != null) {
            return Resource.Success(cached.toTagInfo())
        }
        return try {
            val response = apiService.lookupStockInTag(code, scanType.name)
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

    suspend fun saveToQueue(
        docNumber: String,
        locationId: Long,
        scannedCode: String,
        scanType: ScannerContract.ScanType,
        itemId: Long?,
        itemName: String?,
        notes: String?,
        userId: String
    ): Long {
        return appDao.insertStockInScan(
            StockInScanEntity(
                docNumber = docNumber,
                locationId = locationId,
                scannedCode = scannedCode,
                scanType = scanType.name,
                itemId = itemId,
                itemName = itemName,
                notes = notes,
                createdBy = userId,
                syncStatus = SyncStatus.PENDING
            )
        )
    }

    suspend fun submitDirectly(request: StockInSubmitRequest): Resource<Unit> {
        return try {
            val response = apiService.submitStockIn(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Gagal submit stock in")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal terhubung ke server")
        }
    }

    private fun TagCacheEntity.toTagInfo() = StockInTagInfo(
        tagId = epcTag,
        epcTag = epcTag,
        itemId = itemId,
        itemCode = itemCode,
        itemName = itemName,
        locationId = locationId,
        locationName = locationName,
        status = status
    )

    private fun StockInTagInfo.toTagCacheEntity() = TagCacheEntity(
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
