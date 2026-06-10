package com.invenscan.app.data.repository

import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.LocationModel
import com.invenscan.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val apiService: ApiService
) {

    private var cachedLocations: List<LocationModel> = emptyList()

    suspend fun getLocations(forceRefresh: Boolean = false): Resource<List<LocationModel>> {
        if (!forceRefresh && cachedLocations.isNotEmpty()) {
            return Resource.Success(cachedLocations)
        }
        return try {
            val response = apiService.getLocations()
            if (response.isSuccessful && response.body()?.success == true) {
                cachedLocations = response.body()!!.data ?: emptyList()
                Resource.Success(cachedLocations)
            } else {
                Resource.Error(response.body()?.message ?: "Gagal memuat lokasi")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal terhubung ke server")
        }
    }
}
