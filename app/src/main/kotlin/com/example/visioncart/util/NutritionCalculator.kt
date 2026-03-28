package com.example.visioncart.util

import com.example.visioncart.model.Nutriments

enum class HealthRating(val label: String, val color: String) {
    HEALTHY("Healthy", "#4CAF50"),
    MODERATE("Moderate", "#FF9800"),
    UNHEALTHY("Unhealthy", "#F44336")
}

object NutritionCalculator {

    /**
     * Thresholds based on standard health guidelines per 100g:
     * Unhealthy if:
     * - Sugar > 22.5g
     * - Fat > 17.5g
     * - Sodium > 1.5g (or Salt > 1.5g)
     */
    fun calculateRating(nutrients: Nutriments?): HealthRating {
        if (nutrients == null) return HealthRating.MODERATE

        val sugar = nutrients.sugars100g ?: 0.0
        val fat = nutrients.fat100g ?: 0.0
        val sodium = nutrients.sodium100g ?: 0.0

        return when {
            sugar > 22.5 || fat > 17.5 || sodium > 0.6 -> HealthRating.UNHEALTHY
            sugar > 5.0 || fat > 3.0 || sodium > 0.1 -> HealthRating.MODERATE
            else -> HealthRating.HEALTHY
        }
    }
}
