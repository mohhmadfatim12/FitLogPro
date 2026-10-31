package com.fitlogpro.app.domain.calculator

import com.fitlogpro.app.domain.model.PRType
import com.fitlogpro.app.domain.model.SetType

data class ExistingExercisePrs(
    val exerciseId: Long,
    val heaviestWeightKg: Double = 0.0,
    val bestEstimated1RmKg: Double = 0.0,
    val bestSetVolumeKg: Double = 0.0,
    val mostReps: Int = 0
)

object PersonalRecordDetector {

    /**
     * Evaluates a completed set against existing records.
     * CRITICAL RULE: Warm-up sets must NEVER create PRs.
     */
    fun detectNewPrs(
        weightKg: Double,
        reps: Int,
        setType: SetType,
        existingPrs: ExistingExercisePrs?
    ): List<PRType> {
        // Strict guard: Warm-up sets NEVER qualify for PRs
        if (!setType.countsForPr) return emptyList()
        if (weightKg <= 0.0 || reps <= 0) return emptyList()

        val brokenPrs = mutableListOf<PRType>()
        val current1Rm = OneRepMaxCalculator.calculateEpley(weightKg, reps)
        val currentSetVolume = VolumeCalculator.calculateSetVolume(weightKg, reps, setType)

        val previous = existingPrs ?: ExistingExercisePrs(exerciseId = 0L)

        // 1. Heaviest Weight
        if (weightKg > previous.heaviestWeightKg) {
            brokenPrs.add(PRType.HEAVIEST_WEIGHT)
        }

        // 2. Best Estimated 1RM
        if (current1Rm > previous.bestEstimated1RmKg) {
            brokenPrs.add(PRType.BEST_1RM)
        }

        // 3. Best Set Volume
        if (currentSetVolume > previous.bestSetVolumeKg) {
            brokenPrs.add(PRType.BEST_SET_VOLUME)
        }

        // 4. Most Reps
        if (reps > previous.mostReps) {
            brokenPrs.add(PRType.MOST_REPS)
        }

        return brokenPrs
    }

    fun detectPersonalRecords(
        weightKg: Double,
        reps: Int,
        isWarmup: Boolean,
        prevMaxWeight: Double,
        prev1Rm: Double,
        prevMaxVol: Double,
        prevMaxReps: Int
    ): List<PRType> {
        val setType = if (isWarmup) SetType.WARMUP else SetType.NORMAL
        val existing = ExistingExercisePrs(
            exerciseId = 0L,
            heaviestWeightKg = prevMaxWeight,
            bestEstimated1RmKg = prev1Rm,
            bestSetVolumeKg = prevMaxVol,
            mostReps = prevMaxReps
        )
        return detectNewPrs(weightKg, reps, setType, existing)
    }
}
