package com.example.visioncart.model

import androidx.room.*

@Entity(tableName = "scanned_products")
data class ScannedProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var brand: String?,
    var name: String?,
    var price: String?,
    var time: String?,
    var expires: String?,
    var weight: String?,
    var barcode: String?,
    var category: String?,
    var ingredients: String?,
    var allergens: String?,
    var healthRating: String? = "Moderate",
    var isStarred: Boolean = false,
    var userPrice: String? = null
)
