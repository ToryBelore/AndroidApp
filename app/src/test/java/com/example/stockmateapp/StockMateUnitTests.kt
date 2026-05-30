package com.example.stockmateapp

import com.example.stockmateapp.data.local.TokenStorage
import com.example.stockmateapp.data.remote.ApiService
import com.example.stockmateapp.data.remote.dto.*
import com.example.stockmateapp.data.repository.AuthRepository
import com.example.stockmateapp.ui.auth.LoginEvent
import com.example.stockmateapp.ui.auth.LoginUiState
import com.example.stockmateapp.ui.auth.LoginViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockMateUnitTests {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- LoginUiState ---

    @Test
    fun `LoginUiState default values`() {
        val state = LoginUiState()
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `LoginUiState copy with error`() {
        val state = LoginUiState().copy(error = "Ошибка")
        assertEquals("Ошибка", state.error)
    }

    @Test
    fun `LoginUiState isLoading flag`() {
        val state = LoginUiState(isLoading = true)
        assertTrue(state.isLoading)
    }

    // --- LoginViewModel ---

    @Test
    fun `onEmailChange updates state and clears error`() {
        val repo = mockk<AuthRepository>()
        val vm = LoginViewModel(repo)
        vm.uiState.value.let { assertEquals("", it.email) }
        vm.onEmailChange("test@mail.ru")
        assertEquals("test@mail.ru", vm.uiState.value.email)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `onPasswordChange updates password field`() {
        val repo = mockk<AuthRepository>()
        val vm = LoginViewModel(repo)
        vm.onPasswordChange("secret123")
        assertEquals("secret123", vm.uiState.value.password)
    }

    @Test
    fun `login with blank email sets validation error`() = runTest {
        val repo = mockk<AuthRepository>()
        val vm = LoginViewModel(repo)
        vm.onEmailChange("")
        vm.onPasswordChange("pass")
        vm.login()
        assertEquals("Введите email и пароль", vm.uiState.value.error)
    }

    @Test
    fun `login with blank password sets validation error`() = runTest {
        val repo = mockk<AuthRepository>()
        val vm = LoginViewModel(repo)
        vm.onEmailChange("user@example.com")
        vm.onPasswordChange("")
        vm.login()
        assertEquals("Введите email и пароль", vm.uiState.value.error)
    }

    @Test
    fun `login success emits LoginSuccess event`() = runTest {
        val repo = mockk<AuthRepository>()
        coEvery { repo.login(any(), any()) } returns Result.success(Unit)
        val vm = LoginViewModel(repo)
        vm.onEmailChange("admin@stockmate.ru")
        vm.onPasswordChange("password")
        vm.login()
        advanceUntilIdle()
        assertEquals(LoginEvent.LoginSuccess, vm.event.value)
    }

    @Test
    fun `login failure shows error message`() = runTest {
        val repo = mockk<AuthRepository>()
        coEvery { repo.login(any(), any()) } returns Result.failure(Exception("Неверный пароль"))
        val vm = LoginViewModel(repo)
        vm.onEmailChange("user@example.com")
        vm.onPasswordChange("wrongpass")
        vm.login()
        advanceUntilIdle()
        assertEquals("Неверный пароль", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
    }

    // --- AuthRepository ---

    @Test
    fun `AuthRepository login success saves tokens`() = runTest {
        val api = mockk<ApiService>()
        val storage = mockk<TokenStorage>(relaxed = true)
        coEvery { api.login("a@b.com", "pass") } returns LoginResponse(
            accessToken = "acc", refreshToken = "ref", role = "admin"
        )
        val repo = AuthRepository(api, storage)
        val result = repo.login("a@b.com", "pass")
        assertTrue(result.isSuccess)
        verify { storage.saveTokens("acc", "ref", "admin") }
    }

    @Test
    fun `AuthRepository login failure returns failure`() = runTest {
        val api = mockk<ApiService>()
        val storage = mockk<TokenStorage>(relaxed = true)
        coEvery { api.login(any(), any()) } throws Exception("401")
        val repo = AuthRepository(api, storage)
        val result = repo.login("a@b.com", "wrong")
        assertTrue(result.isFailure)
        assertEquals("401", result.exceptionOrNull()?.message)
    }
}
