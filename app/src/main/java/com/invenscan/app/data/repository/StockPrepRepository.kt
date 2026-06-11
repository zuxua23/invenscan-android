package com.invenscan.app.data.repository

import com.invenscan.app.base.Resource
import com.invenscan.app.data.local.AppDao
import com.invenscan.app.data.local.entity.PendingSubmitEntity
import com.invenscan.app.data.local.entity.SyncStatus
import com.invenscan.app.data.model.StockPrepDetailModel
import com.invenscan.app.data.model.StockPrepModel
import com.invenscan.app.data.model.StockPrepSubmitRequest
import com.invenscan.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockPrepRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDao: AppDao
) {

    suspend fun getPrepList(): Resource<List<StockPrepModel>> {
        return try {
            val response = apiService.getStockPrepList()
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.body()?.message ?: "Gagal memuat picking list")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal terhubung ke server")
        }
    }

    suspend fun getPrepDetail(id: Long): Resource<StockPrepDetailModel> {
        return try {
            val response = apiService.getStockPrepDetail(id)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data
                if (data != null) Resource.Success(data)
                else Resource.Error("Data tidak ditemukan")
            } else {
                Resource.Error(response.body()?.message ?: "Gagal memuat detail")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal terhubung ke server")
        }
    }

    suspend fun submitBulk(request: StockPrepSubmitRequest): Resource<Unit> {
        return try {
            val response = apiService.submitStockPrepBulk(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Gagal submit data")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal terhubung ke server")
        }
    }

    suspend fun queuePendingSubmit(
        stockPrepId: Long,
        detailId: Long,
        tagId: String,
        itemId: Long,
        scannedCode: String,
        scanType: String,
        pickedQty: Int,
        userId: String
    ) {
        appDao.insertPendingSubmit(
            PendingSubmitEntity(
                stockPrepId = stockPrepId,
                stockPrepDetailId = detailId,
                tagId = tagId,
                itemId = itemId,
                scannedCode = scannedCode,
                scanType = scanType,
                pickedQty = pickedQty,
                createdBy = userId,
                syncStatus = SyncStatus.PENDING
            )
        )
    }
}
