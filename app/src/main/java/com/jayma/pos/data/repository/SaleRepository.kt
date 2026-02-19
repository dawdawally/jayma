package com.jayma.pos.data.repository

import com.jayma.pos.data.local.dao.SaleDao
import com.jayma.pos.data.local.entities.*
import com.jayma.pos.data.remote.models.CreateSaleRequest
import com.jayma.pos.data.remote.models.CreateSaleResponse
import com.jayma.pos.data.remote.services.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepository @Inject constructor(
    private val saleDao: SaleDao,
    private val apiService: ApiService
) {
    
    fun getAllSales(): Flow<List<SaleEntity>> = saleDao.getAllSales()
    
    suspend fun getSaleByLocalId(localId: Long): SaleEntity? = saleDao.getSaleByLocalId(localId)
    
    suspend fun getUnsyncedSales(): List<SaleEntity> = saleDao.getUnsyncedSales()
    
    fun getUnsyncedSalesFlow(): Flow<List<SaleEntity>> = saleDao.getUnsyncedSalesFlow()
    
    suspend fun createSale(sale: SaleEntity, details: List<SaleDetailEntity>, payments: List<PaymentEntity>): Long {
        val saleId = saleDao.insertSale(sale)
        saleDao.insertSaleDetails(details.map { it.copy(saleLocalId = saleId) })
        saleDao.insertPayments(payments.map { it.copy(saleLocalId = saleId) })
        return saleId
    }
    
    suspend fun syncSale(sale: SaleEntity, details: List<SaleDetailEntity>, payments: List<PaymentEntity>): Result<Int> {
        return try {
            val request = CreateSaleRequest(
                clientId = sale.clientId,
                warehouseId = sale.warehouseId,
                taxRate = sale.taxRate,
                taxNet = sale.taxNet,
                discount = sale.discount,
                shipping = sale.shipping,
                grandTotal = sale.grandTotal,
                notes = sale.notes,
                poNumber = sale.poNumber,
                accountId = sale.accountId,
                paymentNote = sale.paymentNote,
                details = details.map { detail ->
                    com.jayma.pos.data.remote.models.SaleDetailRequest(
                        productId = detail.productId,
                        productVariantId = detail.productVariantId,
                        saleUnitId = detail.saleUnitId,
                        quantity = detail.quantity,
                        unitPrice = detail.unitPrice,
                        subtotal = detail.subtotal,
                        taxPercent = detail.taxPercent,
                        taxMethod = detail.taxMethod,
                        discount = detail.discount,
                        discountMethod = detail.discountMethod,
                        imeiNumber = detail.imeiNumber,
                        name = detail.productName
                    )
                },
                payments = payments.map { payment ->
                    com.jayma.pos.data.remote.models.PaymentRequest(
                        paymentMethodId = payment.paymentMethodId,
                        amount = payment.amount
                    )
                }
            )
            
            val response = apiService.createSale(request)
            if (response.isSuccessful && response.body() != null) {
                val saleResponse = response.body()!!
                if (saleResponse.success && saleResponse.id != null) {
                    // Update sale with server ID and mark as synced
                    val updatedSale = sale.copy(
                        serverId = saleResponse.id,
                        synced = true
                    )
                    saleDao.updateSale(updatedSale)
                    Result.success(saleResponse.id)
                } else {
                    Result.failure(Exception("Sale creation failed"))
                }
            } else {
                Result.failure(Exception("Failed to sync sale: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSaleDetails(saleLocalId: Long): List<SaleDetailEntity> = 
        saleDao.getSaleDetails(saleLocalId)
    
    suspend fun getPayments(saleLocalId: Long): List<PaymentEntity> = 
        saleDao.getPayments(saleLocalId)
}
