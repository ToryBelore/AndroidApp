package com.example.stockmateapp.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmateapp.data.remote.dto.ProductDto
import com.example.stockmateapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductListUiState(
    val products: List<ProductDto> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedCategoryId: Int? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val total: Int = 0
)

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productRepo: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    private val searchFlow = MutableStateFlow("")

    init {
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            searchFlow
                .debounce(400)
                .distinctUntilChanged()
                .collect { query ->
                    _uiState.value = _uiState.value.copy(
                        searchQuery = query,
                        currentPage = 1,
                        products = emptyList()
                    )
                    loadProducts(reset = true)
                }
        }
        loadProducts(reset = true)
    }

    fun onSearchChange(query: String) {
        searchFlow.value = query
    }

    fun onCategoryFilter(categoryId: Int?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId, currentPage = 1, products = emptyList())
        loadProducts(reset = true)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return
        loadProducts(reset = false)
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(currentPage = 1, products = emptyList())
        loadProducts(reset = true)
    }

    private fun loadProducts(reset: Boolean) {
        val state = _uiState.value
        val page = if (reset) 1 else state.currentPage
        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = reset && state.products.isEmpty(),
                isLoadingMore = !reset
            )
            val result = productRepo.getProducts(
                page = page,
                search = state.searchQuery.takeIf { it.isNotBlank() },
                categoryId = state.selectedCategoryId
            )
            result.onSuccess { response ->
                val allProducts = if (reset) response.items else state.products + response.items
                _uiState.value = _uiState.value.copy(
                    products = allProducts,
                    isLoading = false,
                    isLoadingMore = false,
                    currentPage = page + 1,
                    total = response.total,
                    hasMore = allProducts.size < response.total,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message ?: "Ошибка загрузки"
                )
            }
        }
    }
}
