package com.fitlogpro.app.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class OneRepMaxFormulaComparisonTest {

    @Test
    fun epleyAndBrzycki_bothMatchExactWeightAtSingleRep() {
        val testWeights = listOf(60.0, 100.0, 142.5, 200.0)
        testWeights.forEach { w ->
            val epley = OneRepMaxCalculator.calculateEpley(w, 1)
            val brzycki = OneRepMaxCalculator.calculateBrzycki(w, 1)
            assertEquals(w, epley, 0.001)
            assertEquals(w, brzycki, 0.001)
        }
    }

    @Test
    fun epleyAndBrzycki_withinFivePercentAcrossStandardRepRange() {
        val weight = 100.0
        for (reps in 2..10) {
            val epley = OneRepMaxCalculator.calculateEpley(weight, reps)
            val brzycki = OneRepMaxCalculator.calculateBrzycki(weight, reps)
            val delta = abs(epley - brzycki)
            val percentageDiff = (delta / epley) * 100.0
            assertTrue("Diff at $reps reps must be < 5% (was $percentageDiff%)", percentageDiff < 5.0)
        }
    }
}
