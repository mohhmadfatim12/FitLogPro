package com.fitlogpro.app.domain.calculator

import kotlin.math.round

enum class BmiCategory(
    val label: String,
    val rangeText: String,
    val colorHex: Long,
    val description: String
) {
    UNDERWEIGHT(
        label = "Underweight",
        rangeText = "< 18.5",
        colorHex = 0xFF38BDF8, // Sky Blue
        description = "Below standard weight range. Consider nutrient-rich calories and progressive strength training."
    ),
    NORMAL(
        label = "Normal Weight",
        rangeText = "18.5 – 24.9",
        colorHex = 0xFF22C55E, // Vibrant Emerald Green
        description = "Ideal body mass index. Maintain balanced nutrition, adequate recovery, and consistent workouts."
    ),
    OVERWEIGHT(
        label = "Overweight",
        rangeText = "25.0 – 29.9",
        colorHex = 0xFFF59E0B, // Amber / Warm Orange
        description = "Slightly above standard range. Focus on caloric moderation, daily steps, and resistance training."
    ),
    OBESE(
        label = "Obese",
        rangeText = "≥ 30.0",
        colorHex = 0xFFEF4444, // Vibrant Coral Red
        description = "Significantly above standard range. Consult healthcare professionals and adopt sustainable lifestyle changes."
    );

    companion object {
        fun fromBmi(bmi: Double): BmiCategory {
            return when {
                bmi < 18.5 -> UNDERWEIGHT
                bmi < 25.0 -> NORMAL
                bmi < 30.0 -> OVERWEIGHT
                else -> OBESE
            }
        }
    }
}

data class BmiResult(
    val bmi: Double,
    val category: BmiCategory,
    val minHealthyWeight: Double,
    val maxHealthyWeight: Double,
    val weightDeltaToHealthy: Double, // 0.0 if within range, negative if underweight, positive if overweight
    val isMetric: Boolean
)

object BmiCalculator {

    /**
     * Calculates BMI using metric units (kg, cm).
     * Returns 0.0 for non-positive input.
     */
    fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        if (weightKg <= 0.0 || heightCm <= 0.0) return 0.0
        val heightMeters = heightCm / 100.0
        val rawBmi = weightKg / (heightMeters * heightMeters)
        return roundToDecimals(rawBmi, 1)
    }

    /**
     * Calculates BMI using imperial units (lbs, total inches).
     * Returns 0.0 for non-positive input.
     */
    fun calculateBmiImperial(weightLbs: Double, heightInches: Double): Double {
        if (weightLbs <= 0.0 || heightInches <= 0.0) return 0.0
        val rawBmi = (weightLbs * 703.0) / (heightInches * heightInches)
        return roundToDecimals(rawBmi, 1)
    }

    /**
     * Evaluates BMI and returns comprehensive results for metric inputs.
     */
    fun evaluateMetric(weightKg: Double, heightCm: Double): BmiResult {
        val bmi = calculateBmi(weightKg, heightCm)
        val category = BmiCategory.fromBmi(bmi)
        val (minHealthyKg, maxHealthyKg) = healthyWeightRangeKg(heightCm)
        
        val delta = when {
            weightKg <= 0.0 -> 0.0
            weightKg < minHealthyKg -> roundToDecimals(weightKg - minHealthyKg, 1)
            weightKg > maxHealthyKg -> roundToDecimals(weightKg - maxHealthyKg, 1)
            else -> 0.0
        }

        return BmiResult(
            bmi = bmi,
            category = category,
            minHealthyWeight = minHealthyKg,
            maxHealthyWeight = maxHealthyKg,
            weightDeltaToHealthy = delta,
            isMetric = true
        )
    }

    /**
     * Evaluates BMI and returns comprehensive results for imperial inputs.
     */
    fun evaluateImperial(weightLbs: Double, heightInches: Double): BmiResult {
        val bmi = calculateBmiImperial(weightLbs, heightInches)
        val category = BmiCategory.fromBmi(bmi)
        val (minHealthyLbs, maxHealthyLbs) = healthyWeightRangeImperial(heightInches)

        val delta = when {
            weightLbs <= 0.0 -> 0.0
            weightLbs < minHealthyLbs -> roundToDecimals(weightLbs - minHealthyLbs, 1)
            weightLbs > maxHealthyLbs -> roundToDecimals(weightLbs - maxHealthyLbs, 1)
            else -> 0.0
        }

        return BmiResult(
            bmi = bmi,
            category = category,
            minHealthyWeight = minHealthyLbs,
            maxHealthyWeight = maxHealthyLbs,
            weightDeltaToHealthy = delta,
            isMetric = false
        )
    }

    /**
     * Returns the healthy weight range (BMI 18.5 – 24.9) in kg for a given height in cm.
     */
    fun healthyWeightRangeKg(heightCm: Double): Pair<Double, Double> {
        if (heightCm <= 0.0) return Pair(0.0, 0.0)
        val heightMeters = heightCm / 100.0
        val minKg = roundToDecimals(18.5 * heightMeters * heightMeters, 1)
        val maxKg = roundToDecimals(24.9 * heightMeters * heightMeters, 1)
        return Pair(minKg, maxKg)
    }

    /**
     * Returns the healthy weight range (BMI 18.5 – 24.9) in lbs for a given height in inches.
     */
    fun healthyWeightRangeImperial(heightInches: Double): Pair<Double, Double> {
        if (heightInches <= 0.0) return Pair(0.0, 0.0)
        val minLbs = roundToDecimals((18.5 * heightInches * heightInches) / 703.0, 1)
        val maxLbs = roundToDecimals((24.9 * heightInches * heightInches) / 703.0, 1)
        return Pair(minLbs, maxLbs)
    }

    private fun roundToDecimals(value: Double, decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10.0 }
        return round(value * multiplier) / multiplier
    }
}
