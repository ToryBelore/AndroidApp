package com.example.stockmateapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products_cache")
data class CachedProduct(
    @PrimaryKey val id: Int,
    val sku: String,
    val name: String,
    val barcode: String?,
    val categoryId: Int?,
    val categoryName: String?,
    val unitId: Int,
    val unitShortName: String,
    val minStock: Double,
    val purchasePrice: Double,
    val sellPrice: Double,
    val photoUrl: String?,
    val updatedAt: Long = System.currentTimeMillis()
)
