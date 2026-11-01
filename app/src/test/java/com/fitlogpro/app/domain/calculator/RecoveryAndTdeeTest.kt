package com.fitlogpro.app.domain.calculator

import com.fitlogpro.app.domain.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryAndTdeeTest {

    @Test
    fun baseRecoveryHours_matchesMuscleGroupHierarchy() {
        assertEquals(72, MuscleRecoveryCalculator.getBaseRecoveryHours(MuscleGroup.LEGS))
        assertEquals(72, MuscleRecoveryCalculator.getBaseRecoveryHours(MuscleGroup.BACK))
        assertEquals(60, MuscleRecoveryCalculator.getBaseRecoveryHours(MuscleGroup.CHEST))
        assertEquals(48, MuscleRecoveryCalculator.getBaseRecoveryHours(MuscleGroup.SHOULDERS))
        assertEquals(36, MuscleRecoveryCalculator.getBaseRecoveryHours(MuscleGroup.ARMS))
        assertEquals(36, MuscleRecoveryCalculator.getBaseRecoveryHours(MuscleGroup.CORE))
    }

    @Test
    fun bmrCalculation_producesAccurateMifflinValues() {
        val bmrMale = TdeeCalculator.calculateBmr(70.0, 175.0, 25, true)
        assertEquals(1673.75, bmrMale, 0.1)

        val bmrFemale = TdeeCalculator.calculateBmr(60.0, 165.0, 30, false)
        // (10 * 60) + (6.25 * 165) - (5 * 30) - 161 = 600 + 1031.25 - 150 - 161 = 1320.25
        assertEquals(1320.25, bmrFemale, 0.1)

        // Invalid inputs safely produce 0.0
        assertEquals(0.0, TdeeCalculator.calculateBmr(0.0, 175.0, 25, true), 0.001)
        assertEquals(0.0, TdeeCalculator.calculateBmr(70.0, -10.0, 25, true), 0.001)
    }

    @Test
    fun tdeeCalculation_correctlyMultipliesActivity() {
        val bmr = 1600.0
        val sedentary = TdeeCalculator.calculateTdee(bmr, ActivityLevel.SEDENTARY)
        assertEquals(1920.0, sedentary, 0.1)

        val moderate = TdeeCalculator.calculateTdee(bmr, ActivityLevel.MODERATELY_ACTIVE)
        assertEquals(2480.0, moderate, 0.1)
    }

    @Test
    fun macroTargets_allocatesAdequateProteinForAthletes() {
        val targets = TdeeCalculator.calculateMacroTargets(
            weightKg = 80.0,
            heightCm = 180.0,
            age = 26,
            isMale = true,
            activityLevel = ActivityLevel.MODERATELY_ACTIVE,
            goal = NutritionGoal.LEAN_BULK
        )

        // 80kg * 2.0g/kg = 160g protein
        assertEquals(160, targets.proteinGrams)
        // 80kg * 0.8g/kg = 64g fat
        assertEquals(64, targets.fatGrams)
        assertTrue("Carbs should be positive and balanced", targets.carbGrams > 100)
        assertTrue("Target calories should exceed BMR during bulk", targets.targetCalories > targets.bmr)
        assertTrue("Estimated weekly change should be positive for lean bulk", targets.estimatedWeeklyChangeKg > 0.0)
    }
}
