package com.fitlogpro.app.domain.calculator

import kotlin.math.round

object RpeCalculator {

    const val MIN_RPE = 6.0
    const val MAX_RPE = 10.0

    /**
     * Parses and clamps user-inputted RPE string to valid 6.0-10.0 range with 0.5 step.
     * Returns null if unparseable or empty.
     */
    fun parseAndValidateRpe(input: String): Double? {
        val clean = input.trim().replace(',', '.')
        if (clean.isEmpty()) return null
        val value = clean.toDoubleOrNull() ?: return null

        val clamped = value.coerceIn(MIN_RPE, MAX_RPE)
        // Round to nearest 0.5 step
        return round(clamped * 2.0) / 2.0
    }

    /**
     * Calculates Reps In Reserve (RIR) based on RPE (10 - RPE).
     * E.g. RPE 8.0 -> 2 RIR, RPE 9.5 -> 0.5 RIR, RPE 10.0 -> 0 RIR.
     */
    fun calculateRir(rpe: Double?): Double? {
        if (rpe == null) return null
        val clamped = rpe.coerceIn(MIN_RPE, MAX_RPE)
        val rir = 10.0 - clamped
        return round(rir * 2.0) / 2.0
    }

    /**
     * Formats RIR label for quick UI badges.
     */
    fun formatRirLabel(rpe: Double?): String? {
        val rir = calculateRir(rpe) ?: return null
        return if (rir == 0.0) "Failure (0 RIR)" else "$rir RIR"
    }
}
