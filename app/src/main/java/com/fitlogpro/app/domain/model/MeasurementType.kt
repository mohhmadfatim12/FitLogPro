package com.fitlogpro.app.domain.model

enum class MeasurementType(
    val displayName: String,
    val defaultUnitMetric: String,
    val defaultUnitImperial: String
) {
    WEIGHT("Body Weight", "kg", "lbs"),
    BODY_FAT("Body Fat", "%", "%"),
    CHEST("Chest", "cm", "in"),
    WAIST("Waist", "cm", "in"),
    ARMS("Arms", "cm", "in"),
    THIGHS("Thighs", "cm", "in"),
    CALVES("Calves", "cm", "in"),
    SHOULDERS("Shoulders", "cm", "in"),
    HIPS("Hips", "cm", "in"),
    NECK("Neck", "cm", "in");

    fun getUnit(isMetric: Boolean): String = if (isMetric) defaultUnitMetric else defaultUnitImperial

    companion object {
        fun fromString(value: String): MeasurementType {
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true)
            } ?: WEIGHT
        }
    }
}
