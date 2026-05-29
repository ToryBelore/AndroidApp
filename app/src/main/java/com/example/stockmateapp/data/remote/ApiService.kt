package com.example.stockmateapp.data.remote

import com.example.stockmateapp.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiService(private val client: HttpClient, private val baseUrl: String) {

    suspend fun login(email: String, password: String): LoginResponse {
        return client.post("$baseUrl/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()
    }

    suspend fun refresh(refreshToken: String): RefreshResponse {
        return client.post("$baseUrl/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }.body()
    }

    suspend fun logout(refreshToken: String) {
        client.post("$baseUrl/api/v1/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }
    }
}
