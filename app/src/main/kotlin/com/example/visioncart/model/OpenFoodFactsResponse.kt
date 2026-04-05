package com.example.visioncart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenFoodFactsResponse(
    @SerialName("product") val product: ProductData? = null,
    @SerialName("status") val status: Int? = null,
    @SerialName("status_verbose") val statusVerbose: String? = null
)

@Serializable
data class ProductData(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("brands") val brands: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("quantity") val quantity: String? = null,
    @SerialName("categories") val categories: String? = null,
    @SerialName("ingredients_text") val ingredientsText: String? = null,
    @SerialName("allergens") val allergens: String? = null,
    @SerialName("nutriments") val nutriments: Nutriments? = null,
    @SerialName("image_url") val imageUrl: String? = null
)

@Serializable
data class Nutriments(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
    @SerialName("saturated-fat_100g") val saturatedFat100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydrates100g: Double? = null,
    @SerialName("sugars_100g") val sugars100g: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("sodium_100g") val sodium100g: Double? = null,
    @SerialName("salt_100g") val salt100g: Double? = null,
    @SerialName("fiber_100g") val fiber100g: Double? = null
)
