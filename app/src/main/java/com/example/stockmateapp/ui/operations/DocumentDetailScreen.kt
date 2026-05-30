package com.example.stockmateapp.ui.operations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
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

data class DocumentDetailUiState(
    val document: DocumentDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    private val repo: DocumentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val docId: Int = checkNotNull(savedStateHandle["documentId"])
    private val _uiState = MutableStateFlow(DocumentDetailUiState(isLoading = true))
    val uiState: StateFlow<DocumentDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DocumentDetailUiState(isLoading = true)
            repo.getDocument(docId)
                .onSuccess { _uiState.value = DocumentDetailUiState(document = it) }
                .onFailure { _uiState.value = DocumentDetailUiState(error = it.message) }
        }
    }

    fun conduct() {
        viewModelScope.launch {
            repo.conduct(docId)
                .onSuccess { _uiState.value = _uiState.value.copy(document = it) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun cancel() {
        viewModelScope.launch {
            repo.cancel(docId)
                .onSuccess { _uiState.value = _uiState.value.copy(document = it) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    onBack: () -> Unit,
    viewModel: DocumentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Документ #${uiState.document?.id ?: "..."}", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text(
                    uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                uiState.document != null -> {
                    val doc = uiState.document!!
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                        // Info card
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Информация", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                    DocInfoRow("Тип", when (doc.type) {
                                        "RECEIPT" -> "Приход"
                                        "SHIPMENT" -> "Расход"
                                        "TRANSFER" -> "Перемещение"
                                        else -> doc.type
                                    })
                                    DocInfoRow("Статус", doc.status)
                                    DocInfoRow("Создан", doc.createdAt.take(16).replace("T", " "))
                                    if (doc.warehouseFromName != null) DocInfoRow("Со склада", doc.warehouseFromName)
                                    if (doc.warehouseToName != null) DocInfoRow("На склад", doc.warehouseToName)
                                    if (!doc.comment.isNullOrBlank()) DocInfoRow("Комментарий", doc.comment)
                                }
                            }
                        }

                        // Items card
                        item {
                            Spacer(Modifier.height(12.dp))
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Позиции", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        items(doc.items) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.Medium)
                                        Text(item.sku, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        "${item.quantity} шт",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (doc.status == "DRAFT") {
                            item {
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = viewModel::cancel,
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Отменить") }
                                    Button(
                                        onClick = viewModel::conduct,
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Провести", fontWeight = FontWeight.Medium) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
