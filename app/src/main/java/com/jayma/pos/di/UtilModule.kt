package com.jayma.pos.di

import com.jayma.pos.util.SharedPreferencesHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    
    @Provides
    @Singleton
    fun provideSharedPreferencesHelper(
        @ApplicationContext context: android.content.Context
    ): SharedPreferencesHelper {
        return SharedPreferencesHelper(context)
    }
}
