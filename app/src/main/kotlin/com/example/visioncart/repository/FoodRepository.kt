package com.example.visioncart.repository

import com.example.visioncart.BuildConfig
import com.example.visioncart.api.FoodApiService
import com.example.visioncart.model.ScannedProduct
import com.example.visioncart.util.NutritionCalculator
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class FoodRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val apiService: FoodApiService by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(FoodApiService.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FoodApiService::class.java)
    }

    suspend fun fetchProduct(barcode: String): ScannedProduct? {
        return try {
            val response = apiService.getProduct(barcode, BuildConfig.OFF_USER_AGENT)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == 1 && body.product != null) {
                    val p = body.product
                    val rating = NutritionCalculator.calculateRating(p.nutriments)
                    val cals = p.nutriments?.energyKcal100g ?: 0.0
                    val sugar = p.nutriments?.sugars100g ?: 0.0
                    val fat = p.nutriments?.fat100g ?: 0.0
                    val salt = p.nutriments?.salt100g ?: 0.0
                    
                    val unit = if (p.quantity?.lowercase()?.contains("ml") == true) "ml" else "g"
                    val detailedRating = "${rating.label}. Energy: ${cals.toInt()} kcal, Sugar: ${sugar}${unit}, Fats: ${fat}${unit}, Salt: ${salt}${unit} per 100${unit}."
                    
                    ScannedProduct(
                        brand = p.brands ?: "Unknown Brand",
                        name = p.productName ?: "Unknown Product",
                        price = "N/A", 
                        time = "Just now",
                        expires = "N/A", 
                        weight = p.quantity ?: "Unknown weight",
                        barcode = barcode.trim(), 
                        category = p.categories?.split(",")?.firstOrNull() ?: "General",
                        ingredients = p.ingredientsText ?: "No ingredients listed",
                        allergens = p.allergens?.replace(Regex("(?i)en[:;]\\s*"), "")?.trim() ?: "None listed",
                        healthRating = detailedRating
                    )
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
