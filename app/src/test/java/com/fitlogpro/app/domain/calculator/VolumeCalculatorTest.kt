package com.fitlogpro.app.domain.calculator

import com.fitlogpro.app.domain.model.LiveWorkoutSet
import com.fitlogpro.app.domain.model.SetType
import org.junit.Assert.assertEquals
import org.junit.Test

class VolumeCalculatorTest {

    @Test
    fun calculateSetVolume_normalSet_calculatesAccurately() {
        val volume = VolumeCalculator.calculateSetVolume(80.0, 10, SetType.NORMAL)
        assertEquals(800.0, volume, 0.01)
    }

    @Test
    fun calculateSetVolume_warmupSet_returnsZeroVolume() {
        // Warm-up sets must NOT count toward volume
        val volume = VolumeCalculator.calculateSetVolume(60.0, 12, SetType.WARMUP)
        assertEquals(0.0, volume, 0.001)
    }

    @Test
    fun calculateSetVolume_dropSetAndFailure_countsForVolume() {
        val dropSetVol = VolumeCalculator.calculateSetVolume(70.0, 8, SetType.DROP_SET)
        assertEquals(560.0, dropSetVol, 0.01)

        val failureVol = VolumeCalculator.calculateSetVolume(90.0, 6, SetType.FAILURE)
        assertEquals(540.0, failureVol, 0.01)
    }

    @Test
    fun calculateTotalWorkoutVolume_mixedSets_excludesWarmupsAndIncomplete() {
        val sets = listOf(
            LiveWorkoutSet(id = "1", setNumber = 1, setType = SetType.WARMUP, weightKg = 40.0, reps = 15, isCompleted = true),
            LiveWorkoutSet(id = "2", setNumber = 2, setType = SetType.WARMUP, weightKg = 60.0, reps = 10, isCompleted = true),
            LiveWorkoutSet(id = "3", setNumber = 3, setType = SetType.NORMAL, weightKg = 100.0, reps = 8, isCompleted = true), // 800
            LiveWorkoutSet(id = "4", setNumber = 4, setType = SetType.NORMAL, weightKg = 100.0, reps = 8, isCompleted = true), // 800
            LiveWorkoutSet(id = "5", setNumber = 5, setType = SetType.DROP_SET, weightKg = 75.0, reps = 10, isCompleted = true), // 750
            LiveWorkoutSet(id = "6", setNumber = 6, setType = SetType.NORMAL, weightKg = 100.0, reps = 8, isCompleted = false) // not completed
        )

        val total = VolumeCalculator.calculateTotalWorkoutVolume(sets)
        // 800 + 800 + 750 = 2350
        assertEquals(2350.0, total, 0.01)
    }
}
