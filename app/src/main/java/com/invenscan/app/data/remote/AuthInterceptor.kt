package com.invenscan.app.data.remote

import com.invenscan.app.util.PrefManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val prefManager: PrefManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = prefManager.token
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
