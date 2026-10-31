package com.fitlogpro.app.domain.calculator

import com.fitlogpro.app.domain.model.LiveWorkoutExercise
import com.fitlogpro.app.domain.model.LiveWorkoutSet
import com.fitlogpro.app.domain.model.PRType
import com.fitlogpro.app.domain.model.SetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutCalculationsComprehensiveTest {

    @Test
    fun multiExerciseWorkout_calculatesAccurateVolumeExcludingWarmups() {
        val warmup1 = LiveWorkoutSet("1", 1, SetType.WARMUP, weightKg = 60.0, reps = 12, isCompleted = true)
        val warmup2 = LiveWorkoutSet("2", 2, SetType.WARMUP, weightKg = 80.0, reps = 8, isCompleted = true)
        val working1 = LiveWorkoutSet("3", 3, SetType.NORMAL, weightKg = 100.0, reps = 6, isCompleted = true) // 600 kg
        val working2 = LiveWorkoutSet("4", 4, SetType.NORMAL, weightKg = 100.0, reps = 6, isCompleted = true) // 600 kg
        val dropSet = LiveWorkoutSet("5", 5, SetType.DROP_SET, weightKg = 70.0, reps = 10, isCompleted = true) // 700 kg
        val failureSet = LiveWorkoutSet("6", 6, SetType.FAILURE, weightKg = 85.0, reps = 5, isCompleted = true) // 425 kg
        val incomplete = LiveWorkoutSet("7", 7, SetType.NORMAL, weightKg = 100.0, reps = 6, isCompleted = false)

        val allSets = listOf(warmup1, warmup2, working1, working2, dropSet, failureSet, incomplete)
        val totalVolume = VolumeCalculator.calculateTotalWorkoutVolume(allSets)

        // 600 + 600 + 700 + 425 = 2325 kg
        assertEquals(2325.0, totalVolume, 0.01)
    }

    @Test
    fun personalRecordDetection_warmupSetIsCompletelyImmuneToPrCreation() {
        val existing = ExistingExercisePrs(
            exerciseId = 99L,
            heaviestWeightKg = 100.0,
            bestEstimated1RmKg = 120.0,
            bestSetVolumeKg = 800.0,
            mostReps = 10
        )

        // Even an astronomical weight or rep count on a WARMUP set must never create a PR
        val prs = PersonalRecordDetector.detectNewPrs(
            weightKg = 500.0,
            reps = 100,
            setType = SetType.WARMUP,
            existingPrs = existing
        )

        assertTrue(prs.isEmpty())
    }

    @Test
    fun epleyOneRepMax_handlesDecimalWeightsAndExtremeReps() {
        // 102.5 kg for 6 reps: 102.5 * (1 + 6/30) = 102.5 * 1.2 = 123.0 kg
        val rm = OneRepMaxCalculator.calculateEpley(102.5, 6)
        assertEquals(123.0, rm, 0.1)

        // 1 rep: exact weight
        val single = OneRepMaxCalculator.calculateEpley(157.5, 1)
        assertEquals(157.5, single, 0.001)

        // 30 reps: weight * 2
        val highRep = OneRepMaxCalculator.calculateEpley(50.0, 30)
        assertEquals(100.0, highRep, 0.1)
    }

    @Test
    fun unitConversions_preservesConsistencyAcrossScale() {
        val weightsKg = listOf(2.5, 20.0, 60.0, 100.0, 142.5, 220.0)
        weightsKg.forEach { kg ->
            val lbs = UnitConverter.kgToLbs(kg)
            val convertedBack = UnitConverter.lbsToKg(lbs)
            assertEquals(kg, convertedBack, 0.2)
        }
    }
}
