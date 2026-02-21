package com.jayma.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayma.pos.data.local.entities.ProductEntity
import com.jayma.pos.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductUiState(
    val products: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedCategoryId: Int? = null,
    val selectedBrandId: Int? = null,
    val showInStockOnly: Boolean = false
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val selectedCategoryId = MutableStateFlow<Int?>(null)
    private val selectedBrandId = MutableStateFlow<Int?>(null)
    private val showInStockOnly = MutableStateFlow(false)

    init {
        // Combine all filters and search query
        combine(
            productRepository.getAllProducts(),
            searchQuery,
            selectedCategoryId,
            selectedBrandId,
            showInStockOnly
        ) { allProducts, query, categoryId, brandId, _ ->
            var filtered = allProducts

            // Always filter out out-of-stock items (qteSale <= 0)
            filtered = filtered.filter { it.qteSale > 0 }

            // Apply search filter
            if (query.isNotBlank()) {
                val lowerQuery = query.lowercase()
                filtered = filtered.filter {
                    it.name.lowercase().contains(lowerQuery) ||
                    it.code.lowercase().contains(lowerQuery) ||
                    it.barcode?.lowercase()?.contains(lowerQuery) == true
                }
            }

            // Apply category filter
            if (categoryId != null) {
                filtered = filtered.filter { it.categoryId == categoryId }
            }

            // Apply brand filter
            if (brandId != null) {
                filtered = filtered.filter { it.brandId == brandId }
            }

            filtered
        }.onEach { filteredProducts ->
            _uiState.value = _uiState.value.copy(products = filteredProducts)
        }.launchIn(viewModelScope)
    }

    fun searchProducts(query: String) {
        searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun filterByCategory(categoryId: Int?) {
        selectedCategoryId.value = categoryId
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun filterByBrand(brandId: Int?) {
        selectedBrandId.value = brandId
        _uiState.value = _uiState.value.copy(selectedBrandId = brandId)
    }

    fun toggleInStockOnly() {
        val newValue = !showInStockOnly.value
        showInStockOnly.value = newValue
        _uiState.value = _uiState.value.copy(showInStockOnly = newValue)
    }

    fun clearFilters() {
        searchQuery.value = ""
        selectedCategoryId.value = null
        selectedBrandId.value = null
        showInStockOnly.value = false
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedCategoryId = null,
            selectedBrandId = null,
            showInStockOnly = false
        )
    }

    fun syncProducts(warehouseId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = productRepository.syncAllProducts(warehouseId)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to sync products"
                    )
                }
            )
        }
    }
}
