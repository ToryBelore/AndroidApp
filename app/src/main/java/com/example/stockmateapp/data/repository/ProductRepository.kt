package com.example.stockmateapp.data.repository

import com.example.stockmateapp.data.local.AppDatabase
import com.example.stockmateapp.data.local.entities.CachedProduct
import com.example.stockmateapp.data.remote.ApiService
import com.example.stockmateapp.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val api: ApiService,
    private val db: AppDatabase
) {

    suspend fun getProducts(
        page: Int = 1, size: Int = 30,
        search: String? = null, categoryId: Int? = null, sort: String? = null
    ): Result<ProductListResponse> = runCatching {
        val response = api.getProducts(page, size, search, categoryId, sort)
        if (page == 1 && search == null && categoryId == null) {
            db.productDao().insertAll(response.items.map { it.toCached() })
        }
        response
    }.recoverCatching { _ ->
        val cached = if (!search.isNullOrBlank()) {
            db.productDao().search(search)
        } else {
            db.productDao().search("")
        }
        ProductListResponse(cached.map { it.toDto() }, cached.size, 1, cached.size)
    }

    suspend fun getProduct(id: Int): Result<ProductDto> = runCatching {
        val dto = api.getProduct(id)
        db.productDao().insert(dto.toCached())
        dto
    }.recoverCatching {
        db.productDao().findById(id)?.toDto() ?: throw it
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

    private fun ProductDto.toCached() = CachedProduct(
        id = id, sku = sku, name = name, barcode = barcode,
        categoryId = categoryId, categoryName = categoryName,
        unitId = unitId, unitShortName = unitShortName,
        minStock = minStock, purchasePrice = purchasePrice,
        sellPrice = sellPrice, photoUrl = photoUrl
    )

    private fun CachedProduct.toDto() = ProductDto(
        id = id, sku = sku, name = name, barcode = barcode,
        categoryId = categoryId, categoryName = categoryName,
        unitId = unitId, unitShortName = unitShortName,
        minStock = minStock, purchasePrice = purchasePrice,
        sellPrice = sellPrice, photoUrl = photoUrl,
        createdAt = ""
    )
}
