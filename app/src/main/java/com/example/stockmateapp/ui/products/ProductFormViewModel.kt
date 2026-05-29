package com.example.stockmateapp.ui.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmateapp.data.remote.dto.*
import com.example.stockmateapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductFormUiState(
    val sku: String = "",
    val barcode: String = "",
    val name: String = "",
    val description: String = "",
    val minStock: String = "0",
    val purchasePrice: String = "0",
    val sellPrice: String = "0",
    val selectedCategoryId: Int? = null,
    val selectedUnitId: Int? = null,
    val categories: List<CategoryDto> = emptyList(),
    val units: List<UnitDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class ProductFormViewModel @Inject constructor(
    private val productRepo: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editId: Int? = savedStateHandle.get<Int>("productId")?.takeIf { it != -1 }

    private val _uiState = MutableStateFlow(ProductFormUiState(isLoading = true))
    val uiState: StateFlow<ProductFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val cats = productRepo.getCategories().getOrDefault(emptyList())
            val units = productRepo.getUnits().getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(categories = cats, units = units, isLoading = false)
            if (editId != null) loadProduct(editId)
        }
    }

    private fun loadProduct(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            productRepo.getProduct(id).onSuccess { p ->
                _uiState.value = _uiState.value.copy(
                    sku = p.sku, barcode = p.barcode ?: "",
                    name = p.name, description = p.description ?: "",
                    minStock = p.minStock.toString(),
                    purchasePrice = p.purchasePrice.toString(),
                    sellPrice = p.sellPrice.toString(),
                    selectedCategoryId = p.categoryId,
                    selectedUnitId = p.unitId,
                    isLoading = false
                )
            }
        }
    }

    fun onSkuChange(v: String) = update { copy(sku = v) }
    fun onBarcodeChange(v: String) = update { copy(barcode = v) }
    fun onNameChange(v: String) = update { copy(name = v) }
    fun onDescriptionChange(v: String) = update { copy(description = v) }
    fun onMinStockChange(v: String) = update { copy(minStock = v) }
    fun onPurchasePriceChange(v: String) = update { copy(purchasePrice = v) }
    fun onSellPriceChange(v: String) = update { copy(sellPrice = v) }
    fun onCategorySelect(id: Int?) = update { copy(selectedCategoryId = id) }
    fun onUnitSelect(id: Int?) = update { copy(selectedUnitId = id) }

    fun save() {
        val s = _uiState.value
        if (s.name.isBlank()) { update { copy(error = "Название обязательно") }; return }
        if (s.sku.isBlank()) { update { copy(error = "SKU обязателен") }; return }
        if (s.selectedUnitId == null) { update { copy(error = "Выберите единицу измерения") }; return }
        viewModelScope.launch {
            update { copy(isSaving = true, error = null) }
            val result = if (editId == null) {
                productRepo.createProduct(
                    CreateProductRequest(
                        sku = s.sku.trim(),
                        barcode = s.barcode.takeIf { it.isNotBlank() },
                        name = s.name.trim(),
                        description = s.description.takeIf { it.isNotBlank() },
                        categoryId = s.selectedCategoryId,
                        unitId = s.selectedUnitId,
                        minStock = s.minStock.toDoubleOrNull() ?: 0.0,
                        purchasePrice = s.purchasePrice.toDoubleOrNull() ?: 0.0,
                        sellPrice = s.sellPrice.toDoubleOrNull() ?: 0.0
                    )
                )
            } else {
                productRepo.updateProduct(
                    editId,
                    UpdateProductRequest(
                        sku = s.sku.trim(),
                        barcode = s.barcode.takeIf { it.isNotBlank() },
                        name = s.name.trim(),
                        description = s.description.takeIf { it.isNotBlank() },
                        categoryId = s.selectedCategoryId,
                        unitId = s.selectedUnitId,
                        minStock = s.minStock.toDoubleOrNull() ?: 0.0,
                        purchasePrice = s.purchasePrice.toDoubleOrNull() ?: 0.0,
                        sellPrice = s.sellPrice.toDoubleOrNull() ?: 0.0
                    )
                )
            }
            result
                .onSuccess { update { copy(isSaving = false, saved = true) } }
                .onFailure { update { copy(isSaving = false, error = it.message) } }
        }
    }

    private fun update(block: ProductFormUiState.() -> ProductFormUiState) {
        _uiState.value = _uiState.value.block()
    }
}
