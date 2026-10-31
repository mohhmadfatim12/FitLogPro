package com.fitlogpro.app.domain.model

data class LiveWorkoutSet(
    val id: String,
    val setNumber: Int,
    val setType: SetType = SetType.NORMAL,
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val rpe: Double? = null,
    val durationSeconds: Int? = null,
    val distanceKm: Double? = null,
    val isCompleted: Boolean = false,
    val isPr: Boolean = false,
    val prTypes: List<PRType> = emptyList(),
    val previousPerformance: String? = null
)

data class LiveWorkoutExercise(
    val id: String,
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: MuscleGroup,
    val equipment: Equipment,
    val exerciseType: ExerciseType,
    val defaultRestSeconds: Int = 90,
    val supersetGroupId: String? = null,
    val supersetLabel: String? = null, // e.g., "A1", "A2"
    val notes: String = "",
    val sets: List<LiveWorkoutSet> = emptyList()
)

data class ActiveWorkoutSession(
    val sessionId: String,
    val routineId: Long? = null,
    val workoutName: String,
    val startTimeEpochMs: Long,
    val elapsedSeconds: Long = 0,
    val isPaused: Boolean = false,
    val lastPauseTimestampMs: Long? = null,
    val exercises: List<LiveWorkoutExercise> = emptyList(),
    val workoutNotes: String = ""
)
