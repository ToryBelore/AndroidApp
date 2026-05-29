package com.example.stockmateapp.data.local.dao

import androidx.room.*
import com.example.stockmateapp.data.local.entities.CachedProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products_cache ORDER BY name ASC")
    fun getAllFlow(): Flow<List<CachedProduct>>

    @Query("SELECT * FROM products_cache WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun search(query: String): List<CachedProduct>

    @Query("SELECT * FROM products_cache WHERE id = :id")
    suspend fun findById(id: Int): CachedProduct?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<CachedProduct>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: CachedProduct)

    @Query("DELETE FROM products_cache")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM products_cache")
    suspend fun count(): Int
}
