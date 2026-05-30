package com.example.stockmateapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BatchDto(
    val id: Int,
    val productId: Int,
    val warehouseId: Int,
    val warehouseName: String,
    val quantity: Double,
    val purchaseDate: String,
    val expiryDate: String? = null,
    val documentId: Int? = null
)
