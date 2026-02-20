package com.jayma.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayma.pos.data.local.entities.ProductEntity
import com.jayma.pos.data.model.CartItem
import com.jayma.pos.data.repository.PosDataRepository
import com.jayma.pos.data.repository.SaleRepository
import com.jayma.pos.sync.SyncManager
import com.jayma.pos.util.SharedPreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val discount: Double = 0.0,
    val shipping: Double = 0.0,
    val total: Double = 0.0,
    val selectedClientId: Int? = null,
    val selectedWarehouseId: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val posDataRepository: PosDataRepository,
    private val sharedPreferences: SharedPreferencesHelper,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val cartItems = mutableListOf<CartItem>()

    init {
        loadDefaults()
    }

    private fun loadDefaults() {
        viewModelScope.launch {
            val warehouseId = sharedPreferences.getDefaultWarehouse()
            val clientId = sharedPreferences.getDefaultClient()
            
            _uiState.value = _uiState.value.copy(
                selectedWarehouseId = warehouseId,
                selectedClientId = clientId
            )
        }
    }

    fun addToCart(product: ProductEntity, quantity: Double = 1.0) {
        val existingItem = cartItems.find { it.product.id == product.id }
        
        if (existingItem != null) {
            // Update quantity if item already exists
            val newQuantity = existingItem.quantity + quantity
            if (newQuantity <= product.qteSale) {
                existingItem.quantity = newQuantity
            } else {
                // Quantity exceeds available stock
                return
            }
        } else {
            // Add new item to cart
            if (quantity <= product.qteSale && quantity > 0) {
                cartItems.add(CartItem(product, quantity))
            } else {
                return
            }
        }
        
        updateCartState()
    }

    fun removeFromCart(productId: Int) {
        cartItems.removeAll { it.product.id == productId }
        updateCartState()
    }

    fun updateQuantity(productId: Int, newQuantity: Double) {
        val item = cartItems.find { it.product.id == productId }
        item?.let {
            if (newQuantity <= it.product.qteSale && newQuantity > 0) {
                it.quantity = newQuantity
                updateCartState()
            } else if (newQuantity <= 0) {
                removeFromCart(productId)
            }
        }
    }

    fun clearCart() {
        cartItems.clear()
        updateCartState()
    }

    fun setTax(tax: Double) {
        _uiState.value = _uiState.value.copy(tax = tax)
        calculateTotal()
    }

    fun setDiscount(discount: Double) {
        _uiState.value = _uiState.value.copy(discount = discount)
        calculateTotal()
    }

    fun setShipping(shipping: Double) {
        _uiState.value = _uiState.value.copy(shipping = shipping)
        calculateTotal()
    }

    fun setClient(clientId: Int) {
        _uiState.value = _uiState.value.copy(selectedClientId = clientId)
    }

    private fun updateCartState() {
        val subtotal = cartItems.sumOf { it.subtotal }
        
        // Create a new list with new CartItem instances to ensure StateFlow detects the change
        val newCartItems = cartItems.map { 
            CartItem(
                product = it.product,
                quantity = it.quantity,
                unitPrice = it.unitPrice
            )
        }
        
        _uiState.value = _uiState.value.copy(
            cartItems = newCartItems,
            subtotal = subtotal
        )
        calculateTotal()
    }

    private fun calculateTotal() {
        val state = _uiState.value
        val total = state.subtotal + state.tax + state.shipping - state.discount
        _uiState.value = state.copy(total = total.coerceAtLeast(0.0))
    }

    fun checkout(
        paymentMethodId: Int,
        paymentAmount: Double,
        notes: String? = null
    ) {
        val state = _uiState.value
        
        if (state.cartItems.isEmpty()) {
            _uiState.value = state.copy(error = "Cart is empty")
            return
        }

        if (state.selectedClientId == null) {
            _uiState.value = state.copy(error = "Please select a client")
            return
        }

        if (state.selectedWarehouseId == null) {
            _uiState.value = state.copy(error = "Please select a warehouse")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                // Create sale entity
                val sale = com.jayma.pos.data.local.entities.SaleEntity(
                    clientId = state.selectedClientId!!,
                    warehouseId = state.selectedWarehouseId!!,
                    taxRate = if (state.subtotal > 0) (state.tax / state.subtotal) * 100 else 0.0,
                    taxNet = state.tax,
                    discount = state.discount,
                    shipping = state.shipping,
                    grandTotal = state.total,
                    notes = notes
                )

                // Create sale details
                val saleDetails = state.cartItems.map { cartItem ->
                    com.jayma.pos.data.local.entities.SaleDetailEntity(
                        saleLocalId = 0, // Will be set by repository
                        productId = cartItem.product.id,
                        productVariantId = cartItem.product.productVariantId,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.unitPrice,
                        subtotal = cartItem.subtotal,
                        productName = cartItem.product.name
                    )
                }

                // Create payment
                val payments = listOf(
                    com.jayma.pos.data.local.entities.PaymentEntity(
                        saleLocalId = 0, // Will be set by repository
                        paymentMethodId = paymentMethodId,
                        amount = paymentAmount,
                        change = (paymentAmount - state.total).coerceAtLeast(0.0)
                    )
                )

                // Save sale locally
                val saleLocalId = saleRepository.createSale(sale, saleDetails, payments)

                // Trigger immediate sale upload if online
                // This ensures sales are uploaded as soon as possible (within seconds)
                // Background sync (every 5 minutes) will also handle it as a backup
                syncManager.triggerSaleUpload()
                
                // Trigger product sync to pull latest stock information after sale
                // This ensures stock quantities are updated to reflect the items sold
                // The server will have decremented stock, and we need to sync to get updated quantities
                syncManager.triggerProductSync()
                
                // Clear cart on success
                clearCart()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create sale: ${e.message}"
                )
            }
        }
    }
}
