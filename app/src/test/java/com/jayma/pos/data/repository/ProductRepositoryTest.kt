package com.jayma.pos.data.repository

import com.jayma.pos.data.local.dao.ProductDao
import com.jayma.pos.data.local.entities.ProductEntity
import com.jayma.pos.data.remote.models.ProductResponse
import com.jayma.pos.data.remote.services.ApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import okhttp3.ResponseBody
import org.mockito.kotlin.*
import retrofit2.Response

class ProductRepositoryTest {
    
    private lateinit var productDao: ProductDao
    private lateinit var apiService: ApiService
    private lateinit var productRepository: ProductRepository
    
    @Before
    fun setup() {
        productDao = mock()
        apiService = mock()
        productRepository = ProductRepository(productDao, apiService)
    }
    
    @Test
    fun `getAllProducts returns flow from dao`() = runTest {
        // Given
        val products = listOf(
            createTestProduct(1, "Product 1"),
            createTestProduct(2, "Product 2")
        )
        whenever(productDao.getAllProducts()).thenReturn(flowOf(products))
        
        // When
        val result = productRepository.getAllProducts().first()
        
        // Then
        assertEquals(2, result.size)
        assertEquals("Product 1", result[0].name)
        verify(productDao).getAllProducts()
    }
    
    @Test
    fun `getProductById returns product from dao`() = runTest {
        // Given
        val product = createTestProduct(1, "Product 1")
        whenever(productDao.getProductById(1)).thenReturn(product)
        
        // When
        val result = productRepository.getProductById(1)
        
        // Then
        assertNotNull(result)
        assertEquals("Product 1", result?.name)
        verify(productDao).getProductById(1)
    }
    
    @Test
    fun `getProductByCode returns product from dao`() = runTest {
        // Given
        val product = createTestProduct(1, "Product 1", code = "P001")
        whenever(productDao.getProductByCode("P001")).thenReturn(product)
        
        // When
        val result = productRepository.getProductByCode("P001")
        
        // Then
        assertNotNull(result)
        assertEquals("P001", result?.code)
        verify(productDao).getProductByCode("P001")
    }
    
    @Test
    fun `syncProducts successfully syncs products from API`() = runTest {
        // Given
        val warehouseId = 1
        val page = 1
        val apiResponse = createMockApiResponse(
            listOf(
                createProductResponse(1, "Product 1", "P001"),
                createProductResponse(2, "Product 2", "P002")
            )
        )
        whenever(apiService.getProducts(warehouseId, page)).thenReturn(apiResponse)
        whenever(productDao.insertProducts(any())).thenAnswer { }
        
        // When
        val result = productRepository.syncProducts(warehouseId, page)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
        verify(apiService).getProducts(warehouseId, page)
        verify(productDao).insertProducts(any())
    }
    
    @Test
    fun `syncProducts handles API failure`() = runTest {
        // Given
        val warehouseId = 1
        val page = 1
        whenever(apiService.getProducts(warehouseId, page))
            .thenReturn(Response.error(500, okhttp3.ResponseBody.create(null, "")))
        
        // When
        val result = productRepository.syncProducts(warehouseId, page)
        
        // Then
        assertTrue(result.isFailure)
        verify(apiService).getProducts(warehouseId, page)
        verify(productDao, never()).insertProducts(any())
    }
    
    @Test
    fun `syncAllProducts syncs all pages`() = runTest {
        // Given
        val warehouseId = 1
        val page1Products = (1..28).map { createProductResponse(it, "Product $it", "P$it") }
        val page2Products = (29..30).map { createProductResponse(it, "Product $it", "P$it") }
        
        whenever(apiService.getProducts(warehouseId, 1))
            .thenReturn(createMockApiResponse(page1Products))
        whenever(apiService.getProducts(warehouseId, 2))
            .thenReturn(createMockApiResponse(page2Products))
        whenever(productDao.insertProducts(any())).thenAnswer { }
        
        // When
        val result = productRepository.syncAllProducts(warehouseId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(30, result.getOrNull())
        verify(apiService, times(2)).getProducts(eq(warehouseId), any())
    }
    
    private fun createTestProduct(
        id: Int,
        name: String,
        code: String = "CODE$id"
    ): ProductEntity {
        return ProductEntity(
            id = id,
            productVariantId = id,
            code = code,
            name = name,
            barcode = "BAR$id",
            image = null,
            qte = 100.0,
            qteSale = 100.0,
            unitSale = "pcs",
            productType = "standard",
            netPrice = 10.0,
            synced = true
        )
    }
    
    private fun createProductResponse(
        id: Int,
        name: String,
        code: String
    ): ProductResponse {
        return ProductResponse(
            id = id,
            productVariantId = id,
            code = code,
            name = name,
            barcode = "BAR$id",
            image = null,
            qte = 100.0,
            qteSale = 100.0,
            unitSale = "pcs",
            productType = "standard",
            netPrice = 10.0
        )
    }
    
    private fun createMockApiResponse(products: List<ProductResponse>): Response<com.jayma.pos.data.remote.models.ProductListResponse> {
        val responseBody = com.jayma.pos.data.remote.models.ProductListResponse(
            products = products,
            totalRows = products.size
        )
        return Response.success(responseBody)
    }
}
