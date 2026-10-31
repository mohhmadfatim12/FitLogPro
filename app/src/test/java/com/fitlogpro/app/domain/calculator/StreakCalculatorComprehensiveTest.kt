package com.fitlogpro.app.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class StreakCalculatorComprehensiveTest {

    @Test
    fun calculateStreaks_spanningYearBoundary_calculatesUnbrokenWeeklyStreak() {
        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.DECEMBER, 29) // Last week of 2025
        val ts1 = cal.timeInMillis

        cal.set(2026, Calendar.JANUARY, 5) // First week of 2026
        val ts2 = cal.timeInMillis

        val result = StreakCalculator.calculateStreaks(listOf(ts1, ts2), ts2)
        assertTrue(result.currentWeeklyStreak >= 2)
    }

    @Test
    fun calculateStreaks_withGapWeek_resetsCurrentStreak() {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        val twoWeeksAgo = now - 14L * 24 * 60 * 60 * 1000
        val fourWeeksAgo = now - 28L * 24 * 60 * 60 * 1000

        // Missed last week
        val result = StreakCalculator.calculateStreaks(listOf(fourWeeksAgo, twoWeeksAgo), now)
        assertEquals(0, result.currentWeeklyStreak)
        assertTrue(result.bestWeeklyStreak >= 1)
    }

    @Test
    fun yearWeek_isImmediatelyFollowing_validatesSequentialWeeks() {
        val w1 = StreakCalculator.YearWeek(2026, 10)
        val w2 = StreakCalculator.YearWeek(2026, 11)
        assertTrue(w2.isImmediatelyFollowing(w1))

        val endYear = StreakCalculator.YearWeek(2025, 52)
        val startYear = StreakCalculator.YearWeek(2026, 1)
        assertTrue(startYear.isImmediatelyFollowing(endYear))
    }
}
