package com.fitlogpro.app.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Test

class CardioCalculationsTest {

    @Test
    fun calculateSpeedKmH_accurateForTypicalRunningPaces() {
        // 10 km in 3600s (1 hour) = 10.0 km/h
        assertEquals(10.0, CardioCalculator.calculateSpeedKmH(10.0, 3600), 0.01)

        // 5 km in 1800s (30 mins) = 10.0 km/h
        assertEquals(10.0, CardioCalculator.calculateSpeedKmH(5.0, 1800), 0.01)

        // 5 km in 1500s (25 mins) = 12.0 km/h
        assertEquals(12.0, CardioCalculator.calculateSpeedKmH(5.0, 1500), 0.01)

        // Edge cases
        assertEquals(0.0, CardioCalculator.calculateSpeedKmH(0.0, 3600), 0.01)
        assertEquals(0.0, CardioCalculator.calculateSpeedKmH(5.0, 0), 0.01)
        assertEquals(0.0, CardioCalculator.calculateSpeedKmH(-5.0, 1800), 0.01)
    }

    @Test
    fun calculatePaceSecondsPerKm_accurateFor5MinKm() {
        // 5 km in 1500s -> 300s / km (5:00 min/km)
        assertEquals(300L, CardioCalculator.calculatePaceSecondsPerKm(5.0, 1500))

        // 10 km in 3300s -> 330s / km (5:30 min/km)
        assertEquals(330L, CardioCalculator.calculatePaceSecondsPerKm(10.0, 3300))
    }

    @Test
    fun formatPace_formatsReadableString() {
        // 5 km in 1500s = 5:00 /km
        assertEquals("5:00 /km", CardioCalculator.formatPace(5.0, 1500, isMetric = true))

        // 10 km in 3300s = 5:30 /km
        assertEquals("5:30 /km", CardioCalculator.formatPace(10.0, 3300, isMetric = true))

        // Zero distance fallback
        assertEquals("--:-- /km", CardioCalculator.formatPace(0.0, 1800, isMetric = true))
    }

    @Test
    fun formatDuration_handlesMinutesAndHours() {
        assertEquals("00:00", CardioCalculator.formatDuration(0))
        assertEquals("00:45", CardioCalculator.formatDuration(45))
        assertEquals("05:30", CardioCalculator.formatDuration(330))
        assertEquals("1:05:00", CardioCalculator.formatDuration(3900))
        assertEquals("2:15:30", CardioCalculator.formatDuration(8130))
    }
}
