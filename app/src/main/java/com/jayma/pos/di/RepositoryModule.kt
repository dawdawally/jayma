package com.jayma.pos.di

import com.jayma.pos.data.repository.PosDataRepository
import com.jayma.pos.data.repository.ProductRepository
import com.jayma.pos.data.repository.SaleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    // Repositories are already annotated with @Singleton
    // This module is for any additional repository setup if needed
    // The actual repositories are provided via constructor injection
}
