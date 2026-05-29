package com.example.stockmateapp.data.repository

import com.example.stockmateapp.data.remote.ApiService
import com.example.stockmateapp.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(private val api: ApiService) {

    suspend fun getProducts(
        page: Int = 1, size: Int = 30,
        search: String? = null, categoryId: Int? = null, sort: String? = null
    ): Result<ProductListResponse> = runCatching {
        api.getProducts(page, size, search, categoryId, sort)
    }

    suspend fun getProduct(id: Int): Result<ProductDto> = runCatching {
        api.getProduct(id)
    }

    suspend fun createProduct(req: CreateProductRequest): Result<ProductDto> = runCatching {
        api.createProduct(req)
    }

    suspend fun updateProduct(id: Int, req: UpdateProductRequest): Result<ProductDto> = runCatching {
        api.updateProduct(id, req)
    }

    suspend fun deleteProduct(id: Int): Result<Unit> = runCatching {
        api.deleteProduct(id)
    }

    suspend fun getCategories(): Result<List<CategoryDto>> = runCatching {
        api.getCategories()
    }

    suspend fun getUnits(): Result<List<UnitDto>> = runCatching {
        api.getUnits()
    }
}
