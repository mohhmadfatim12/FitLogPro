package com.fitlogpro.app.domain.model

enum class Equipment(val displayName: String) {
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    MACHINE("Machine"),
    CABLE("Cable"),
    BODYWEIGHT("Bodyweight"),
    KETTLEBELL("Kettlebell"),
    SMITH_MACHINE("Smith Machine"),
    RESISTANCE_BAND("Resistance Band"),
    CARDIO_MACHINE("Cardio Machine"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): Equipment {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
