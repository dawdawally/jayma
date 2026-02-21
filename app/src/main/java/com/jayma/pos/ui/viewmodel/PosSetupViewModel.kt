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
import java.net.UnknownHostException
import javax.inject.Inject

data class PosSetupUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val setupComplete: Boolean = false,
    val progressMessage: String = "Initializing POS...",
    val shouldNavigateToSettings: Boolean = false // Flag to indicate host resolution error
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
                        
                        // Step 2: Sync first page immediately for instant feedback
                        val warehouseId = posData.defaultWarehouse
                        val firstPageResult = productRepository.syncFirstPage(warehouseId)
                        
                        firstPageResult.fold(
                            onSuccess = { firstPageCount ->
                                // Show first page immediately
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    setupComplete = true,
                                    progressMessage = "Loaded $firstPageCount products (loading more...)"
                                )
                                
                                // Load remaining products in background
                                viewModelScope.launch {
                                    val remainingResult = productRepository.syncRemainingProducts(warehouseId)
                                    remainingResult.fold(
                                        onSuccess = { remainingCount ->
                                            val totalCount = firstPageCount + remainingCount
                                            _uiState.value = _uiState.value.copy(
                                                progressMessage = "Loaded $totalCount products"
                                            )
                                        },
                                        onFailure = { error ->
                                            // Don't fail setup if remaining pages fail, just log
                                            _uiState.value = _uiState.value.copy(
                                                progressMessage = "Loaded $firstPageCount products (some failed to load)"
                                            )
                                        }
                                    )
                                }
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
                        // Check if it's a host resolution error
                        val isHostError = error is UnknownHostException || 
                                         error.cause is UnknownHostException ||
                                         error.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                                         error.message?.contains("UnknownHostException", ignoreCase = true) == true
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = if (isHostError) {
                                "Unable to connect to server. Please check your domain settings."
                            } else {
                                "Failed to initialize POS: ${error.message}"
                            },
                            setupComplete = false,
                            shouldNavigateToSettings = isHostError
                        )
                    }
                )
            } catch (e: Exception) {
                // Check if it's a host resolution error
                val isHostError = e is UnknownHostException || 
                                 e.cause is UnknownHostException ||
                                 e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                                 e.message?.contains("UnknownHostException", ignoreCase = true) == true
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = if (isHostError) {
                        "Unable to connect to server. Please check your domain settings."
                    } else {
                        "Unexpected error: ${e.message}"
                    },
                    setupComplete = false,
                    shouldNavigateToSettings = isHostError
                )
            }
        }
    }
    
    fun retry() {
        _uiState.value = PosSetupUiState()
        initializePos()
    }
}
