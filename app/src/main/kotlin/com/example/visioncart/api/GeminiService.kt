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
                        text("You are identifying a product for a blind user from a camera frame. Look at the product and tell me the Brand, Name and approximate weight (ml or g). Format as 'Brand | Name | Weight'. If it's a bit blurry, give your best guess based on colors and logos. Reply 'Unknown' only if absolutely nothing is visible.")
                    }
                )
                val text = response.text?.trim()
                if (text?.contains("Unknown", ignoreCase = true) == true) null else text
            } catch (e: Exception) {
                null
            }
        }
    }
    suspend fun askNutritionalQuestion(question: String, productContext: String): String? {
        if (BuildConfig.GEMINI_API_KEY.isEmpty()) return "AI is not configured."
        
        return withContext(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        text("You are a helpful shopping assistant for a blind person. Based on this product data: [$productContext], answer the user's question: [$question]. Be concise and clear. Focus on safety and ingredients.")
                    }
                )
                response.text?.trim() ?: "I couldn't find an answer."
            } catch (e: Exception) {
                "Sorry, I encountered an error answering that."
            }
        }
    }
}
