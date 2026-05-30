package com.example.stockmateapp.data.repository

import com.example.stockmateapp.data.local.TokenStorage
import com.example.stockmateapp.data.remote.ApiService
import com.example.stockmateapp.data.remote.dto.RegisterResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage
) {

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(email, password)
            tokenStorage.saveTokens(response.accessToken, response.refreshToken, response.role)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, fullName: String): Result<RegisterResponse> {
        return try {
            val response = api.register(email, password, fullName)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            val refreshToken = tokenStorage.getRefreshToken() ?: return
            api.logout(refreshToken)
        } catch (_: Exception) {
        } finally {
            tokenStorage.clear()
        }
    }

    fun isLoggedIn() = tokenStorage.isLoggedIn()

    fun getRole() = tokenStorage.getRole()
}
