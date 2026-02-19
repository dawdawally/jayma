package com.jayma.pos.data.repository

import com.jayma.pos.data.local.dao.SaleDao
import com.jayma.pos.data.local.entities.*
import com.jayma.pos.data.remote.models.CreateSaleRequest
import com.jayma.pos.data.remote.models.CreateSaleResponse
import com.jayma.pos.data.remote.services.ApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import retrofit2.Response

class SaleRepositoryTest {
    
    private lateinit var saleDao: SaleDao
    private lateinit var apiService: ApiService
    private lateinit var saleRepository: SaleRepository
    
    @Before
    fun setup() {
        saleDao = mock()
        apiService = mock()
        saleRepository = SaleRepository(saleDao, apiService)
    }
    
    @Test
    fun `createSale saves sale locally`() = runTest {
        // Given
        val sale = createTestSale()
        val details = listOf(createTestSaleDetail())
        val payments = listOf(createTestPayment())
        whenever(saleDao.insertSale(sale)).thenReturn(1L)
        whenever(saleDao.insertSaleDetails(any())).thenAnswer { }
        whenever(saleDao.insertPayments(any())).thenAnswer { }
        
        // When
        val result = saleRepository.createSale(sale, details, payments)
        
        // Then
        assertEquals(1L, result)
        verify(saleDao).insertSale(sale)
        verify(saleDao).insertSaleDetails(any())
        verify(saleDao).insertPayments(any())
    }
    
    @Test
    fun `getUnsyncedSales returns unsynced sales from dao`() = runTest {
        // Given
        val unsyncedSales = listOf(
            createTestSale(synced = false),
            createTestSale(synced = false, localId = 2)
        )
        whenever(saleDao.getUnsyncedSales()).thenReturn(unsyncedSales)
        
        // When
        val result = saleRepository.getUnsyncedSales()
        
        // Then
        assertEquals(2, result.size)
        assertFalse(result[0].synced)
        verify(saleDao).getUnsyncedSales()
    }
    
    @Test
    fun `syncSale successfully uploads sale to API`() = runTest {
        // Given
        val sale = createTestSale(synced = false, localId = 1)
        val details = listOf(createTestSaleDetail())
        val payments = listOf(createTestPayment())
        val serverId = 123
        
        val apiResponse = Response.success(CreateSaleResponse(success = true, id = serverId))
        whenever(apiService.createSale(any())).thenReturn(apiResponse)
        whenever(saleDao.updateSale(any())).thenAnswer { }
        
        // When
        val result = saleRepository.syncSale(sale, details, payments)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(serverId, result.getOrNull())
        verify(apiService).createSale(any())
        verify(saleDao).updateSale(argThat { it.synced && it.serverId == serverId })
    }
    
    @Test
    fun `syncSale handles API failure`() = runTest {
        // Given
        val sale = createTestSale(synced = false)
        val details = listOf(createTestSaleDetail())
        val payments = listOf(createTestPayment())
        
        whenever(apiService.createSale(any()))
            .thenReturn(Response.error(500, okhttp3.ResponseBody.create(null, "")))
        
        // When
        val result = saleRepository.syncSale(sale, details, payments)
        
        // Then
        assertTrue(result.isFailure)
        verify(apiService).createSale(any())
        verify(saleDao, never()).updateSale(any())
    }
    
    @Test
    fun `syncSale handles unsuccessful response`() = runTest {
        // Given
        val sale = createTestSale(synced = false)
        val details = listOf(createTestSaleDetail())
        val payments = listOf(createTestPayment())
        
        val apiResponse = Response.success(CreateSaleResponse(success = false, id = null))
        whenever(apiService.createSale(any())).thenReturn(apiResponse)
        
        // When
        val result = saleRepository.syncSale(sale, details, payments)
        
        // Then
        assertTrue(result.isFailure)
        verify(apiService).createSale(any())
        verify(saleDao, never()).updateSale(any())
    }
    
    private fun createTestSale(
        localId: Long = 1,
        synced: Boolean = true,
        serverId: Int? = null
    ): SaleEntity {
        return SaleEntity(
            localId = localId,
            serverId = serverId,
            clientId = 1,
            warehouseId = 1,
            taxRate = 10.0,
            taxNet = 10.0,
            discount = 0.0,
            shipping = 0.0,
            grandTotal = 110.0,
            notes = null,
            synced = synced,
            createdAt = System.currentTimeMillis()
        )
    }
    
    private fun createTestSaleDetail(): SaleDetailEntity {
        return SaleDetailEntity(
            saleLocalId = 1,
            productId = 1,
            productVariantId = 1,
            quantity = 1.0,
            unitPrice = 100.0,
            subtotal = 100.0,
            productName = "Test Product"
        )
    }
    
    private fun createTestPayment(): PaymentEntity {
        return PaymentEntity(
            saleLocalId = 1,
            paymentMethodId = 1,
            amount = 110.0,
            change = 0.0
        )
    }
}
