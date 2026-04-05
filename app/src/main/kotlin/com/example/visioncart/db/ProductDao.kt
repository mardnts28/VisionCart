package com.example.visioncart.db

import androidx.room.*
import com.example.visioncart.model.ScannedProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM scanned_products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ScannedProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ScannedProduct): Long

    @Query("DELETE FROM scanned_products")
    suspend fun clearHistory()

    @Query("UPDATE scanned_products SET isStarred = :isStarred WHERE id = :productId")
    suspend fun updateStarredStatus(productId: Long, isStarred: Boolean)

    @Query("UPDATE scanned_products SET userPrice = :price WHERE id = :productId")
    suspend fun updatePrice(productId: Long, price: String)
}
