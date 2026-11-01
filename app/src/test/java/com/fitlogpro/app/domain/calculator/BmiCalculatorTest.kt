package com.fitlogpro.app.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BmiCalculatorTest {

    @Test
    fun calculateBmi_normalWeight_returnsCorrectValueAndCategory() {
        // 70 kg at 175 cm (1.75m): 70 / (1.75 * 1.75) = 22.857 -> 22.9
        val result = BmiCalculator.evaluateMetric(70.0, 175.0)
        assertEquals(22.9, result.bmi, 0.1)
        assertEquals(BmiCategory.NORMAL, result.category)
        assertEquals(0.0, result.weightDeltaToHealthy, 0.001)
    }

    @Test
    fun calculateBmi_underweight_returnsCorrectCategoryAndDelta() {
        // 50 kg at 175 cm: 50 / 3.0625 = 16.3
        val result = BmiCalculator.evaluateMetric(50.0, 175.0)
        assertEquals(16.3, result.bmi, 0.1)
        assertEquals(BmiCategory.UNDERWEIGHT, result.category)
        assertTrue(result.weightDeltaToHealthy < 0)
    }

    @Test
    fun calculateBmi_overweight_returnsCorrectCategoryAndDelta() {
        // 85 kg at 175 cm: 85 / 3.0625 = 27.8
        val result = BmiCalculator.evaluateMetric(85.0, 175.0)
        assertEquals(27.8, result.bmi, 0.1)
        assertEquals(BmiCategory.OVERWEIGHT, result.category)
        assertTrue(result.weightDeltaToHealthy > 0)
    }

    @Test
    fun calculateBmi_obese_returnsCorrectCategory() {
        // 100 kg at 175 cm: 100 / 3.0625 = 32.7
        val result = BmiCalculator.evaluateMetric(100.0, 175.0)
        assertEquals(32.7, result.bmi, 0.1)
        assertEquals(BmiCategory.OBESE, result.category)
    }

    @Test
    fun calculateBmi_imperialUnits_returnsAccurateBmi() {
        // 154 lbs, 69 inches (5'9"): (154 * 703) / (69 * 69) = 22.7
        val result = BmiCalculator.evaluateImperial(154.0, 69.0)
        assertEquals(22.7, result.bmi, 0.1)
        assertEquals(BmiCategory.NORMAL, result.category)
    }

    @Test
    fun calculateBmi_zeroOrNegative_returnsZeroGracefully() {
        assertEquals(0.0, BmiCalculator.calculateBmi(0.0, 175.0), 0.001)
        assertEquals(0.0, BmiCalculator.calculateBmi(70.0, 0.0), 0.001)
        assertEquals(0.0, BmiCalculator.calculateBmi(-70.0, 175.0), 0.001)
        assertEquals(0.0, BmiCalculator.calculateBmi(70.0, -175.0), 0.001)
        assertEquals(0.0, BmiCalculator.calculateBmiImperial(0.0, 68.0), 0.001)
    }

    @Test
    fun healthyWeightRangeKg_calculatesProperBounds() {
        // 180 cm (1.8m): Min = 18.5 * 3.24 = 59.94 -> 59.9 kg, Max = 24.9 * 3.24 = 80.676 -> 80.7 kg
        val (minKg, maxKg) = BmiCalculator.healthyWeightRangeKg(180.0)
        assertEquals(59.9, minKg, 0.1)
        assertEquals(80.7, maxKg, 0.1)
    }
}
