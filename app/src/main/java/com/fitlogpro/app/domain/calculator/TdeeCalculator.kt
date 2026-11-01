package com.fitlogpro.app.domain.calculator

enum class ActivityLevel(val displayName: String, val multiplier: Double, val description: String) {
    SEDENTARY("Sedentary", 1.2, "Desk job, little to no exercise"),
    LIGHTLY_ACTIVE("Lightly Active", 1.375, "1-3 workout sessions / week"),
    MODERATELY_ACTIVE("Moderately Active", 1.55, "3-5 workout sessions / week"),
    VERY_ACTIVE("Very Active", 1.725, "6-7 hard workout sessions / week"),
    EXTREMELY_ACTIVE("Extremely Active", 1.9, "Daily heavy training or physical job")
}

enum class NutritionGoal(val displayName: String, val calorieDelta: Int, val description: String) {
    AGGRESSIVE_CUT("Aggressive Cut", -500, "Lose ~0.5kg/wk with high protein muscle sparing"),
    MODERATE_CUT("Moderate Cut", -300, "Gradual fat loss with optimal gym strength"),
    MAINTENANCE("Maintenance", 0, "Hold weight, recomposition & muscle repair"),
    LEAN_BULK("Lean Bulk", 250, "Gain ~0.25kg/wk with minimal fat accumulation"),
    AGGRESSIVE_BULK("Heavy Bulk", 500, "Maximum strength, size & mass accumulation")
}

data class MacroTarget(
    val bmr: Int,
    val tdee: Int,
    val targetCalories: Int,
    val proteinGrams: Int,
    val carbGrams: Int,
    val fatGrams: Int,
    val estimatedWeeklyChangeKg: Double
)

object TdeeCalculator {

    /**
     * Mifflin-St Jeor formula for Basal Metabolic Rate (BMR)
     */
    fun calculateBmr(weightKg: Double, heightCm: Double, age: Int = 25, isMale: Boolean = true): Double {
        if (weightKg <= 0.0 || heightCm <= 0.0 || age <= 0) return 0.0
        val base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age)
        return if (isMale) base + 5.0 else base - 161.0
    }

    /**
     * Total Daily Energy Expenditure (TDEE) = BMR * Activity Multiplier
     */
    fun calculateTdee(bmr: Double, activityLevel: ActivityLevel): Double {
        if (bmr <= 0.0) return 0.0
        return bmr * activityLevel.multiplier
    }

    /**
     * Calculates adaptive target calories and macronutrient breakdown for strength & physique athletes
     */
    fun calculateMacroTargets(
        weightKg: Double,
        heightCm: Double,
        age: Int = 25,
        isMale: Boolean = true,
        activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
        goal: NutritionGoal = NutritionGoal.MAINTENANCE
    ): MacroTarget {
        val bmr = calculateBmr(weightKg, heightCm, age, isMale)
        val tdee = calculateTdee(bmr, activityLevel)
        if (bmr <= 0.0 || tdee <= 0.0) {
            return MacroTarget(0, 0, 0, 0, 0, 0, 0.0)
        }

        val targetCalories = (tdee + goal.calorieDelta).toInt().coerceAtLeast(1200)

        // 2.0g protein per kg for active gym lifters (4 kcal/g)
        val proteinGrams = (weightKg * 2.0).toInt().coerceIn(60, 280)
        // 0.8g fat per kg for hormone and joint health (9 kcal/g)
        val fatGrams = (weightKg * 0.8).toInt().coerceIn(35, 120)

        val calsFromProteinAndFat = (proteinGrams * 4) + (fatGrams * 9)
        val remainingCals = (targetCalories - calsFromProteinAndFat).coerceAtLeast(200)
        val carbGrams = (remainingCals / 4).coerceAtLeast(40)

        // 7700 kcal is approximately 1kg of body mass
        val weeklyChangeKg = (goal.calorieDelta * 7.0) / 7700.0

        return MacroTarget(
            bmr = bmr.toInt(),
            tdee = tdee.toInt(),
            targetCalories = targetCalories,
            proteinGrams = proteinGrams,
            carbGrams = carbGrams,
            fatGrams = fatGrams,
            estimatedWeeklyChangeKg = weeklyChangeKg
        )
    }
}
