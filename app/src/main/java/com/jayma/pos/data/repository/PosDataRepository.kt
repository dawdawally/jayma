package com.jayma.pos.data.repository

import com.jayma.pos.data.local.dao.ClientDao
import com.jayma.pos.data.local.entities.*
import com.jayma.pos.data.remote.models.PosDataResponse
import com.jayma.pos.data.remote.services.ApiService
import com.jayma.pos.util.SharedPreferencesHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PosDataRepository @Inject constructor(
    private val apiService: ApiService,
    private val clientDao: ClientDao,
    private val sharedPreferences: SharedPreferencesHelper
) {
    
    suspend fun fetchPosData(): Result<PosDataResponse> {
        return try {
            val response = apiService.getPosData()
            if (response.isSuccessful && response.body() != null) {
                val posData = response.body()!!
                
                // Validate defaultWarehouse and defaultClient
                if (posData.defaultWarehouse <= 0) {
                    return Result.failure(Exception("Invalid default warehouse ID: ${posData.defaultWarehouse}. Please configure a default warehouse in the system."))
                }
                if (posData.defaultClient <= 0) {
                    return Result.failure(Exception("Invalid default client ID: ${posData.defaultClient}. Please configure a default client in the system."))
                }
                
                // Cache clients locally
                val clients = posData.clients.map { client ->
                    ClientEntity(
                        id = client.id,
                        name = client.name,
                        isDefault = client.id == posData.defaultClient
                    )
                }
                clientDao.insertClients(clients)
                
                // Save default warehouse and client
                sharedPreferences.saveDefaultWarehouse(posData.defaultWarehouse)
                sharedPreferences.saveDefaultClient(posData.defaultClient)
                
                Result.success(posData)
            } else {
                Result.failure(Exception("Failed to fetch POS data: ${response.message()}"))
            }
        } catch (e: NumberFormatException) {
            Result.failure(Exception("Failed to parse POS data: Invalid number format. Please check API response format."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAllClients()
    
    suspend fun getDefaultClient(): ClientEntity? = clientDao.getDefaultClient()
}
