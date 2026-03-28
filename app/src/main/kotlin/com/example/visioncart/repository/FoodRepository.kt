package com.example.visioncart.repository

import com.example.visioncart.HistoryActivity
import com.example.visioncart.api.FoodApiService
import com.example.visioncart.util.NutritionCalculator
import com.example.visioncart.BuildConfig
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

    suspend fun fetchProduct(barcode: String): HistoryActivity.ScannedProduct? {
        return try {
            val response = apiService.getProduct(barcode, BuildConfig.OFF_USER_AGENT)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == 1 && body.product != null) {
                    val p = body.product
                    val rating = NutritionCalculator.calculateRating(p.nutriments)
                    
                    HistoryActivity.ScannedProduct(
                        brand = p.brands ?: "Unknown Brand",
                        name = p.productName ?: "Unknown Product",
                        price = "N/A", 
                        time = "Just now",
                        expires = "N/A", 
                        weight = p.quantity ?: "Unknown weight",
                        barcode = p.code ?: barcode,
                        category = p.categories?.split(",")?.firstOrNull() ?: "General",
                        ingredients = p.ingredientsText ?: "No ingredients listed",
                        allergens = p.allergens ?: "None listed",
                        healthRating = rating.label
                    )
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
