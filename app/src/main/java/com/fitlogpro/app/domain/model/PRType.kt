package com.fitlogpro.app.domain.model

enum class PRType(
    val title: String,
    val description: String,
    val unitSuffix: (weightUnit: WeightUnit) -> String
) {
    HEAVIEST_WEIGHT(
        title = "Heaviest Weight",
        description = "Highest absolute weight logged for a completed non-warmup set",
        unitSuffix = { it.symbol }
    ),
    BEST_1RM(
        title = "Best Estimated 1RM",
        description = "Highest estimated one-rep max calculated via Epley formula",
        unitSuffix = { it.symbol }
    ),
    BEST_SET_VOLUME(
        title = "Best Set Volume",
        description = "Highest single-set workload volume (Weight × Reps)",
        unitSuffix = { it.symbol }
    ),
    MOST_REPS(
        title = "Most Reps",
        description = "Highest number of repetitions achieved in a single non-warmup set",
        unitSuffix = { "reps" }
    );

    companion object {
        fun fromString(value: String): PRType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: HEAVIEST_WEIGHT
        }
    }
}
