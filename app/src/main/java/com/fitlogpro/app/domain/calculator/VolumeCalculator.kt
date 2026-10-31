package com.fitlogpro.app.domain.calculator

import com.fitlogpro.app.domain.model.LiveWorkoutSet
import com.fitlogpro.app.domain.model.SetType
import kotlin.math.round

object VolumeCalculator {

    /**
     * Calculates volume for a single set: weight * reps.
     * Crucial rule: Warm-up sets MUST NOT count toward workout volume.
     */
    fun calculateSetVolume(weight: Double, reps: Int, setType: SetType): Double {
        if (!setType.countsForVolume) return 0.0
        if (weight <= 0.0 || reps <= 0) return 0.0
        return roundToDecimals(weight * reps, 1)
    }

    /**
     * Calculates total workout volume summing all completed non-warmup sets.
     */
    fun calculateTotalWorkoutVolume(sets: List<LiveWorkoutSet>): Double {
        val total = sets
            .filter { it.isCompleted && it.setType.countsForVolume }
            .sumOf { calculateSetVolume(it.weightKg, it.reps, it.setType) }
        return roundToDecimals(total, 1)
    }

    /**
     * Calculates volume from raw values.
     */
    fun calculateRawVolume(items: List<Triple<Double, Int, SetType>>): Double {
        val total = items
            .filter { it.third.countsForVolume }
            .sumOf { (weight, reps, setType) -> calculateSetVolume(weight, reps, setType) }
        return roundToDecimals(total, 1)
    }

    private fun roundToDecimals(value: Double, decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10.0 }
        return round(value * multiplier) / multiplier
    }
}
