package com.jayma.pos.data.repository

import com.jayma.pos.data.local.dao.ClientDao
import com.jayma.pos.data.local.entities.ClientEntity
import com.jayma.pos.data.remote.models.PosDataResponse
import com.jayma.pos.data.remote.services.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PosDataRepository @Inject constructor(
    private val apiService: ApiService,
    private val clientDao: ClientDao
) {
    
    suspend fun fetchPosData(): Result<PosDataResponse> {
        return try {
            val response = apiService.getPosData()
            if (response.isSuccessful && response.body() != null) {
                val posData = response.body()!!
                
                // Cache clients locally
                val clients = posData.clients.map { client ->
                    ClientEntity(
                        id = client.id,
                        name = client.name,
                        isDefault = client.id == posData.defaultClient
                    )
                }
                clientDao.insertClients(clients)
                
                Result.success(posData)
            } else {
                Result.failure(Exception("Failed to fetch POS data: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAllClients()
    
    suspend fun getDefaultClient(): ClientEntity? = clientDao.getDefaultClient()
}
