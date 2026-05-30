package com.example.stockmateapp.ui.operations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stockmateapp.data.remote.dto.DocumentDto
import com.example.stockmateapp.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentListUiState(
    val documents: List<DocumentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val typeFilter: String? = null
)

@HiltViewModel
class DocumentListViewModel @Inject constructor(
    private val repo: DocumentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DocumentListUiState(isLoading = true))
    val uiState: StateFlow<DocumentListUiState> = _uiState.asStateFlow()

    init { load() }

    fun setFilter(type: String?) {
        _uiState.value = _uiState.value.copy(typeFilter = type)
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repo.getDocuments(type = _uiState.value.typeFilter)
                .onSuccess { _uiState.value = _uiState.value.copy(documents = it.items, isLoading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
        }
    }
}

private val DOC_TYPES = listOf(
    null to "Все",
    "RECEIPT" to "Приход",
    "SHIPMENT" to "Расход",
    "TRANSFER" to "Перемещение",
    "ADJUSTMENT" to "Корр."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    onDocumentClick: (Int) -> Unit,
    onAddDocument: () -> Unit,
    viewModel: DocumentListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Операции", fontWeight = FontWeight.Medium) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDocument) {
                Icon(Icons.Default.Add, contentDescription = "Создать")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DOC_TYPES) { (type, label) ->
                    FilterChip(
                        selected = uiState.typeFilter == type,
                        onClick = { viewModel.setFilter(type) },
                        label = { Text(label) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::load) { Text("Повторить") }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.documents, key = { it.id }) { doc ->
                        DocumentCard(doc, onClick = { onDocumentClick(doc.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(doc: DocumentDto, onClick: () -> Unit) {
    val typeLabel = when (doc.type) {
        "RECEIPT" -> "Приход"
        "SHIPMENT" -> "Расход"
        "TRANSFER" -> "Перемещение"
        "ADJUSTMENT" -> "Корректировка"
        "INVENTORY" -> "Инвентаризация"
        else -> doc.type
    }
    val statusColor = when (doc.status) {
        "CONDUCTED" -> MaterialTheme.colorScheme.primary
        "CANCELLED" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val containerColor = when (doc.status) {
        "CONDUCTED" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        "CANCELLED" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        else -> CardDefaults.cardColors().containerColor
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$typeLabel #${doc.id}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    doc.createdAt.take(10),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                doc.status,
                style = MaterialTheme.typography.labelLarge,
                color = statusColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
