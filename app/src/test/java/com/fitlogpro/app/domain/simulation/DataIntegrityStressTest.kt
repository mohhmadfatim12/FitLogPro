package com.fitlogpro.app.domain.simulation

import com.fitlogpro.app.domain.calculator.OneRepMaxCalculator
import com.fitlogpro.app.domain.calculator.VolumeCalculator
import com.fitlogpro.app.domain.model.LiveWorkoutSet
import com.fitlogpro.app.domain.model.SetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataIntegrityStressTest {

    @Test
    fun highVolumeWorkout_processesAccuratelyWithoutOverflowOrSlowdown() {
        val sets = mutableListOf<LiveWorkoutSet>()
        var expectedVolume = 0.0

        // Generate 100 sets across a high-volume powerlifting cycle
        for (i in 1..100) {
            val isWarmup = (i % 5 == 1) // 1 in 5 is warmup
            val setType = if (isWarmup) SetType.WARMUP else SetType.NORMAL
            val weight = 100.0 + (i % 20) * 5.0 // 100kg to 195kg
            val reps = 5 + (i % 6)               // 5 to 10 reps

            if (!isWarmup) {
                expectedVolume += (weight * reps)
            }

            sets.add(
                LiveWorkoutSet(
                    id = "set-$i",
                    setNumber = i,
                    setType = setType,
                    weightKg = weight,
                    reps = reps,
                    isCompleted = true
                )
            )
        }

        val startTime = System.currentTimeMillis()
        val calculatedVolume = VolumeCalculator.calculateTotalWorkoutVolume(sets)
        val elapsed = System.currentTimeMillis() - startTime

        // Verification of volume calculation accuracy
        assertEquals(expectedVolume, calculatedVolume, 0.01)
        // Processing 100 sets takes less than 50 milliseconds
        assertTrue("Calculation must complete in under 50ms, took ${elapsed}ms", elapsed < 50)
    }

    @Test
    fun extremeHeavyWeights_handlesWithoutOverflow() {
        // Strongman / world record lifts (e.g. 500kg deadlift)
        val epley1Rm = OneRepMaxCalculator.calculateEpley(501.0, 1)
        assertEquals(501.0, epley1Rm, 0.01)

        val setVolume = VolumeCalculator.calculateSetVolume(500.0, 3, SetType.NORMAL)
        assertEquals(1500.0, setVolume, 0.01)
    }
}
