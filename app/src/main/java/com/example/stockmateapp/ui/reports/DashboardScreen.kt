package com.example.stockmateapp.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stockmateapp.data.remote.dto.DashboardDto
import com.example.stockmateapp.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val data: DashboardDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(private val api: ApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState(isLoading = true)
            try {
                val data = api.getDashboard()
                _uiState.value = DashboardUiState(data = data)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState(error = e.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onProductsClick: () -> Unit,
    onWarehousesClick: () -> Unit,
    onDocumentsClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("StockMate") }) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Ошибка загрузки", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::load) { Text("Повторить") }
                    Spacer(Modifier.height(16.dp))
                    QuickNav(onProductsClick, onWarehousesClick, onDocumentsClick)
                }
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val data = uiState.data!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Стоимость запасов",
                            value = "%.0f ₽".format(data.totalStockValue),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Мало на складе",
                            value = "${data.lowStockCount}",
                            modifier = Modifier.weight(1f),
                            highlight = data.lowStockCount > 0
                        )
                    }
                    StatCard(
                        label = "Операций сегодня",
                        value = "${data.todayOperationsCount}",
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (data.stockByCategory.isNotEmpty()) {
                        Text("Запасы по категориям", style = MaterialTheme.typography.titleSmall)
                        data.stockByCategory.take(5).forEach { cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat.categoryName, style = MaterialTheme.typography.bodyMedium)
                                Text("%.0f ₽".format(cat.value), style = MaterialTheme.typography.bodyMedium)
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    QuickNav(onProductsClick, onWarehousesClick, onDocumentsClick)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = if (highlight) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuickNav(
    onProductsClick: () -> Unit,
    onWarehousesClick: () -> Unit,
    onDocumentsClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onProductsClick, modifier = Modifier.fillMaxWidth()) { Text("Товары") }
        Button(onClick = onWarehousesClick, modifier = Modifier.fillMaxWidth()) { Text("Склады") }
        Button(onClick = onDocumentsClick, modifier = Modifier.fillMaxWidth()) { Text("Операции") }
    }
}
