package com.fitlogpro.app.domain.simulation

import com.fitlogpro.app.domain.calculator.OneRepMaxCalculator
import com.fitlogpro.app.domain.calculator.PersonalRecordDetector
import com.fitlogpro.app.domain.calculator.VolumeCalculator
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.LiveWorkoutExercise
import com.fitlogpro.app.domain.model.LiveWorkoutSet
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.PRType
import com.fitlogpro.app.domain.model.SetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EndToEndWorkoutFlowSimulationTest {

    @Test
    fun simulateCompleteWorkoutLifecycle_withWarmupsAndPrs() {
        // Step 1: User starts "Leg Power Session"
        val squat = LiveWorkoutExercise(
            id = "ex-squat",
            exerciseId = 6L,
            exerciseName = "Barbell Squat",
            muscleGroup = MuscleGroup.QUADS,
            equipment = Equipment.BARBELL,
            exerciseType = ExerciseType.WEIGHT_AND_REPS,
            defaultRestSeconds = 180
        )

        // Previous historical records for Squat
        var prevMaxWeight = 120.0
        var prev1Rm = 144.0
        var prevMaxVol = 800.0
        var prevMaxReps = 10

        // Step 2: Set 1 — Warm-up set (70kg x 10)
        val set1Warmup = LiveWorkoutSet(
            id = "s-1",
            setNumber = 1,
            setType = SetType.WARMUP,
            weightKg = 70.0,
            reps = 10,
            isCompleted = true
        )
        // Verify warm-up generates 0 PRs and 0 volume
        val set1Prs = PersonalRecordDetector.detectPersonalRecords(
            set1Warmup.weightKg, set1Warmup.reps, set1Warmup.setType.isWarmup,
            prevMaxWeight, prev1Rm, prevMaxVol, prevMaxReps
        )
        assertTrue(set1Prs.isEmpty())
        assertEquals(0.0, VolumeCalculator.calculateSetVolume(set1Warmup.weightKg, set1Warmup.reps, set1Warmup.setType), 0.001)

        // Step 3: Set 2 — Heavy working set (130kg x 5) -> New Heaviest Weight & Best 1RM PR!
        // 130 * (1 + 5/30) = 130 * 1.1666... = 151.7kg 1RM (> 144.0)
        val set2Working = LiveWorkoutSet(
            id = "s-2",
            setNumber = 2,
            setType = SetType.NORMAL,
            weightKg = 130.0,
            reps = 5,
            isCompleted = true
        )
        val set2Prs = PersonalRecordDetector.detectPersonalRecords(
            set2Working.weightKg, set2Working.reps, set2Working.setType.isWarmup,
            prevMaxWeight, prev1Rm, prevMaxVol, prevMaxReps
        )
        assertTrue(set2Prs.contains(PRType.HEAVIEST_WEIGHT))
        assertTrue(set2Prs.contains(PRType.BEST_1RM))

        // Update historical bests
        prevMaxWeight = 130.0
        prev1Rm = OneRepMaxCalculator.calculateEpley(130.0, 5)

        // Step 4: Set 3 — High-rep Drop Set (90kg x 12) -> New Most Reps PR!
        val set3Drop = LiveWorkoutSet(
            id = "s-3",
            setNumber = 3,
            setType = SetType.DROP_SET,
            weightKg = 90.0,
            reps = 12,
            isCompleted = true
        )
        val set3Prs = PersonalRecordDetector.detectPersonalRecords(
            set3Drop.weightKg, set3Drop.reps, set3Drop.setType.isWarmup,
            prevMaxWeight, prev1Rm, prevMaxVol, prevMaxReps
        )
        assertTrue(set3Prs.contains(PRType.MOST_REPS))
        // Set volume = 90 * 12 = 1080 > 800 -> Also a Set Volume PR!
        assertTrue(set3Prs.contains(PRType.BEST_SET_VOLUME))

        // Step 5: End of workout volume summation
        val allSets = listOf(set1Warmup, set2Working, set3Drop)
        val totalVolume = VolumeCalculator.calculateTotalWorkoutVolume(allSets)

        // Warmup (70x10) = 0. Set 2 (130x5) = 650. Set 3 (90x12) = 1080. Total = 1730.0 kg
        assertEquals(1730.0, totalVolume, 0.001)
    }
}
