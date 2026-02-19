package com.jayma.pos.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "jayma_pos_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveDefaultWarehouse(warehouseId: Int) {
        sharedPreferences.edit().putInt(KEY_DEFAULT_WAREHOUSE, warehouseId).apply()
    }
    
    fun getDefaultWarehouse(): Int? {
        val id = sharedPreferences.getInt(KEY_DEFAULT_WAREHOUSE, -1)
        return if (id == -1) null else id
    }
    
    fun saveDefaultClient(clientId: Int) {
        sharedPreferences.edit().putInt(KEY_DEFAULT_CLIENT, clientId).apply()
    }
    
    fun getDefaultClient(): Int? {
        val id = sharedPreferences.getInt(KEY_DEFAULT_CLIENT, -1)
        return if (id == -1) null else id
    }
    
    fun saveLastSyncTimestamp(timestamp: Long) {
        sharedPreferences.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }
    
    fun getLastSyncTimestamp(): Long {
        return sharedPreferences.getLong(KEY_LAST_SYNC, 0)
    }
    
    companion object {
        private const val KEY_DEFAULT_WAREHOUSE = "default_warehouse"
        private const val KEY_DEFAULT_CLIENT = "default_client"
        private const val KEY_LAST_SYNC = "last_sync"
    }
}
