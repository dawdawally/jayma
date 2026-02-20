package com.jayma.pos.di

import com.jayma.pos.data.remote.ApiConfig
import com.jayma.pos.data.remote.services.ApiService
import com.jayma.pos.util.SharedPreferencesHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(sharedPreferences: SharedPreferencesHelper): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        
        // Add dynamic base URL interceptor
        builder.addInterceptor { chain ->
            val storedUrl = sharedPreferences.getApiBaseUrl()
            val baseUrl = storedUrl ?: ApiConfig.BASE_URL
            
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url
            
            // Parse the stored base URL using the builder pattern
            val baseHttpUrl = try {
                val parsed = originalUrl.resolve(baseUrl)
                if (parsed != null) {
                    parsed
                } else {
                    originalUrl.resolve(ApiConfig.BASE_URL) ?: originalUrl
                }
            } catch (e: Exception) {
                originalUrl.resolve(ApiConfig.BASE_URL) ?: originalUrl
            }
            
            // Build new URL with the dynamic base, preserving the path and query
            val newUrl = originalUrl.newBuilder()
                .scheme(baseHttpUrl.scheme)
                .host(baseHttpUrl.host)
                .port(baseHttpUrl.port)
                .build()
            
            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()
            
            chain.proceed(newRequest)
        }
        
        // Only add logging in debug builds
        if (com.jayma.pos.BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }
        
        return builder.build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, sharedPreferences: SharedPreferencesHelper): Retrofit {
        // Use a placeholder base URL - the interceptor will handle the actual URL
        val storedUrl = sharedPreferences.getApiBaseUrl()
        val baseUrl = storedUrl ?: ApiConfig.BASE_URL
        
        return Retrofit.Builder()
            .baseUrl(baseUrl + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
