package com.example.stockmateapp.ui.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmateapp.data.remote.dto.ProductDto
import com.example.stockmateapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val product: ProductDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val deleted: Boolean = false
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepo: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: Int = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(ProductDetailUiState(isLoading = true))
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState(isLoading = true)
            productRepo.getProduct(productId)
                .onSuccess { _uiState.value = ProductDetailUiState(product = it) }
                .onFailure { _uiState.value = ProductDetailUiState(error = it.message) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            productRepo.deleteProduct(productId)
                .onSuccess { _uiState.value = _uiState.value.copy(deleted = true) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }
}
