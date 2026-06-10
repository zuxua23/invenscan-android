package com.invenscan.app.di

import com.invenscan.app.data.remote.ApiClient
import com.invenscan.app.data.remote.ApiService
import com.invenscan.app.data.remote.AuthInterceptor
import com.invenscan.app.util.PrefManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(prefManager: PrefManager, authInterceptor: AuthInterceptor): Retrofit {
        return ApiClient.buildRetrofit(prefManager, authInterceptor)
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
