package com.example.stockmateapp.data.repository

import com.example.stockmateapp.data.remote.ApiService
import com.example.stockmateapp.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WarehouseRepository @Inject constructor(private val api: ApiService) {

    suspend fun getWarehouses(): Result<List<WarehouseDto>> = runCatching {
        api.getWarehouses()
    }

    suspend fun getStock(
        warehouseId: Int, page: Int = 1, size: Int = 30,
        search: String? = null, lowStockOnly: Boolean = false
    ): Result<StockListResponse> = runCatching {
        api.getWarehouseStock(warehouseId, page, size, search, lowStockOnly)
    }
}
