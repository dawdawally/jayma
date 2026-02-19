package com.jayma.pos.ui.viewmodel

import com.jayma.pos.data.local.entities.ProductEntity
import com.jayma.pos.data.repository.PosDataRepository
import com.jayma.pos.data.repository.SaleRepository
import com.jayma.pos.sync.SyncManager
import com.jayma.pos.util.SharedPreferencesHelper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class CartViewModelTest {
    
    private lateinit var saleRepository: SaleRepository
    private lateinit var posDataRepository: PosDataRepository
    private lateinit var sharedPreferences: SharedPreferencesHelper
    private lateinit var syncManager: SyncManager
    private lateinit var cartViewModel: CartViewModel
    
    @Before
    fun setup() {
        saleRepository = mock()
        posDataRepository = mock()
        sharedPreferences = mock()
        syncManager = mock()
        cartViewModel = CartViewModel(saleRepository, posDataRepository, sharedPreferences, syncManager)
    }
    
    @Test
    fun `addToCart adds product to cart`() = runTest {
        // Given
        val product = createProduct(1, "Test Product", qteSale = 10.0)
        
        // When
        cartViewModel.addToCart(product, 2.0)
        
        // Then
        val state = cartViewModel.uiState.value
        assertEquals(1, state.cartItems.size)
        assertEquals(2.0, state.cartItems[0].quantity, 0.01)
        assertEquals(20.0, state.subtotal, 0.01) // 2 * 10.0
    }
    
    @Test
    fun `addToCart updates quantity if product already in cart`() = runTest {
        // Given
        val product = createProduct(1, "Test Product", qteSale = 10.0)
        cartViewModel.addToCart(product, 2.0)
        
        // When
        cartViewModel.addToCart(product, 3.0)
        
        // Then
        val state = cartViewModel.uiState.value
        assertEquals(1, state.cartItems.size)
        assertEquals(5.0, state.cartItems[0].quantity, 0.01) // 2 + 3
    }
    
    @Test
    fun `addToCart does not exceed available stock`() = runTest {
        // Given
        val product = createProduct(1, "Test Product", qteSale = 5.0)
        cartViewModel.addToCart(product, 3.0)
        
        // When - try to add more than available
        cartViewModel.addToCart(product, 5.0)
        
        // Then - quantity should not exceed stock
        val state = cartViewModel.uiState.value
        assertEquals(5.0, state.cartItems[0].quantity, 0.01) // Max is 5.0
    }
    
    @Test
    fun `removeFromCart removes product from cart`() = runTest {
        // Given
        val product = createProduct(1, "Test Product")
        cartViewModel.addToCart(product, 1.0)
        
        // When
        cartViewModel.removeFromCart(1)
        
        // Then
        val state = cartViewModel.uiState.value
        assertTrue(state.cartItems.isEmpty())
    }
    
    @Test
    fun `updateQuantity updates product quantity`() = runTest {
        // Given
        val product = createProduct(1, "Test Product", qteSale = 10.0)
        cartViewModel.addToCart(product, 2.0)
        
        // When
        cartViewModel.updateQuantity(1, 5.0)
        
        // Then
        val state = cartViewModel.uiState.value
        assertEquals(5.0, state.cartItems[0].quantity, 0.01)
    }
    
    @Test
    fun `clearCart removes all items`() = runTest {
        // Given
        cartViewModel.addToCart(createProduct(1, "Product 1"), 1.0)
        cartViewModel.addToCart(createProduct(2, "Product 2"), 1.0)
        
        // When
        cartViewModel.clearCart()
        
        // Then
        val state = cartViewModel.uiState.value
        assertTrue(state.cartItems.isEmpty())
        assertEquals(0.0, state.total, 0.01)
    }
    
    @Test
    fun `calculateTotal includes tax discount and shipping`() = runTest {
        // Given
        cartViewModel.addToCart(createProduct(1, "Product", netPrice = 100.0), 1.0)
        cartViewModel.setTax(10.0)
        cartViewModel.setDiscount(5.0)
        cartViewModel.setShipping(2.0)
        
        // Then
        val state = cartViewModel.uiState.value
        // Total = 100 (subtotal) + 10 (tax) + 2 (shipping) - 5 (discount) = 107
        assertEquals(107.0, state.total, 0.01)
    }
    
    private fun createProduct(
        id: Int,
        name: String,
        netPrice: Double = 10.0,
        qteSale: Double = 100.0
    ): ProductEntity {
        return ProductEntity(
            id = id,
            productVariantId = id,
            code = "P$id",
            name = name,
            barcode = "BAR$id",
            image = null,
            qte = qteSale,
            qteSale = qteSale,
            unitSale = "pcs",
            productType = "standard",
            netPrice = netPrice,
            synced = true
        )
    }
}
