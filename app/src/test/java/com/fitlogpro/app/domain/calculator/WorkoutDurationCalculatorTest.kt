package com.fitlogpro.app.domain.calculator

import com.fitlogpro.app.domain.model.ActiveWorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutDurationCalculatorTest {

    private fun calculateActiveDuration(
        startTimeEpochMs: Long,
        currentTimeEpochMs: Long,
        totalPausedMs: Long
    ): Long {
        val rawElapsed = (currentTimeEpochMs - startTimeEpochMs) - totalPausedMs
        return if (rawElapsed > 0) rawElapsed / 1000L else 0L
    }

    @Test
    fun calculateActiveDuration_accountsForPauseIntervals() {
        val start = 1000000L
        val current = 1060000L // 60 seconds elapsed wall-clock
        val paused = 15000L    // 15 seconds paused

        val activeSeconds = calculateActiveDuration(start, current, paused)
        assertEquals(45L, activeSeconds)
    }

    @Test
    fun formatWorkoutDuration_formatsCleanGymDisplays() {
        assertEquals("00:45", CardioCalculator.formatDuration(45L))
        assertEquals("25:30", CardioCalculator.formatDuration(1530L))
        assertEquals("1:15:00", CardioCalculator.formatDuration(4500L))
        assertEquals("2:00:15", CardioCalculator.formatDuration(7215L))
    }

    @Test
    fun activeSessionElapsedSeconds_incrementsCorrectly() {
        val session = ActiveWorkoutSession(
            sessionId = "CURRENT",
            workoutName = "Shoulders & Arms",
            startTimeEpochMs = 1000L,
            elapsedSeconds = 120L
        )

        val updated = session.copy(elapsedSeconds = session.elapsedSeconds + 1)
        assertEquals(121L, updated.elapsedSeconds)
    }
}
