package com.invenscan.app.data.repository

import com.invenscan.app.base.Resource
import com.invenscan.app.data.local.AppDao
import com.invenscan.app.data.local.entity.ScanQueueEntity
import com.invenscan.app.data.local.entity.SyncStatus
import com.invenscan.app.data.model.StockTakingModel
import com.invenscan.app.data.model.StockTakingSubmitRequest
import com.invenscan.app.data.model.StockTakingTagModel
import com.invenscan.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockTakingRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDao: AppDao
) {

    suspend fun getActiveSession(locationId: Long): Resource<StockTakingModel> {
        return try {
            val response = apiService.getActiveStockTakingSession(locationId)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data
                if (data != null) Resource.Success(data)
                else Resource.Error("No active session at this location")
            } else {
                Resource.Error(response.body()?.message ?: "No active session at this location")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to connect to server")
        }
    }

    suspend fun createSession(locationId: Long, remark: String): Resource<StockTakingModel> {
        return try {
            val body = mapOf<String, Any>("locationId" to locationId, "remark" to remark)
            val response = apiService.createStockTakingSession(body)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data
                if (data != null) Resource.Success(data)
                else Resource.Error("Failed to create session")
            } else {
                Resource.Error(response.body()?.message ?: "Failed to create session")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to connect to server")
        }
    }

    suspend fun getSessionTags(sttId: Long): Resource<List<StockTakingTagModel>> {
        return try {
            val response = apiService.getStockTakingTags(sttId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.body()?.message ?: "Failed to load session data")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to connect to server")
        }
    }

    suspend fun submitResults(request: StockTakingSubmitRequest): Resource<Unit> {
        return try {
            val response = apiService.submitStockTakingResults(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to submit results")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to connect to server")
        }
    }

    suspend fun queueScanResult(
        sttId: Long,
        tagId: String,
        itemId: Long,
        action: String,
        userId: String
    ) {
        appDao.insertScanQueue(
            ScanQueueEntity(
                sttId = sttId,
                tagId = tagId,
                itemId = itemId,
                action = action,
                createdBy = userId,
                syncStatus = SyncStatus.PENDING
            )
        )
    }
}
