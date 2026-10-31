package com.fitlogpro.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_workout_sessions")
data class ActiveWorkoutSessionEntity(
    @PrimaryKey
    val sessionId: String, // Constant single active session ID e.g. "CURRENT_ACTIVE_SESSION"
    val routineId: Long? = null,
    val workoutName: String,
    val startTimeEpochMs: Long,
    val elapsedSeconds: Long,
    val isPaused: Boolean,
    val lastPauseTimestampMs: Long? = null,
    val sessionJson: String, // Full serialized snapshot of ActiveWorkoutSession
    val lastUpdatedEpochMs: Long = System.currentTimeMillis()
)
