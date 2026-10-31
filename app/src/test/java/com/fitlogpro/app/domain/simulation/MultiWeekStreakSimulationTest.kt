package com.fitlogpro.app.domain.simulation

import com.fitlogpro.app.domain.calculator.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class MultiWeekStreakSimulationTest {

    @Test
    fun simulate60DayUserWorkoutPattern_streakCalculationsAreAccurate() {
        val timestamps = mutableListOf<Long>()
        val cal = Calendar.getInstance()

        // 6 consecutive weeks of 3 workouts per week (Mon, Wed, Fri)
        // Set anchor to 8 weeks ago
        cal.add(Calendar.WEEK_OF_YEAR, -8)

        for (week in 0 until 6) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            timestamps.add(cal.timeInMillis)

            cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
            timestamps.add(cal.timeInMillis)

            cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            timestamps.add(cal.timeInMillis)

            cal.add(Calendar.WEEK_OF_YEAR, 1)
        }

        // Total 18 workouts over 6 weeks
        assertEquals(18, timestamps.size)

        // Evaluate historical streak
        val initialStreak = StreakCalculator.calculateStreaks(timestamps)
        assertEquals(6, initialStreak.bestWeeklyStreak)

        // Simulate 1 gap week where user was sick (week -2)
        cal.add(Calendar.WEEK_OF_YEAR, 1)

        // Then user trains in the current week (Mon and Wed)
        val currentCal = Calendar.getInstance()
        currentCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        timestamps.add(currentCal.timeInMillis)

        val updatedStreak = StreakCalculator.calculateStreaks(timestamps)

        // Best streak remains 6 weeks
        assertEquals(6, updatedStreak.bestWeeklyStreak)
        // Current streak restarts from 1
        assertEquals(1, updatedStreak.currentWeeklyStreak)
        assertEquals(1, updatedStreak.workoutsThisWeek)
    }
}
