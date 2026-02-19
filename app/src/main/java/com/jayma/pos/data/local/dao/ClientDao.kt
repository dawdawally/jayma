package com.jayma.pos.data.local.dao

import androidx.room.*
import com.jayma.pos.data.local.entities.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<ClientEntity>>
    
    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: Int): ClientEntity?
    
    @Query("SELECT * FROM clients WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultClient(): ClientEntity?
    
    @Query("SELECT * FROM clients WHERE name LIKE :query")
    fun searchClients(query: String): Flow<List<ClientEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)
    
    @Update
    suspend fun updateClient(client: ClientEntity)
    
    @Delete
    suspend fun deleteClient(client: ClientEntity)
    
    @Query("DELETE FROM clients")
    suspend fun deleteAllClients()
}
