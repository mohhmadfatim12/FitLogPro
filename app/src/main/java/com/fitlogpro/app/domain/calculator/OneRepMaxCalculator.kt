package com.fitlogpro.app.domain.calculator

import kotlin.math.round

object OneRepMaxCalculator {

    /**
     * Estimates 1RM using the industry standard Epley formula:
     * 1RM = weight * (1 + reps / 30.0)
     *
     * Special cases:
     * - If reps == 1, 1RM equals the weight lifted.
     * - If reps <= 0 or weight <= 0, returns 0.0.
     */
    fun calculateEpley(weight: Double, reps: Int): Double {
        if (weight <= 0.0 || reps <= 0) return 0.0
        if (reps == 1) return roundToDecimals(weight, 1)

        val estimated = weight * (1.0 + reps / 30.0)
        return roundToDecimals(estimated, 1)
    }

    /**
     * Alternative Brzycki formula:
     * 1RM = weight * (36.0 / (37.0 - reps))
     * Valid for reps between 1 and 36.
     */
    fun calculateBrzycki(weight: Double, reps: Int): Double {
        if (weight <= 0.0 || reps <= 0) return 0.0
        if (reps == 1) return roundToDecimals(weight, 1)
        if (reps >= 37) return calculateEpley(weight, reps)

        val estimated = weight * (36.0 / (37.0 - reps))
        return roundToDecimals(estimated, 1)
    }

    /**
     * Calculates the estimated 1RM percentage table (e.g. 95%, 90%, 85%, etc.)
     */
    fun calculatePercentageTable(oneRepMax: Double): Map<Int, Double> {
        val percentages = listOf(100, 95, 90, 85, 80, 75, 70, 65, 60, 50)
        return percentages.associateWith { pct ->
            roundToDecimals(oneRepMax * (pct / 100.0), 1)
        }
    }

    private fun roundToDecimals(value: Double, decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10.0 }
        return round(value * multiplier) / multiplier
    }
}
