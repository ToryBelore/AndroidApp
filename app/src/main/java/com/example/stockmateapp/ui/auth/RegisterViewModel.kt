package com.example.stockmateapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmateapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class RegisterEvent {
    data object RegisterSuccess : RegisterEvent()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<RegisterEvent?>(null)
    val event: StateFlow<RegisterEvent?> = _event.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, error = null)
    }

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value, error = null)
    }

    fun register() {
        val state = _uiState.value
        if (state.fullName.isBlank()) {
            _uiState.value = state.copy(error = "Введите имя")
            return
        }
        if (state.email.isBlank()) {
            _uiState.value = state.copy(error = "Введите email")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "Пароль минимум 6 символов")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Пароли не совпадают")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = authRepository.register(state.email.trim(), state.password, state.fullName.trim())
            if (result.isSuccess) {
                _event.value = RegisterEvent.RegisterSuccess
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Ошибка регистрации"
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
            }
        }
    }

    fun consumeEvent() {
        _event.value = null
    }
}
