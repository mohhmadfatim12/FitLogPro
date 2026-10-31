package com.fitlogpro.app.domain.model

data class ExerciseProgressPoint(
    val timestampMs: Long,
    val dateLabel: String,
    val maxWeightKg: Double,
    val estimated1RmKg: Double,
    val totalVolumeKg: Double,
    val maxReps: Int
)

data class VolumeHistoryPoint(
    val periodLabel: String, // e.g. "Week 42" or "Oct 2026"
    val timestampMs: Long,
    val totalVolumeKg: Double,
    val workoutCount: Int
)

data class MuscleGroupDistribution(
    val muscleGroup: MuscleGroup,
    val percentage: Float,
    val totalSets: Int,
    val totalVolumeKg: Double
)

data class WorkoutStreakInfo(
    val currentStreakWeeks: Int,
    val bestStreakWeeks: Int,
    val workoutsThisWeek: Int,
    val totalWorkoutsAllTime: Int,
    val totalVolumeKgAllTime: Double,
    val totalDurationSecondsAllTime: Long
)

data class CalendarWorkoutDay(
    val epochDay: Long,
    val dayOfMonth: Int,
    val hasWorkout: Boolean,
    val workoutTitles: List<String> = emptyList()
)
