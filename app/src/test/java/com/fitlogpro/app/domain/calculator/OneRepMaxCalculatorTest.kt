package com.fitlogpro.app.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Test

class OneRepMaxCalculatorTest {

    @Test
    fun calculateEpley_withStandardValues_returnsCorrectEstimate() {
        // 100 kg for 10 reps: 100 * (1 + 10/30) = 133.3 kg
        val result = OneRepMaxCalculator.calculateEpley(100.0, 10)
        assertEquals(133.3, result, 0.1)
    }

    @Test
    fun calculateEpley_singleRep_returnsExactWeight() {
        // 1 rep should return exactly the weight
        val result = OneRepMaxCalculator.calculateEpley(140.0, 1)
        assertEquals(140.0, result, 0.001)
    }

    @Test
    fun calculateEpley_zeroOrNegativeRepsOrWeight_returnsZero() {
        assertEquals(0.0, OneRepMaxCalculator.calculateEpley(0.0, 5), 0.001)
        assertEquals(0.0, OneRepMaxCalculator.calculateEpley(-50.0, 5), 0.001)
        assertEquals(0.0, OneRepMaxCalculator.calculateEpley(100.0, 0), 0.001)
        assertEquals(0.0, OneRepMaxCalculator.calculateEpley(100.0, -2), 0.001)
    }

    @Test
    fun calculatePercentageTable_returnsAccurateBrackets() {
        val table = OneRepMaxCalculator.calculatePercentageTable(100.0)
        assertEquals(100.0, table[100] ?: 0.0, 0.1)
        assertEquals(90.0, table[90] ?: 0.0, 0.1)
        assertEquals(80.0, table[80] ?: 0.0, 0.1)
        assertEquals(70.0, table[70] ?: 0.0, 0.1)
    }
}
