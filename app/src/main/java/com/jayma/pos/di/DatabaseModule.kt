package com.jayma.pos.di

import android.content.Context
import androidx.room.Room
import com.jayma.pos.data.local.AppDatabase
import com.jayma.pos.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "jayma_pos_database"
        )
            .fallbackToDestructiveMigration() // For development - remove in production
            .setQueryCallback({ sqlQuery, bindArgs ->
                // Log slow queries in debug builds
                if (com.jayma.pos.BuildConfig.DEBUG) {
                    com.jayma.pos.util.Logger.d("RoomQuery", "SQL: $sqlQuery")
                }
            }, java.util.concurrent.Executors.newSingleThreadExecutor())
            .build()
    }
    
    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()
    
    @Provides
    fun provideClientDao(database: AppDatabase): ClientDao = database.clientDao()
    
    @Provides
    fun provideSaleDao(database: AppDatabase): SaleDao = database.saleDao()
    
    @Provides
    fun provideDraftDao(database: AppDatabase): DraftDao = database.draftDao()
    
    @Provides
    fun provideSyncStatusDao(database: AppDatabase): SyncStatusDao = database.syncStatusDao()
}
