package com.fitlogpro.app.domain.calculator

import kotlin.math.round

object CardioCalculator {

    /**
     * Calculates speed in km/h given distance in km and duration in seconds.
     * Returns 0.0 if duration <= 0 or distance <= 0.
     */
    fun calculateSpeedKmH(distanceKm: Double, durationSeconds: Long): Double {
        if (distanceKm <= 0.0 || durationSeconds <= 0L) return 0.0
        val hours = durationSeconds / 3600.0
        val speed = distanceKm / hours
        return round(speed * 10.0) / 10.0
    }

    /**
     * Calculates pace in seconds per kilometer.
     * Returns 0L if distance <= 0.
     */
    fun calculatePaceSecondsPerKm(distanceKm: Double, durationSeconds: Long): Long {
        if (distanceKm <= 0.0 || durationSeconds <= 0L) return 0L
        return (durationSeconds / distanceKm).toLong()
    }

    /**
     * Formats pace as standard runner format: e.g., "5:30 /km"
     */
    fun formatPace(distanceKm: Double, durationSeconds: Long, isMetric: Boolean = true): String {
        if (distanceKm <= 0.0 || durationSeconds <= 0L) return "--:-- /${if (isMetric) "km" else "mi"}"
        val effectiveDist = if (isMetric) distanceKm else UnitConverter.kmToMiles(distanceKm)
        if (effectiveDist <= 0.0) return "--:-- /${if (isMetric) "km" else "mi"}"

        val totalSeconds = (durationSeconds / effectiveDist).toLong()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val unitLabel = if (isMetric) "km" else "mi"
        return String.format("%d:%02d /%s", minutes, seconds, unitLabel)
    }

    /**
     * Formats duration in seconds as HH:MM:SS or MM:SS
     */
    fun formatDuration(durationSeconds: Long): String {
        if (durationSeconds <= 0L) return "00:00"
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
