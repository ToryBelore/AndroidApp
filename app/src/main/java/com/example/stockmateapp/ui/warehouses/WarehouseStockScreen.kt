package com.example.stockmateapp.ui.warehouses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stockmateapp.data.remote.dto.StockItemDto
import com.example.stockmateapp.data.repository.WarehouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WarehouseStockUiState(
    val items: List<StockItemDto> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val lowStockOnly: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 1,
    val total: Int = 0
)

@HiltViewModel
class WarehouseStockViewModel @Inject constructor(
    private val repo: WarehouseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val warehouseId: Int = checkNotNull(savedStateHandle["warehouseId"])

    private val _uiState = MutableStateFlow(WarehouseStockUiState(isLoading = true))
    val uiState: StateFlow<WarehouseStockUiState> = _uiState.asStateFlow()

    private val searchFlow = MutableStateFlow("")

    init {
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            searchFlow.debounce(400).distinctUntilChanged().collect { q ->
                _uiState.value = _uiState.value.copy(searchQuery = q, currentPage = 1, items = emptyList())
                load(reset = true)
            }
        }
        load(reset = true)
    }

    fun onSearchChange(q: String) { searchFlow.value = q }

    fun toggleLowStock() {
        _uiState.value = _uiState.value.copy(lowStockOnly = !_uiState.value.lowStockOnly, currentPage = 1, items = emptyList())
        load(reset = true)
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.isLoadingMore || !s.hasMore) return
        load(reset = false)
    }

    private fun load(reset: Boolean) {
        val s = _uiState.value
        val page = if (reset) 1 else s.currentPage
        viewModelScope.launch {
            _uiState.value = s.copy(
                isLoading = reset && s.items.isEmpty(),
                isLoadingMore = !reset
            )
            repo.getStock(warehouseId, page, 30, s.searchQuery.takeIf { it.isNotBlank() }, s.lowStockOnly)
                .onSuccess { resp ->
                    val all = if (reset) resp.items else s.items + resp.items
                    _uiState.value = _uiState.value.copy(
                        items = all, isLoading = false, isLoadingMore = false,
                        currentPage = page + 1, total = resp.total,
                        hasMore = all.size < resp.total, error = null
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoadingMore = false, error = it.message)
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseStockScreen(
    onBack: () -> Unit,
    viewModel: WarehouseStockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            last >= total - 3 && !uiState.isLoadingMore && uiState.hasMore
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) viewModel.loadMore() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Остатки", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchChange,
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Поиск") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                ) {}
                FilterChip(
                    selected = uiState.lowStockOnly,
                    onClick = viewModel::toggleLowStock,
                    label = { Text("Низкий") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.items, key = { "${it.productId}_${it.warehouseId}" }) { item ->
                        StockItemCard(item)
                    }
                    if (uiState.isLoadingMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockItemCard(item: StockItemDto) {
    val cardColors = if (item.isLow) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
    } else {
        CardDefaults.cardColors()
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = cardColors) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${item.sku}${if (item.cellCode != null) " · ${item.cellCode}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${item.quantity} ${item.unitShortName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                if (item.isLow) {
                    Text(
                        "Мало",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
