package com.example.stockmateapp.ui.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stockmateapp.data.remote.dto.ReplenishmentDto
import com.example.stockmateapp.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RequestListUiState(
    val requests: List<ReplenishmentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RequestListViewModel @Inject constructor(private val api: ApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(RequestListUiState(isLoading = true))
    val uiState: StateFlow<RequestListUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = RequestListUiState(isLoading = true)
            try {
                val requests = api.getRequests()
                _uiState.value = RequestListUiState(requests = requests)
            } catch (e: Exception) {
                _uiState.value = RequestListUiState(error = e.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestListScreen(
    onBack: () -> Unit,
    viewModel: RequestListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заявки на пополнение") },
                actions = {
                    IconButton(onClick = viewModel::load) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::load) { Text("Повторить") }
                }
                uiState.requests.isEmpty() -> Text(
                    "Заявок нет",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(uiState.requests, key = { it.id }) { req ->
                        RequestItem(req)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestItem(req: ReplenishmentDto) {
    val statusColor = when (req.status) {
        "DONE" -> MaterialTheme.colorScheme.primary
        "REJECTED" -> MaterialTheme.colorScheme.error
        "IN_PROGRESS" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    ListItem(
        headlineContent = { Text(req.productName) },
        supportingContent = {
            Text(
                "${req.warehouseName} · ${req.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Text(req.status, style = MaterialTheme.typography.labelSmall, color = statusColor)
        }
    )
}
