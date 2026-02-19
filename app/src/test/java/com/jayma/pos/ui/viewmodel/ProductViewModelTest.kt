package com.jayma.pos.ui.viewmodel

import com.jayma.pos.data.local.entities.ProductEntity
import com.jayma.pos.data.repository.ProductRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ProductViewModelTest {
    
    private lateinit var productRepository: ProductRepository
    private lateinit var productViewModel: ProductViewModel
    
    @Before
    fun setup() {
        productRepository = mock()
        productViewModel = ProductViewModel(productRepository)
    }
    
    @Test
    fun `initial state has empty products`() = runTest {
        // Given
        whenever(productRepository.getAllProducts()).thenReturn(flowOf(emptyList()))
        
        // When
        val initialState = productViewModel.uiState.value
        
        // Then
        assertTrue(initialState.products.isEmpty())
        assertFalse(initialState.isLoading)
        assertNull(initialState.error)
    }
    
    @Test
    fun `searchProducts filters products by query`() = runTest {
        // Given
        val allProducts = listOf(
            createProduct(1, "Apple iPhone"),
            createProduct(2, "Samsung Galaxy"),
            createProduct(3, "Apple Watch")
        )
        whenever(productRepository.getAllProducts()).thenReturn(flowOf(allProducts))
        whenever(productRepository.searchProducts("Apple")).thenReturn(
            flowOf(listOf(allProducts[0], allProducts[2]))
        )
        
        // When
        productViewModel.searchProducts("Apple")
        
        // Then
        verify(productRepository).searchProducts("Apple")
    }
    
    @Test
    fun `syncProducts triggers repository sync`() = runTest {
        // Given
        val warehouseId = 1
        whenever(productRepository.syncAllProducts(warehouseId))
            .thenReturn(kotlin.Result.success(10))
        
        // When
        productViewModel.syncProducts(warehouseId)
        
        // Then
        verify(productRepository).syncAllProducts(warehouseId)
    }
    
    private fun createProduct(id: Int, name: String): ProductEntity {
        return ProductEntity(
            id = id,
            productVariantId = id,
            code = "P$id",
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
}
