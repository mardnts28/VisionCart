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
                        text("You are identifying the expiration date on a food product package for a blind user. Scan this image carefully for dates. Look for keywords like 'EXP', 'BB', 'Best Before', or just a date format. If found, reply ONLY with the date (e.g. June 2025). If NOT found or blurry, reply 'Not clearly visible'.")
                    }
                )
                val text = response.text?.trim()
                if (text?.contains("visible", ignoreCase = true) == true) "Expiry not visible" else text
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    suspend fun identifyProduct(bitmap: Bitmap): String? {
        if (BuildConfig.GEMINI_API_KEY.isEmpty()) return null
        
        return withContext(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text("You are identifying a product for a blind user. Scan the image carefully. Identify the following details: Brand, Product Name, Weight, Category, Ingredients, Allergens, and a Health Rating (Healthy, Moderate, or Unhealthy). " + 
                             "Format the output EXACTLY as: Brand | Name | Weight | Category | Ingredients | Allergens | HealthRating. " +
                             "If some info is not visible, use 'N/A'. Be as accurate as possible. If it's blurry, make your best professional guess based on logos and colors.")
                    }
                )
                val text = response.text?.trim()
                if (text == null || text.contains("Unknown", ignoreCase = true) || !text.contains("|")) null else text
            } catch (e: Exception) {
                null
            }
        }
    }
    suspend fun askNutritionalQuestion(question: String, productContext: String): String? {
        if (BuildConfig.GEMINI_API_KEY.isEmpty()) {
            return "⚠️ AI Assistant is unavailable at the moment. Please try again later."
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        text(
                            "You are a concise shopping assistant for a visually impaired person. " +
                            "Product info: [$productContext]. " +
                            "User question: [$question]. " +
                            "Answer in 2-3 short sentences maximum. Be direct, clear, and focus on safety. " +
                            "Start your answer immediately without repeating the question."
                        )
                    }
                )
                response.text?.trim() ?: "⚠️ AI Assistant is unavailable at the moment. Please try again later."
            } catch (e: Exception) {
                e.printStackTrace()
                "⚠️ AI Assistant is temporarily unavailable. Please check your connection and try again."
            }
        }
    }
}
