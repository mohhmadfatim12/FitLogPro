package com.fitlogpro.app.domain.calculator

import com.fitlogpro.app.domain.model.DistanceUnit
import com.fitlogpro.app.domain.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitSystemFormattingTest {

    @Test
    fun weightUnit_fromString_handlesCaseAndSymbols() {
        assertEquals(WeightUnit.KG, WeightUnit.fromString("kg"))
        assertEquals(WeightUnit.KG, WeightUnit.fromString("KG"))
        assertEquals(WeightUnit.KG, WeightUnit.fromString("Kilograms"))
        assertEquals(WeightUnit.LBS, WeightUnit.fromString("lbs"))
        assertEquals(WeightUnit.LBS, WeightUnit.fromString("LBS"))
        assertEquals(WeightUnit.LBS, WeightUnit.fromString("Pounds"))
        // Fallback default
        assertEquals(WeightUnit.KG, WeightUnit.fromString("unknown_unit"))
    }

    @Test
    fun distanceUnit_fromString_handlesCaseAndSymbols() {
        assertEquals(DistanceUnit.KM, DistanceUnit.fromString("km"))
        assertEquals(DistanceUnit.KM, DistanceUnit.fromString("KM"))
        assertEquals(DistanceUnit.KM, DistanceUnit.fromString("Kilometers"))
        assertEquals(DistanceUnit.MILES, DistanceUnit.fromString("mi"))
        assertEquals(DistanceUnit.MILES, DistanceUnit.fromString("MILES"))
        // Fallback default
        assertEquals(DistanceUnit.KM, DistanceUnit.fromString("yards"))
    }

    @Test
    fun displayWeightFormatting_preservesCleanValues() {
        // Storage is always in KG
        val storedKg = 100.0

        val inKg = UnitConverter.displayWeight(storedKg, WeightUnit.KG)
        assertEquals(100.0, inKg, 0.001)

        val inLbs = UnitConverter.displayWeight(storedKg, WeightUnit.LBS)
        assertEquals(220.5, inLbs, 0.001)

        // Storage conversion back
        val storedBackFromLbs = UnitConverter.storageWeight(inLbs, WeightUnit.LBS)
        assertEquals(100.0, storedBackFromLbs, 0.1)
    }

    @Test
    fun displayDistanceFormatting_preservesCleanValues() {
        val storedKm = 5.0

        val inKm = UnitConverter.displayDistance(storedKm, DistanceUnit.KM)
        assertEquals(5.0, inKm, 0.001)

        val inMiles = UnitConverter.displayDistance(storedKm, DistanceUnit.MILES)
        assertEquals(3.11, inMiles, 0.001)

        val storedBackFromMiles = UnitConverter.storageDistance(inMiles, DistanceUnit.MILES)
        assertEquals(5.0, storedBackFromMiles, 0.05)
    }

    @Test
    fun zeroAndNegativeValues_produceZero() {
        assertEquals(0.0, UnitConverter.displayWeight(0.0, WeightUnit.KG), 0.001)
        assertEquals(0.0, UnitConverter.displayWeight(-25.0, WeightUnit.KG), 0.001)
        assertEquals(0.0, UnitConverter.displayDistance(0.0, DistanceUnit.KM), 0.001)
        assertEquals(0.0, UnitConverter.displayDistance(-10.0, DistanceUnit.MILES), 0.001)
    }
}
