package com.jayma.pos.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.crypto.AEADBadTagException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "jayma_pos_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // If encryption fails (e.g., corrupted data, keystore changed), 
        // delete the corrupted file and recreate
        if (e is AEADBadTagException || e.cause is AEADBadTagException || 
            e.message?.contains("Signature/MAC verification failed") == true ||
            e.message?.contains("AEADBadTagException") == true) {
            try {
                // Delete corrupted encrypted preferences file
                val prefsFile = File(context.filesDir.parent, "shared_prefs/jayma_pos_prefs.xml")
                if (prefsFile.exists()) {
                    prefsFile.delete()
                }
                // Also try deleting the master key file if it exists
                val masterKeyFile = File(context.filesDir.parent, "shared_prefs/__androidx_security_crypto_encrypted_prefs_key_keyset__.xml")
                if (masterKeyFile.exists()) {
                    masterKeyFile.delete()
                }
                val masterKeyPrefsFile = File(context.filesDir.parent, "shared_prefs/__androidx_security_crypto_encrypted_prefs_key_pref_keyset__.xml")
                if (masterKeyPrefsFile.exists()) {
                    masterKeyPrefsFile.delete()
                }
            } catch (deleteException: Exception) {
                // Ignore deletion errors
            }
            
            // Retry creating encrypted preferences
            try {
                EncryptedSharedPreferences.create(
                    context,
                    "jayma_pos_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (retryException: Exception) {
                // If retry also fails, fall back to regular SharedPreferences
                // This should rarely happen, but provides a safety net
                context.getSharedPreferences("jayma_pos_prefs", Context.MODE_PRIVATE)
            }
        } else {
            // For other exceptions, fall back to regular SharedPreferences
            context.getSharedPreferences("jayma_pos_prefs", Context.MODE_PRIVATE)
        }
    }
    
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
    
    fun saveApiBaseUrl(baseUrl: String) {
        sharedPreferences.edit().putString(KEY_API_BASE_URL, baseUrl.trim()).apply()
    }
    
    fun getApiBaseUrl(): String? {
        val url = sharedPreferences.getString(KEY_API_BASE_URL, null)
        return if (url.isNullOrBlank()) null else url.trim()
    }
    
    fun clearApiBaseUrl() {
        sharedPreferences.edit().remove(KEY_API_BASE_URL).apply()
    }
    
    fun saveDefaultPaymentMethod(paymentMethodId: Int) {
        sharedPreferences.edit().putInt(KEY_DEFAULT_PAYMENT_METHOD, paymentMethodId).apply()
    }
    
    fun getDefaultPaymentMethod(): Int? {
        val id = sharedPreferences.getInt(KEY_DEFAULT_PAYMENT_METHOD, -1)
        return if (id == -1) null else id
    }
    
    companion object {
        private const val KEY_DEFAULT_WAREHOUSE = "default_warehouse"
        private const val KEY_DEFAULT_CLIENT = "default_client"
        private const val KEY_DEFAULT_PAYMENT_METHOD = "default_payment_method"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_API_BASE_URL = "api_base_url"
    }
}
