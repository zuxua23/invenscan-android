package com.invenscan.app.di

import com.invenscan.app.scanner.BatteryProvider
import com.invenscan.app.scanner.MockBatteryProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {

    @Provides
    @Singleton
    fun provideBatteryProvider(): BatteryProvider = MockBatteryProvider()
}
