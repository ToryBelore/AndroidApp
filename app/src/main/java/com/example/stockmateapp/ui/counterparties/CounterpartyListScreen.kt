package com.example.stockmateapp.ui.counterparties

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.stockmateapp.data.remote.ApiService
import com.example.stockmateapp.data.remote.dto.CounterpartyDto
import com.example.stockmateapp.data.remote.dto.CreateCounterpartyRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CounterpartyState(
    val items: List<CounterpartyDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDialog: Boolean = false,
    val editing: CounterpartyDto? = null,
    val filterType: String? = null
)

@HiltViewModel
class CounterpartyViewModel @Inject constructor(private val api: ApiService) : ViewModel() {
    private val _state = MutableStateFlow(CounterpartyState(isLoading = true))
    val state: StateFlow<CounterpartyState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                _state.value = _state.value.copy(
                    items = api.getCounterparties(_state.value.filterType),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun setFilter(type: String?) {
        _state.value = _state.value.copy(filterType = type)
        load()
    }

    fun showCreate() { _state.value = _state.value.copy(showDialog = true, editing = null) }
    fun showEdit(item: CounterpartyDto) { _state.value = _state.value.copy(showDialog = true, editing = item) }
    fun hideDialog() { _state.value = _state.value.copy(showDialog = false, editing = null) }

    fun save(name: String, inn: String, contact: String, phone: String, email: String, type: String) {
        viewModelScope.launch {
            try {
                val req = CreateCounterpartyRequest(
                    name = name,
                    inn = inn.ifBlank { null },
                    contactName = contact.ifBlank { null },
                    phone = phone.ifBlank { null },
                    email = email.ifBlank { null },
                    type = type
                )
                val editing = _state.value.editing
                if (editing != null) api.updateCounterparty(editing.id, req) else api.createCounterparty(req)
                hideDialog()
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterpartyListScreen(
    onBack: () -> Unit,
    canEdit: Boolean = true,
    viewModel: CounterpartyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.showDialog) {
        CounterpartyDialog(
            existing = state.editing,
            onDismiss = viewModel::hideDialog,
            onSave = { name, inn, contact, phone, email, type ->
                viewModel.save(name, inn, contact, phone, email, type)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Контрагенты", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(onClick = viewModel::showCreate) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Filter tabs
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(null to "Все", "SUPPLIER" to "Поставщики", "BUYER" to "Покупатели").forEach { (type, label) ->
                    FilterChip(
                        selected = state.filterType == type,
                        onClick = { viewModel.setFilter(type) },
                        label = { Text(label) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    state.error != null -> Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::load) { Text("Повторить") }
                    }
                    state.items.isEmpty() -> Text(
                        "Нет контрагентов",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.items) { item ->
                            CounterpartyCard(
                                item = item,
                                onClick = if (canEdit) ({ viewModel.showEdit(item) }) else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterpartyCard(item: CounterpartyDto, onClick: (() -> Unit)?) {
    Card(
        onClick = { onClick?.invoke() },
        modifier = Modifier.fillMaxWidth(),
        enabled = onClick != null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Business,
                contentDescription = null,
                tint = if (item.type == "SUPPLIER") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (!item.inn.isNullOrBlank()) Text("ИНН: ${item.inn}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!item.contactName.isNullOrBlank()) Text(item.contactName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!item.phone.isNullOrBlank()) Text(item.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Badge(containerColor = if (item.type == "SUPPLIER") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer) {
                Text(if (item.type == "SUPPLIER") "Поставщик" else "Покупатель", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun CounterpartyDialog(
    existing: CounterpartyDto?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var inn by remember { mutableStateOf(existing?.inn ?: "") }
    var contact by remember { mutableStateOf(existing?.contactName ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: "SUPPLIER") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Новый контрагент" else "Редактировать") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название*") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = inn, onValueChange = { inn = it }, label = { Text("ИНН") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Контактное лицо") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Телефон") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Тип", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == "SUPPLIER", onClick = { type = "SUPPLIER" })
                    Text("Поставщик")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = type == "BUYER", onClick = { type = "BUYER" })
                    Text("Покупатель")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name, inn, contact, phone, email, type) }, enabled = name.isNotBlank()) {
                Text("Сохранить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
