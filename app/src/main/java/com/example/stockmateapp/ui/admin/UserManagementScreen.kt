package com.example.stockmateapp.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
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
import com.example.stockmateapp.data.remote.dto.CreateUserRequest
import com.example.stockmateapp.data.remote.dto.UpdateUserRequest
import com.example.stockmateapp.data.remote.dto.UserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserManagementState(
    val users: List<UserDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val editingUser: UserDto? = null
)

@HiltViewModel
class UserManagementViewModel @Inject constructor(private val api: ApiService) : ViewModel() {
    private val _state = MutableStateFlow(UserManagementState(isLoading = true))
    val state: StateFlow<UserManagementState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                _state.value = _state.value.copy(users = api.getUsers(), isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun showCreate() { _state.value = _state.value.copy(showCreateDialog = true) }
    fun hideCreate() { _state.value = _state.value.copy(showCreateDialog = false) }
    fun showEdit(user: UserDto) { _state.value = _state.value.copy(editingUser = user) }
    fun hideEdit() { _state.value = _state.value.copy(editingUser = null) }

    fun createUser(email: String, password: String, fullName: String, role: String) {
        viewModelScope.launch {
            try {
                api.createUser(CreateUserRequest(email, password, fullName, role))
                hideCreate()
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateUser(id: Int, role: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                api.updateUser(id, UpdateUserRequest(role = role, isActive = isActive))
                hideEdit()
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}

val ROLES = listOf("Admin", "Manager", "Warehouse", "Analyst")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onBack: () -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.showCreateDialog) {
        CreateUserDialog(
            onDismiss = viewModel::hideCreate,
            onCreate = { email, pass, name, role -> viewModel.createUser(email, pass, name, role) }
        )
    }
    state.editingUser?.let { user ->
        EditUserDialog(
            user = user,
            onDismiss = viewModel::hideEdit,
            onSave = { role, active -> viewModel.updateUser(user.id, role, active) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Пользователи", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showCreate) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить")
            }
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
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.users) { user ->
                        UserCard(user = user, onClick = { viewModel.showEdit(user) })
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(user: UserDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = if (user.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Badge(containerColor = roleColor(user.role)) {
                    Text(user.role, style = MaterialTheme.typography.labelSmall)
                }
                if (!user.isActive) {
                    Spacer(Modifier.height(4.dp))
                    Text("Неактивен", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun roleColor(role: String) = when (role) {
    "Admin" -> MaterialTheme.colorScheme.error
    "Manager" -> MaterialTheme.colorScheme.primary
    "Warehouse" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.secondary
}

@Composable
private fun CreateUserDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Warehouse") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый пользователь") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Имя") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Роль", style = MaterialTheme.typography.labelMedium)
                ROLES.forEach { role ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedRole == role, onClick = { selectedRole = role })
                        Text(role)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (email.isNotBlank() && password.isNotBlank() && fullName.isNotBlank()) onCreate(email, password, fullName, selectedRole) },
                enabled = email.isNotBlank() && password.isNotBlank() && fullName.isNotBlank()
            ) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun EditUserDialog(
    user: UserDto,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    var selectedRole by remember { mutableStateOf(user.role) }
    var isActive by remember { mutableStateOf(user.isActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(user.fullName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Роль", style = MaterialTheme.typography.labelMedium)
                ROLES.forEach { role ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedRole == role, onClick = { selectedRole = role })
                        Text(role)
                    }
                }
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Активен", modifier = Modifier.weight(1f))
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selectedRole, isActive) }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
