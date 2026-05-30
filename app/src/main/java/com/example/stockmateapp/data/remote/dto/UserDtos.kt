package com.example.stockmateapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val fullName: String,
    val role: String,
    val isActive: Boolean
)

@Serializable
data class UpdateUserRequest(
    val fullName: String? = null,
    val role: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class CreateUserRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val role: String
)
