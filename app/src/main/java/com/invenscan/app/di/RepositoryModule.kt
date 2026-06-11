package com.invenscan.app.di

import com.invenscan.app.data.local.AppDao
import com.invenscan.app.data.remote.ApiService
import com.invenscan.app.util.PrefManager
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds repository interfaces to their implementations.
 * Repository classes are added here as features are implemented in Day 4.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // Repositories are provided via @Inject constructor in their own classes.
    // Bindings for interfaces → implementations are added here as each feature
    // screen is built.
}
