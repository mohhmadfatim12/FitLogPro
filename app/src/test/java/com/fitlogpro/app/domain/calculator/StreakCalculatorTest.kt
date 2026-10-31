package com.fitlogpro.app.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class StreakCalculatorTest {

    @Test
    fun calculateStreaks_emptyHistory_returnsZeroes() {
        val result = StreakCalculator.calculateStreaks(emptyList())
        assertEquals(0, result.currentWeeklyStreak)
        assertEquals(0, result.bestWeeklyStreak)
        assertEquals(0, result.workoutsThisWeek)
        assertEquals(0, result.currentDailyStreak)
    }

    @Test
    fun calculateStreaks_singleWorkoutToday_returnsOneStreak() {
        val now = System.currentTimeMillis()
        val result = StreakCalculator.calculateStreaks(listOf(now), now)
        assertEquals(1, result.currentWeeklyStreak)
        assertEquals(1, result.bestWeeklyStreak)
        assertEquals(1, result.workoutsThisWeek)
        assertEquals(1, result.currentDailyStreak)
    }

    @Test
    fun calculateStreaks_threeConsecutiveDays_calculatesDailyStreak() {
        val now = System.currentTimeMillis()
        val oneDayMs = 24L * 60 * 60 * 1000
        val timestamps = listOf(
            now - 2 * oneDayMs,
            now - oneDayMs,
            now
        )
        val result = StreakCalculator.calculateStreaks(timestamps, now)
        assertEquals(3, result.currentDailyStreak)
        assertTrue(result.currentWeeklyStreak >= 1)
    }
}
