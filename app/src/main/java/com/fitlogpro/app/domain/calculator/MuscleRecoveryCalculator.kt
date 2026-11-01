package com.fitlogpro.app.domain.calculator

import com.fitlogpro.app.data.local.entity.WorkoutWithDetails
import com.fitlogpro.app.domain.model.DailyReadiness
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.MuscleRecoveryStatus
import com.fitlogpro.app.domain.model.RecoveryState
import com.fitlogpro.app.domain.model.SetType
import kotlin.math.roundToInt

object MuscleRecoveryCalculator {

    private const val MS_PER_HOUR = 3600000L
    private const val SEVEN_DAYS_MS = 7L * 24L * MS_PER_HOUR

    fun getBaseRecoveryHours(muscleGroup: MuscleGroup): Int {
        return when (muscleGroup) {
            MuscleGroup.LEGS -> 72
            MuscleGroup.BACK -> 72
            MuscleGroup.CHEST -> 60
            MuscleGroup.SHOULDERS -> 48
            MuscleGroup.ARMS -> 36
            MuscleGroup.CORE -> 36
            MuscleGroup.OTHER, MuscleGroup.FULL_BODY -> 48
        }
    }

    fun calculateMuscleRecovery(
        workouts: List<WorkoutWithDetails>,
        nowEpochMs: Long = System.currentTimeMillis()
    ): List<MuscleRecoveryStatus> {
        val cutoffMs = nowEpochMs - SEVEN_DAYS_MS
        val allGroups = listOf(
            MuscleGroup.CHEST,
            MuscleGroup.BACK,
            MuscleGroup.LEGS,
            MuscleGroup.SHOULDERS,
            MuscleGroup.ARMS,
            MuscleGroup.CORE
        )

        return allGroups.map { group ->
            var lastTrainedMs: Long? = null
            var recentSetsCount = 0

            workouts.forEach { w ->
                val workoutStart = w.workout.startTimeEpochMs
                if (workoutStart >= cutoffMs && workoutStart <= nowEpochMs) {
                    val matchingExercises = w.exercises.filter { it.exercise.muscleGroup == group }
                    matchingExercises.forEach { we ->
                        val completedWorkingSets = we.sets.count { it.isCompleted && it.setType != SetType.WARMUP }
                        if (completedWorkingSets > 0) {
                            recentSetsCount += completedWorkingSets
                            if (lastTrainedMs == null || workoutStart > lastTrainedMs!!) {
                                lastTrainedMs = workoutStart
                            }
                        }
                    }
                }
            }

            if (lastTrainedMs == null || recentSetsCount == 0) {
                MuscleRecoveryStatus(
                    muscleGroup = group,
                    recoveryPercentage = 100,
                    state = RecoveryState.FRESH,
                    hoursSinceLastTrained = null,
                    recentSetsCount = 0,
                    recommendation = "Fully rested and primed for heavy progressive overload."
                )
            } else {
                val hoursElapsed = ((nowEpochMs - lastTrainedMs!!) / MS_PER_HOUR).coerceAtLeast(0L)
                val baseHours = getBaseRecoveryHours(group)
                val volumeExtraHours = when {
                    recentSetsCount >= 16 -> 24
                    recentSetsCount >= 10 -> 12
                    else -> 0
                }
                val totalRequiredHours = baseHours + volumeExtraHours
                val fraction = (hoursElapsed.toDouble() / totalRequiredHours.toDouble()).coerceIn(0.0, 1.0)
                val percentage = (fraction * 100.0).roundToInt().coerceIn(0, 100)

                val state = when {
                    percentage < 35 -> RecoveryState.DEEP_FATIGUE
                    percentage < 75 -> RecoveryState.RECOVERING
                    else -> RecoveryState.FRESH
                }

                val recommendation = when (state) {
                    RecoveryState.DEEP_FATIGUE -> "High muscle damage in progress. Rest or light mobility recommended."
                    RecoveryState.RECOVERING -> "Muscle tissue rebuilding. Light accessory work or moderate volume acceptable."
                    RecoveryState.FRESH -> "Supercompensated and fresh! Optimal state for heavy sets or PR attempts."
                }

                MuscleRecoveryStatus(
                    muscleGroup = group,
                    recoveryPercentage = percentage,
                    state = state,
                    hoursSinceLastTrained = hoursElapsed,
                    recentSetsCount = recentSetsCount,
                    recommendation = recommendation
                )
            }
        }
    }

    fun calculateDailyReadiness(
        muscleStatuses: List<MuscleRecoveryStatus>,
        workouts: List<WorkoutWithDetails>,
        nowEpochMs: Long = System.currentTimeMillis()
    ): DailyReadiness {
        if (muscleStatuses.isEmpty()) {
            return DailyReadiness(
                score = 95,
                headline = "Peak Readiness",
                recommendation = "No accumulated muscular fatigue detected. Prime condition to crush your next session!",
                fatiguedMuscleCount = 0,
                recoveringMuscleCount = 0,
                freshMuscleCount = 6
            )
        }

        val fatiguedCount = muscleStatuses.count { it.state == RecoveryState.DEEP_FATIGUE }
        val recoveringCount = muscleStatuses.count { it.state == RecoveryState.RECOVERING }
        val freshCount = muscleStatuses.count { it.state == RecoveryState.FRESH }

        // Raw average from muscle status
        val averageMusclePct = muscleStatuses.map { it.recoveryPercentage }.average()

        // Check if workout was performed within the last 12 hours
        val lastWorkoutMs = workouts.maxOfOrNull { it.workout.startTimeEpochMs }
        val hoursSinceLastWorkout = lastWorkoutMs?.let { (nowEpochMs - it) / MS_PER_HOUR } ?: 999L

        val cnsPenalty = if (hoursSinceLastWorkout < 12L) 15 else 0
        val finalScore = (averageMusclePct - cnsPenalty).roundToInt().coerceIn(30, 99)

        val (headline, recommendation) = when {
            finalScore >= 85 -> Pair(
                "Optimal Readiness",
                "Central nervous system and major muscle groups are restored. You are in a peak window for PR attempts or progressive overload."
            )
            finalScore >= 65 -> Pair(
                "Moderate Readiness",
                "Good working capacity. Execute planned movements with standard RPE (7-8), focusing on mind-muscle connection."
            )
            else -> Pair(
                "Fatigued / Rest Advised",
                "Significant fatigue accumulated across multiple muscle groups. Consider an active recovery day or reducing working sets by 25%."
            )
        }

        return DailyReadiness(
            score = finalScore,
            headline = headline,
            recommendation = recommendation,
            fatiguedMuscleCount = fatiguedCount,
            recoveringMuscleCount = recoveringCount,
            freshMuscleCount = freshCount
        )
    }
}
