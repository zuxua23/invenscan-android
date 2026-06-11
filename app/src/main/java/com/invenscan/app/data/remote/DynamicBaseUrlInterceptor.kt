package com.invenscan.app.data.remote

import com.invenscan.app.util.PrefManager
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicBaseUrlInterceptor @Inject constructor(
    private val prefManager: PrefManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val rawServerUrl = prefManager.serverUrl?.trimEnd('/')
            ?: return chain.proceed(originalRequest)

        val serverHttpUrl = rawServerUrl.toHttpUrlOrNull()
            ?: return chain.proceed(originalRequest)

        val newUrl = originalRequest.url.newBuilder()
            .scheme(serverHttpUrl.scheme)
            .host(serverHttpUrl.host)
            .port(serverHttpUrl.port)
            .build()

        val newRequest = originalRequest.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}
