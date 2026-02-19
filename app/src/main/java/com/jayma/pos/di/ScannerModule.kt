package com.jayma.pos.di

import com.jayma.pos.util.scanner.BarcodeScannerService
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
    fun provideBarcodeScannerService(): BarcodeScannerService {
        return BarcodeScannerService()
    }
}
