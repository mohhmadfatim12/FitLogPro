package com.fitlogpro.app.domain.model

enum class SetType(
    val displayName: String,
    val shortCode: String,
    val isWarmup: Boolean = false,
    val countsForVolume: Boolean = true,
    val countsForPr: Boolean = true
) {
    NORMAL("Normal", "N", isWarmup = false, countsForVolume = true, countsForPr = true),
    WARMUP("Warm-up", "W", isWarmup = true, countsForVolume = false, countsForPr = false),
    DROP_SET("Drop Set", "D", isWarmup = false, countsForVolume = true, countsForPr = true),
    FAILURE("Failure", "F", isWarmup = false, countsForVolume = true, countsForPr = true);

    companion object {
        fun fromString(value: String): SetType {
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.shortCode.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true)
            } ?: NORMAL
        }
    }
}
