package com.jayma.pos.data.local.dao

import androidx.room.*
import com.jayma.pos.data.local.entities.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    
    @Query("SELECT * FROM payment_methods ORDER BY name ASC")
    fun getAllPaymentMethods(): Flow<List<PaymentMethodEntity>>
    
    @Query("SELECT * FROM payment_methods WHERE id = :id")
    suspend fun getPaymentMethodById(id: Int): PaymentMethodEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(paymentMethods: List<PaymentMethodEntity>)
    
    @Update
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethodEntity)
    
    @Delete
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethodEntity)
    
    @Query("DELETE FROM payment_methods")
    suspend fun deleteAllPaymentMethods()
}
