package com.example.stockmateapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WarehouseDto(
    val id: Int,
    val name: String,
    val address: String? = null,
    val managerId: Int? = null,
    val managerName: String? = null,
    val isActive: Boolean
)

@Serializable
data class StockItemDto(
    val productId: Int,
    val productName: String,
    val sku: String,
    val unitShortName: String,
    val warehouseId: Int,
    val cellId: Int? = null,
    val cellCode: String? = null,
    val quantity: Double,
    val minStock: Double,
    val isLow: Boolean
)

@Serializable
data class StockListResponse(
    val items: List<StockItemDto>,
    val total: Int,
    val page: Int,
    val size: Int
)
