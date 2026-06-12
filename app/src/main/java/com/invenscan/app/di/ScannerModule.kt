package com.invenscan.app.di

import com.invenscan.app.scanner.BatteryProvider
import com.invenscan.app.scanner.MockBatteryProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import android.content.Context


@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {

    @Provides
    @Singleton
    fun provideBatteryProvider(@ApplicationContext context: Context): BatteryProvider =
        MockBatteryProvider(context)
}
