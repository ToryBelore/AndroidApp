package com.example.stockmateapp.data.remote

import com.example.stockmateapp.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiService(private val client: HttpClient, private val baseUrl: String) {

    // Auth
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

    // Categories & Units
    suspend fun getCategories(): List<CategoryDto> =
        client.get("$baseUrl/api/v1/categories").body()

    suspend fun getUnits(): List<UnitDto> =
        client.get("$baseUrl/api/v1/units").body()

    // Products
    suspend fun getProducts(
        page: Int = 1, size: Int = 30,
        search: String? = null, categoryId: Int? = null, sort: String? = null
    ): ProductListResponse {
        return client.get("$baseUrl/api/v1/products") {
            parameter("page", page)
            parameter("size", size)
            if (search != null) parameter("search", search)
            if (categoryId != null) parameter("category_id", categoryId)
            if (sort != null) parameter("sort", sort)
        }.body()
    }

    suspend fun getProduct(id: Int): ProductDto =
        client.get("$baseUrl/api/v1/products/$id").body()

    suspend fun createProduct(req: CreateProductRequest): ProductDto =
        client.post("$baseUrl/api/v1/products") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun updateProduct(id: Int, req: UpdateProductRequest): ProductDto =
        client.put("$baseUrl/api/v1/products/$id") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun deleteProduct(id: Int) {
        client.delete("$baseUrl/api/v1/products/$id")
    }

    // Documents
    suspend fun getDocuments(
        page: Int = 1, size: Int = 30,
        type: String? = null, status: String? = null
    ): DocumentListResponse {
        return client.get("$baseUrl/api/v1/documents") {
            parameter("page", page)
            parameter("size", size)
            if (type != null) parameter("type", type)
            if (status != null) parameter("status", status)
        }.body()
    }

    suspend fun getDocument(id: Int): DocumentDto =
        client.get("$baseUrl/api/v1/documents/$id").body()

    suspend fun createDocument(req: CreateDocumentRequest): DocumentDto =
        client.post("$baseUrl/api/v1/documents") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun conductDocument(id: Int): DocumentDto =
        client.post("$baseUrl/api/v1/documents/$id/conduct").body()

    suspend fun cancelDocument(id: Int): DocumentDto =
        client.post("$baseUrl/api/v1/documents/$id/cancel").body()

    // Warehouses
    suspend fun getWarehouses(): List<WarehouseDto> =
        client.get("$baseUrl/api/v1/warehouses").body()

    suspend fun getWarehouseStock(
        warehouseId: Int, page: Int = 1, size: Int = 30,
        search: String? = null, lowStockOnly: Boolean = false
    ): StockListResponse {
        return client.get("$baseUrl/api/v1/warehouses/$warehouseId/stock") {
            parameter("page", page)
            parameter("size", size)
            if (search != null) parameter("search", search)
            if (lowStockOnly) parameter("low_stock", "true")
        }.body()
    }
}
