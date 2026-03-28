package com.example.visioncart.api

import android.graphics.Bitmap
import com.example.visioncart.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun detectExpirationDate(bitmap: Bitmap): String? {
        if (BuildConfig.GEMINI_API_KEY.isEmpty()) return null
        
        return withContext(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text("Find the expiration date or best before date in this product image. Reply only with the date found, or 'Not found'.")
                    }
                )
                val text = response.text
                if (text?.contains("Not found", ignoreCase = true) == true) null else text
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
