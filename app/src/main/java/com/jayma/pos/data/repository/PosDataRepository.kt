package com.jayma.pos.data.repository

import com.jayma.pos.data.local.dao.ClientDao
import com.jayma.pos.data.local.entities.*
import com.jayma.pos.data.remote.models.PosDataResponse
import com.jayma.pos.data.remote.services.ApiService
import com.jayma.pos.util.SharedPreferencesHelper
import kotlinx.coroutines.flow.Flow
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PosDataRepository @Inject constructor(
    private val apiService: ApiService,
    private val clientDao: ClientDao,
    private val warehouseDao: com.jayma.pos.data.local.dao.WarehouseDao,
    private val sharedPreferences: SharedPreferencesHelper
) {
    
    suspend fun fetchPosData(): Result<PosDataResponse> {
        return try {
            val response = apiService.getPosData()
            if (response.isSuccessful && response.body() != null) {
                var posData = response.body()!!
                
                // Handle invalid defaultWarehouse - fallback to first warehouse if available
                var defaultWarehouse = posData.defaultWarehouse
                if (defaultWarehouse <= 0) {
                    if (posData.warehouses.isNotEmpty()) {
                        defaultWarehouse = posData.warehouses.first().id
                        // Create a new PosDataResponse with corrected defaultWarehouse
                        posData = posData.copy(defaultWarehouse = defaultWarehouse)
                    } else {
                        return Result.failure(Exception("No warehouses available. Please configure at least one warehouse in the system."))
                    }
                }
                
                // Handle invalid defaultClient - fallback to first client if available
                var defaultClient = posData.defaultClient
                if (defaultClient <= 0) {
                    if (posData.clients.isNotEmpty()) {
                        defaultClient = posData.clients.first().id
                        // Create a new PosDataResponse with corrected defaultClient
                        posData = posData.copy(defaultClient = defaultClient)
                    } else {
                        return Result.failure(Exception("No clients available. Please configure at least one client in the system."))
                    }
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
                
                // Cache warehouses locally
                val warehouses = posData.warehouses.map { warehouse ->
                    WarehouseEntity(
                        id = warehouse.id,
                        name = warehouse.name,
                        isDefault = warehouse.id == posData.defaultWarehouse
                    )
                }
                warehouseDao.insertWarehouses(warehouses)
                
                // Save default warehouse and client
                sharedPreferences.saveDefaultWarehouse(posData.defaultWarehouse)
                sharedPreferences.saveDefaultClient(posData.defaultClient)
                
                Result.success(posData)
            } else {
                Result.failure(Exception("Failed to fetch POS data: ${response.message()}"))
            }
        } catch (e: NumberFormatException) {
            Result.failure(Exception("Failed to parse POS data: Invalid number format. Please check API response format."))
        } catch (e: UnknownHostException) {
            // Host resolution error - wrap it to preserve the exception type
            Result.failure(UnknownHostException("Unable to resolve host. Please check your domain settings."))
        } catch (e: Exception) {
            // Check if the cause is UnknownHostException
            if (e.cause is UnknownHostException || 
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("UnknownHostException", ignoreCase = true) == true) {
                Result.failure(UnknownHostException("Unable to resolve host. Please check your domain settings."))
            } else {
                Result.failure(e)
            }
        }
    }
    
    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAllClients()
    
    suspend fun getDefaultClient(): ClientEntity? = clientDao.getDefaultClient()
    
    fun getAllWarehouses(): Flow<List<WarehouseEntity>> = warehouseDao.getAllWarehouses()
    
    suspend fun getDefaultWarehouse(): WarehouseEntity? = warehouseDao.getDefaultWarehouse()
}
