package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.WorkoutDao
import com.fitlogpro.app.domain.calculator.OneRepMaxCalculator
import com.fitlogpro.app.domain.calculator.StreakCalculator
import com.fitlogpro.app.domain.calculator.VolumeCalculator
import com.fitlogpro.app.domain.model.CalendarWorkoutDay
import com.fitlogpro.app.domain.model.ExerciseProgressPoint
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.MuscleGroupDistribution
import com.fitlogpro.app.domain.model.VolumeHistoryPoint
import com.fitlogpro.app.domain.model.WorkoutStreakInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalyticsRepository(private val workoutDao: WorkoutDao) {

    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.US)
    private val weekDateFormat = SimpleDateFormat("'Wk' w, yyyy", Locale.US)

    suspend fun getExerciseProgress(exerciseId: Long): List<ExerciseProgressPoint> {
        val rows = workoutDao.getExercisePerformanceHistory(exerciseId)
        if (rows.isEmpty()) return emptyList()

        // Group rows by workout startTimeEpochMs
        val groupedByWorkout = rows.groupBy { it.startTimeEpochMs }

        return groupedByWorkout.map { (timestampMs, setRows) ->
            val nonWarmupRows = setRows.filter { it.setType.countsForPr }
            val candidateRows = if (nonWarmupRows.isNotEmpty()) nonWarmupRows else setRows

            val maxWeight = candidateRows.maxOfOrNull { it.weightKg } ?: 0.0
            val best1Rm = candidateRows.maxOfOrNull { OneRepMaxCalculator.calculateEpley(it.weightKg, it.reps) } ?: 0.0
            val volume = setRows.filter { it.setType.countsForVolume }.sumOf { VolumeCalculator.calculateSetVolume(it.weightKg, it.reps, it.setType) }
            val maxReps = candidateRows.maxOfOrNull { it.reps } ?: 0

            ExerciseProgressPoint(
                timestampMs = timestampMs,
                dateLabel = shortDateFormat.format(timestampMs),
                maxWeightKg = maxWeight,
                estimated1RmKg = best1Rm,
                totalVolumeKg = volume,
                maxReps = maxReps
            )
        }.sortedBy { it.timestampMs }
    }

    fun getWeeklyVolumeHistory(): Flow<List<VolumeHistoryPoint>> = workoutDao.getAllWorkouts().map { workouts ->
        if (workouts.isEmpty()) return@map emptyList()

        val cal = Calendar.getInstance()
        val grouped = workouts.groupBy { w ->
            cal.timeInMillis = w.startTimeEpochMs
            val year = cal.get(Calendar.YEAR)
            val week = cal.get(Calendar.WEEK_OF_YEAR)
            "$year-$week"
        }

        grouped.map { (_, list) ->
            val firstTs = list.first().startTimeEpochMs
            val totalVol = list.sumOf { it.totalVolumeKg }
            VolumeHistoryPoint(
                periodLabel = weekDateFormat.format(firstTs),
                timestampMs = firstTs,
                totalVolumeKg = totalVol,
                workoutCount = list.size
            )
        }.sortedBy { it.timestampMs }
    }

    fun getMuscleGroupDistribution(): Flow<List<MuscleGroupDistribution>> = workoutDao.getAllWorkoutsWithDetails().map { workouts ->
        if (workouts.isEmpty()) return@map emptyList()

        val countMap = mutableMapOf<MuscleGroup, Int>()
        val volumeMap = mutableMapOf<MuscleGroup, Double>()

        workouts.forEach { w ->
            w.exercises.forEach { we ->
                val group = we.exercise.muscleGroup
                val completedSets = we.sets.filter { it.isCompleted }
                val setsCount = completedSets.size
                val vol = completedSets.filter { it.setType.countsForVolume }
                    .sumOf { VolumeCalculator.calculateSetVolume(it.weightKg, it.reps, it.setType) }

                countMap[group] = (countMap[group] ?: 0) + setsCount
                volumeMap[group] = (volumeMap[group] ?: 0.0) + vol
            }
        }

        val totalSetsAll = countMap.values.sum()
        if (totalSetsAll == 0) return@map emptyList()

        countMap.map { (group, setsCount) ->
            val percentage = (setsCount.toFloat() / totalSetsAll.toFloat()) * 100f
            MuscleGroupDistribution(
                muscleGroup = group,
                percentage = percentage,
                totalSets = setsCount,
                totalVolumeKg = volumeMap[group] ?: 0.0
            )
        }.sortedByDescending { it.totalSets }
    }

    fun getWorkoutStreakInfo(): Flow<WorkoutStreakInfo> = workoutDao.getAllWorkoutTimestampsFlow().map { timestamps ->
        val streakResult = StreakCalculator.calculateStreaks(timestamps)
        val totalVolume = workoutDao.getTotalVolumeAllTime()
        val totalDuration = workoutDao.getTotalDurationAllTime()
        val totalWorkouts = workoutDao.getWorkoutCount()

        WorkoutStreakInfo(
            currentStreakWeeks = streakResult.currentWeeklyStreak,
            bestStreakWeeks = streakResult.bestWeeklyStreak,
            workoutsThisWeek = streakResult.workoutsThisWeek,
            totalWorkoutsAllTime = totalWorkouts,
            totalVolumeKgAllTime = totalVolume,
            totalDurationSecondsAllTime = totalDuration
        )
    }

    fun getCalendarDays(year: Int, month: Int): Flow<List<CalendarWorkoutDay>> = workoutDao.getAllWorkouts().map { workouts ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val days = mutableListOf<CalendarWorkoutDay>()

        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val dayEnd = cal.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val matchingWorkouts = workouts.filter { it.startTimeEpochMs in dayStart..dayEnd }
            days.add(
                CalendarWorkoutDay(
                    epochDay = dayStart / (1000L * 60 * 60 * 24),
                    dayOfMonth = day,
                    hasWorkout = matchingWorkouts.isNotEmpty(),
                    workoutTitles = matchingWorkouts.map { it.name }
                )
            )
        }
        days
    }
}
