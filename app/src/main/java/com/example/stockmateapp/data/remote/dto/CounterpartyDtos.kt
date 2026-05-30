package com.example.stockmateapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyDto(
    val id: Int,
    val name: String,
    val inn: String? = null,
    val contactName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val type: String
)

@Serializable
data class CreateCounterpartyRequest(
    val name: String,
    val inn: String? = null,
    val contactName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val type: String
)
