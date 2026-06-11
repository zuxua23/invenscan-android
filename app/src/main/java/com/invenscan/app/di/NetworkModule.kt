package com.invenscan.app.di

import com.invenscan.app.data.remote.ApiClient
import com.invenscan.app.data.remote.ApiService
import com.invenscan.app.data.remote.AuthInterceptor
import com.invenscan.app.data.remote.DynamicBaseUrlInterceptor
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
    fun provideRetrofit(authInterceptor: AuthInterceptor, dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor): Retrofit {
        return ApiClient.buildRetrofit(authInterceptor, dynamicBaseUrlInterceptor)
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
