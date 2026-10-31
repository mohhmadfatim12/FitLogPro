package com.fitlogpro.app.domain.model

enum class ExerciseType(val displayName: String) {
    WEIGHT_AND_REPS("Weight & Reps"),
    BODYWEIGHT_REPS("Bodyweight Reps"),
    WEIGHTED_BODYWEIGHT("Weighted Bodyweight"),
    ASSISTED_BODYWEIGHT("Assisted Bodyweight"),
    CARDIO("Cardio (Duration & Distance)"),
    REPS_ONLY("Reps Only");

    companion object {
        fun fromString(value: String): ExerciseType {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: WEIGHT_AND_REPS
        }
    }
}
