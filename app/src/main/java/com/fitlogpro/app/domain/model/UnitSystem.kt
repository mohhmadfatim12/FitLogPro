package com.fitlogpro.app.domain.model

enum class WeightUnit(val symbol: String, val displayName: String) {
    KG("kg", "Kilograms"),
    LBS("lbs", "Pounds");

    companion object {
        fun fromString(value: String): WeightUnit {
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.symbol.equals(value, ignoreCase = true)
            } ?: KG
        }
    }
}

enum class DistanceUnit(val symbol: String, val displayName: String) {
    KM("km", "Kilometers"),
    MILES("mi", "Miles");

    companion object {
        fun fromString(value: String): DistanceUnit {
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.symbol.equals(value, ignoreCase = true)
            } ?: KM
        }
    }
}
