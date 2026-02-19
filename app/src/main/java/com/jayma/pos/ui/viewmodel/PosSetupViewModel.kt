package com.jayma.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayma.pos.data.repository.PosDataRepository
import com.jayma.pos.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PosSetupUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val setupComplete: Boolean = false,
    val progressMessage: String = "Initializing POS..."
)

@HiltViewModel
class PosSetupViewModel @Inject constructor(
    private val posDataRepository: PosDataRepository,
    private val productRepository: ProductRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PosSetupUiState())
    val uiState: StateFlow<PosSetupUiState> = _uiState.asStateFlow()
    
    init {
        initializePos()
    }
    
    private fun initializePos() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    progressMessage = "Fetching POS data..."
                )
                
                // Step 1: Fetch POS setup data
                val posDataResult = posDataRepository.fetchPosData()
                posDataResult.fold(
                    onSuccess = { posData ->
                        _uiState.value = _uiState.value.copy(
                            progressMessage = "Loading products..."
                        )
                        
                        // Step 2: Sync products for default warehouse
                        val warehouseId = posData.defaultWarehouse
                        val syncResult = productRepository.syncAllProducts(warehouseId)
                        
                        syncResult.fold(
                            onSuccess = { productCount ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    setupComplete = true,
                                    progressMessage = "Loaded $productCount products"
                                )
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "Failed to load products: ${error.message}",
                                    setupComplete = false
                                )
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to initialize POS: ${error.message}",
                            setupComplete = false
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.message}",
                    setupComplete = false
                )
            }
        }
    }
    
    fun retry() {
        _uiState.value = PosSetupUiState()
        initializePos()
    }
}
