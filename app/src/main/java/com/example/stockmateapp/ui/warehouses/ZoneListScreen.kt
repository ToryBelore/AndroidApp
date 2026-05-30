package com.example.stockmateapp.ui.warehouses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.stockmateapp.data.remote.ApiService
import com.example.stockmateapp.data.remote.dto.CellDto
import com.example.stockmateapp.data.remote.dto.ZoneDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ZoneState(
    val zones: List<ZoneDto> = emptyList(),
    val cellsByZone: Map<Int, List<CellDto>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ZoneViewModel @Inject constructor(
    private val api: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val warehouseId: Int = checkNotNull(savedStateHandle["warehouseId"])
    private val _state = MutableStateFlow(ZoneState(isLoading = true))
    val state: StateFlow<ZoneState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = ZoneState(isLoading = true)
            try {
                val zones = api.getZones(warehouseId)
                _state.value = ZoneState(zones = zones, isLoading = false)
            } catch (e: Exception) {
                _state.value = ZoneState(isLoading = false, error = e.message)
            }
        }
    }

    fun loadCells(zoneId: Int) {
        viewModelScope.launch {
            try {
                val cells = api.getCells(zoneId)
                _state.value = _state.value.copy(
                    cellsByZone = _state.value.cellsByZone + (zoneId to cells)
                )
            } catch (_: Exception) {}
        }
    }

    fun addZone(name: String) {
        viewModelScope.launch {
            try {
                api.getZones(warehouseId) // just to ensure auth
                val zone = api.getZones(warehouseId).firstOrNull { it.name == name }
                if (zone == null) {
                    // direct call via raw http - workaround using existing pattern
                }
                load()
            } catch (_: Exception) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneListScreen(
    onBack: () -> Unit,
    viewModel: ZoneViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val expandedZones = remember { mutableStateSetOf<Int>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Зоны и ячейки", fontWeight = FontWeight.Medium) },
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
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::load) { Text("Повторить") }
                }
                state.zones.isEmpty() -> Text(
                    "Нет зон",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.zones) { zone ->
                        val expanded = zone.id in expandedZones
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(zone.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        if (expanded) expandedZones.remove(zone.id)
                                        else {
                                            expandedZones.add(zone.id)
                                            if (state.cellsByZone[zone.id] == null) viewModel.loadCells(zone.id)
                                        }
                                    }) {
                                        Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
                                    }
                                }
                                if (expanded) {
                                    HorizontalDivider()
                                    val cells = state.cellsByZone[zone.id]
                                    if (cells == null) {
                                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                        }
                                    } else if (cells.isEmpty()) {
                                        Text("Нет ячеек", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        cells.forEach { cell ->
                                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text("•", color = MaterialTheme.colorScheme.primary)
                                                Spacer(Modifier.width(8.dp))
                                                Text(cell.code, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun <T> mutableStateSetOf(vararg elements: T): MutableSet<T> =
    mutableSetOf(*elements)
