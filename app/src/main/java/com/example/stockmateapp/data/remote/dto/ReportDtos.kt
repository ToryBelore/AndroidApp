package com.example.stockmateapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DashboardDto(
    val totalStockValue: Double,
    val lowStockCount: Int,
    val todayOperationsCount: Int,
    val stockByCategory: List<CategoryStock>
)

@Serializable
data class CategoryStock(val categoryName: String, val value: Double)

@Serializable
data class StockReportItem(
    val productId: Int,
    val productName: String,
    val sku: String,
    val categoryName: String? = null,
    val warehouseName: String,
    val quantity: Double,
    val unitShortName: String,
    val sellPrice: Double,
    val totalValue: Double
)

@Serializable
data class MovementReportItem(
    val date: String,
    val documentId: Int,
    val docType: String,
    val productName: String,
    val sku: String,
    val quantity: Double,
    val warehouseName: String? = null
)

@Serializable
data class ReplenishmentDto(
    val id: Int,
    val productId: Int,
    val productName: String,
    val warehouseId: Int,
    val warehouseName: String,
    val quantity: Double,
    val status: String,
    val createdBy: Int,
    val createdByName: String,
    val assignedTo: Int? = null,
    val assignedToName: String? = null,
    val comment: String? = null,
    val createdAt: String
)

@Serializable
data class CreateReplenishmentRequest(
    val productId: Int,
    val warehouseId: Int,
    val quantity: Double,
    val comment: String? = null
)
