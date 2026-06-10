package com.invenscan.app.data.repository

import com.invenscan.app.base.Resource
import com.invenscan.app.data.local.AppDao
import com.invenscan.app.data.local.entity.SearchItemEntity
import com.invenscan.app.data.model.SearchItemModel
import com.invenscan.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchItemRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDao: AppDao
) {

    private val cacheExpiryMs = 30L * 60 * 1000

    suspend fun findByCode(code: String): Resource<SearchItemModel> {
        val cached = appDao.getSearchItemByCode(code)
        if (cached != null && !isCacheExpired(cached.cachedAt)) {
            return Resource.Success(cached.toModel())
        }
        return try {
            val response = apiService.searchItemByCode(code)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                appDao.insertSearchItems(listOf(data.toEntity()))
                Resource.Success(data)
            } else {
                Resource.Error(response.body()?.message ?: "Item tidak ditemukan")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mencari item")
        }
    }

    suspend fun refreshCache(): Resource<Unit> {
        return try {
            val response = apiService.getAllItemsForCache()
            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()!!.data ?: emptyList()
                appDao.clearSearchItemCache()
                appDao.insertSearchItems(items.map { it.toEntity() })
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Gagal refresh cache")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal terhubung ke server")
        }
    }

    private fun isCacheExpired(cachedAt: Long): Boolean {
        return System.currentTimeMillis() - cachedAt > cacheExpiryMs
    }

    private fun SearchItemEntity.toModel() = SearchItemModel(
        id = itemId,
        itemCode = itemCode,
        itemName = itemName,
        description = description,
        unit = unit,
        minStock = minStock,
        locationName = locationName,
        status = status,
        epcTag = null
    )

    private fun SearchItemModel.toEntity() = SearchItemEntity(
        itemId = id,
        itemCode = itemCode,
        itemName = itemName,
        description = description,
        unit = unit,
        minStock = minStock,
        locationName = locationName,
        status = status
    )
}
