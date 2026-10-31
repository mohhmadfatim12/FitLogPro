package com.fitlogpro.app.domain.calculator

import java.util.Calendar
import java.util.TimeZone

data class StreakResult(
    val currentWeeklyStreak: Int,
    val bestWeeklyStreak: Int,
    val workoutsThisWeek: Int,
    val currentDailyStreak: Int
)

object StreakCalculator {

    /**
     * Calculates weekly and daily streaks from a list of workout completion timestamps (epoch ms).
     */
    fun calculateStreaks(workoutTimestampsMs: List<Long>, nowEpochMs: Long = System.currentTimeMillis()): StreakResult {
        if (workoutTimestampsMs.isEmpty()) {
            return StreakResult(
                currentWeeklyStreak = 0,
                bestWeeklyStreak = 0,
                workoutsThisWeek = 0,
                currentDailyStreak = 0
            )
        }

        val tz = TimeZone.getDefault()
        val calendar = Calendar.getInstance(tz)

        // Convert timestamps to unique epoch days (days since Jan 1, 1970)
        val epochDays = workoutTimestampsMs.map { toEpochDay(it, tz) }.toSortedSet()
        val currentEpochDay = toEpochDay(nowEpochMs, tz)

        // Count workouts this current calendar week
        calendar.timeInMillis = nowEpochMs
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        var thisWeekCount = 0
        workoutTimestampsMs.forEach { ts ->
            calendar.timeInMillis = ts
            if (calendar.get(Calendar.YEAR) == currentYear && calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                thisWeekCount++
            }
        }

        // Daily streak calculation (consecutive days ending yesterday or today)
        var dailyStreak = 0
        var checkDay = currentEpochDay
        if (!epochDays.contains(checkDay)) {
            // If haven't worked out today, check if worked out yesterday to maintain active streak
            checkDay -= 1
        }
        while (epochDays.contains(checkDay)) {
            dailyStreak++
            checkDay--
        }

        // Weekly streak calculation (weeks with at least one workout)
        // Group timestamps by year-week key
        val activeYearWeeks = workoutTimestampsMs.map { ts ->
            calendar.timeInMillis = ts
            YearWeek(calendar.get(Calendar.YEAR), calendar.get(Calendar.WEEK_OF_YEAR))
        }.toSortedSet()

        val currentYearWeek = YearWeek(currentYear, currentWeek)
        var weeklyStreak = 0
        var evalYW = currentYearWeek

        // If no workout yet this current week, check last week
        if (!activeYearWeeks.contains(evalYW)) {
            evalYW = evalYW.previousWeek()
        }

        while (activeYearWeeks.contains(evalYW)) {
            weeklyStreak++
            evalYW = evalYW.previousWeek()
        }

        // Best weekly streak
        var bestStreak = 0
        var runningStreak = 0
        var prevYW: YearWeek? = null
        for (yw in activeYearWeeks) {
            if (prevYW == null || yw.isImmediatelyFollowing(prevYW)) {
                runningStreak++
            } else {
                runningStreak = 1
            }
            if (runningStreak > bestStreak) {
                bestStreak = runningStreak
            }
            prevYW = yw
        }

        if (weeklyStreak > bestStreak) {
            bestStreak = weeklyStreak
        }

        return StreakResult(
            currentWeeklyStreak = weeklyStreak,
            bestWeeklyStreak = bestStreak,
            workoutsThisWeek = thisWeekCount,
            currentDailyStreak = dailyStreak
        )
    }

    private fun toEpochDay(epochMs: Long, tz: TimeZone): Long {
        val offset = tz.getOffset(epochMs)
        return (epochMs + offset) / (1000L * 60 * 60 * 24)
    }

    data class YearWeek(val year: Int, val week: Int) : Comparable<YearWeek> {
        override fun compareTo(other: YearWeek): Int {
            return if (this.year != other.year) {
                this.year.compareTo(other.year)
            } else {
                this.week.compareTo(other.week)
            }
        }

        fun previousWeek(): YearWeek {
            return if (week > 1) {
                YearWeek(year, week - 1)
            } else {
                YearWeek(year - 1, 52)
            }
        }

        fun isImmediatelyFollowing(prev: YearWeek): Boolean {
            return (this.year == prev.year && this.week == prev.week + 1) ||
                    (this.year == prev.year + 1 && prev.week >= 52 && this.week == 1)
        }
    }
}
