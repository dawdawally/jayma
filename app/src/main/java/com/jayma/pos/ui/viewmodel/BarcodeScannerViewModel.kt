package com.jayma.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayma.pos.data.local.entities.ProductEntity
import com.jayma.pos.data.repository.ProductRepository
import com.jayma.pos.ui.scanner.BarcodeScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    private val _scanResult = MutableStateFlow<BarcodeScanResult>(BarcodeScanResult.Idle)
    val scanResult: StateFlow<BarcodeScanResult> = _scanResult.asStateFlow()
    
    private val _foundProduct = MutableStateFlow<ProductEntity?>(null)
    val foundProduct: StateFlow<ProductEntity?> = _foundProduct.asStateFlow()
    
    /**
     * Scan barcode and search for product
     */
    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                // Search by barcode code
                val product = productRepository.getProductByCode(barcode)
                
                if (product != null) {
                    _scanResult.value = BarcodeScanResult.Success(product)
                    _foundProduct.value = product
                } else {
                    _scanResult.value = BarcodeScanResult.NotFound(barcode)
                }
            } catch (e: Exception) {
                _scanResult.value = BarcodeScanResult.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Called when product is found and should be added to cart
     */
    fun onProductFound(product: ProductEntity) {
        _foundProduct.value = product
    }
    
    /**
     * Reset scan result
     */
    fun reset() {
        _scanResult.value = BarcodeScanResult.Idle
        _foundProduct.value = null
    }
}
