package com.fitlogpro.app.domain.calculator

import com.fitlogpro.app.domain.model.PRType
import com.fitlogpro.app.domain.model.SetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalRecordDetectorTest {

    @Test
    fun detectNewPrs_warmupSet_neverTriggersPr() {
        val existing = ExistingExercisePrs(
            exerciseId = 1L,
            heaviestWeightKg = 50.0,
            bestEstimated1RmKg = 60.0,
            bestSetVolumeKg = 500.0,
            mostReps = 10
        )

        // Lifting 150 kg for 20 reps on a WARMUP set should NEVER trigger any PR
        val prs = PersonalRecordDetector.detectNewPrs(
            weightKg = 150.0,
            reps = 20,
            setType = SetType.WARMUP,
            existingPrs = existing
        )

        assertTrue("Warm-up sets must not generate PRs", prs.isEmpty())
    }

    @Test
    fun detectNewPrs_firstTimeExercise_detectsAllCategories() {
        val prs = PersonalRecordDetector.detectNewPrs(
            weightKg = 80.0,
            reps = 8,
            setType = SetType.NORMAL,
            existingPrs = null
        )

        assertEquals(4, prs.size)
        assertTrue(prs.contains(PRType.HEAVIEST_WEIGHT))
        assertTrue(prs.contains(PRType.BEST_1RM))
        assertTrue(prs.contains(PRType.BEST_SET_VOLUME))
        assertTrue(prs.contains(PRType.MOST_REPS))
    }

    @Test
    fun detectNewPrs_higherWeightLowerReps_detectsHeaviestAnd1RmOnly() {
        val existing = ExistingExercisePrs(
            exerciseId = 1L,
            heaviestWeightKg = 100.0,
            bestEstimated1RmKg = 120.0,
            bestSetVolumeKg = 1000.0, // 100 * 10
            mostReps = 15
        )

        // Lift 115 kg for 4 reps: 1RM = 115 * (1 + 4/30) = 130.3 kg, Vol = 460 kg
        val prs = PersonalRecordDetector.detectNewPrs(
            weightKg = 115.0,
            reps = 4,
            setType = SetType.NORMAL,
            existingPrs = existing
        )

        assertTrue(prs.contains(PRType.HEAVIEST_WEIGHT))
        assertTrue(prs.contains(PRType.BEST_1RM))
        assertFalse(prs.contains(PRType.BEST_SET_VOLUME))
        assertFalse(prs.contains(PRType.MOST_REPS))
    }

    @Test
    fun detectNewPrs_highRepBurnout_detectsMostRepsAndVolume() {
        val existing = ExistingExercisePrs(
            exerciseId = 1L,
            heaviestWeightKg = 100.0,
            bestEstimated1RmKg = 125.0,
            bestSetVolumeKg = 800.0,
            mostReps = 10
        )

        // Drop set with 60 kg for 20 reps: Vol = 1200, Reps = 20
        val prs = PersonalRecordDetector.detectNewPrs(
            weightKg = 60.0,
            reps = 20,
            setType = SetType.DROP_SET,
            existingPrs = existing
        )

        assertTrue(prs.contains(PRType.BEST_SET_VOLUME))
        assertTrue(prs.contains(PRType.MOST_REPS))
        assertFalse(prs.contains(PRType.HEAVIEST_WEIGHT))
    }
}
