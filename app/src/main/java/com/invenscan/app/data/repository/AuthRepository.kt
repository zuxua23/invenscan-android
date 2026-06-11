package com.invenscan.app.data.repository

import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.AuthRequest
import com.invenscan.app.data.model.AuthResponse
import com.invenscan.app.data.remote.ApiService
import com.invenscan.app.util.PrefManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val prefManager: PrefManager
) {

    suspend fun testConnection(): Resource<Boolean> {
        return try {
            val response = apiService.ping()
            if (response.isSuccessful) Resource.Success(true)
            else Resource.Error("Server responded with ${response.code()}", response.code())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Connection failed")
        }
    }

    suspend fun login(userId: String, password: String): Resource<AuthResponse> {
        return try {
            val response = apiService.login(AuthRequest(userId, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                prefManager.token = data.token
                prefManager.refreshToken = data.refreshToken
                prefManager.userId = data.userId
                prefManager.fullName = data.fullName
                prefManager.userRole = data.role
                Resource.Success(data)
            } else {
                Resource.Error(response.body()?.message ?: "Login gagal", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal terhubung ke server")
        }
    }
}
