package com.fitlogpro.app.domain.model

enum class RecoveryState(val displayName: String, val colorHex: Long) {
    DEEP_FATIGUE("Deep Fatigue", 0xFFEF4444), // Red
    RECOVERING("Recovering", 0xFFF59E0B),    // Amber
    FRESH("Fresh & Primed", 0xFF10B981)     // Vibrant Emerald Green
}

data class MuscleRecoveryStatus(
    val muscleGroup: MuscleGroup,
    val recoveryPercentage: Int, // 0..100
    val state: RecoveryState,
    val hoursSinceLastTrained: Long?,
    val recentSetsCount: Int,
    val recommendation: String
)

data class DailyReadiness(
    val score: Int, // 0..100
    val headline: String,
    val recommendation: String,
    val fatiguedMuscleCount: Int,
    val recoveringMuscleCount: Int,
    val freshMuscleCount: Int
)

enum class SorenessLevel(val displayName: String) {
    NONE("None"),
    MILD("Mild"),
    MODERATE("Moderate"),
    SEVERE("Severe")
}

data class JointCheckIn(
    val jointName: String,
    val isTender: Boolean,
    val cautionNote: String
)
