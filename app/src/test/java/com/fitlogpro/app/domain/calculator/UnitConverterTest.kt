package com.fitlogpro.app.domain.calculator

import com.fitlogpro.app.domain.model.DistanceUnit
import com.fitlogpro.app.domain.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun kgToLbs_calculatesAccurately() {
        // 100 kg is ~220.5 lbs
        val lbs = UnitConverter.kgToLbs(100.0)
        assertEquals(220.5, lbs, 0.1)
    }

    @Test
    fun lbsToKg_calculatesAccurately() {
        // 220.462 lbs is ~100 kg
        val kg = UnitConverter.lbsToKg(220.462)
        assertEquals(100.0, kg, 0.1)
    }

    @Test
    fun roundTripConversion_preservesPrecision() {
        val originalKg = 82.5
        val lbs = UnitConverter.kgToLbs(originalKg)
        val backToKg = UnitConverter.lbsToKg(lbs)
        assertEquals(originalKg, backToKg, 0.2)
    }

    @Test
    fun kmAndMiles_calculatesAccurately() {
        // 5 km is ~3.11 miles
        val miles = UnitConverter.kmToMiles(5.0)
        assertEquals(3.11, miles, 0.05)

        // 10 miles is ~16.09 km
        val km = UnitConverter.milesToKm(10.0)
        assertEquals(16.09, km, 0.05)
    }

    @Test
    fun displayAndStorageWeight_respectsTargetUnit() {
        val baseKg = 60.0
        val displayLbs = UnitConverter.displayWeight(baseKg, WeightUnit.LBS)
        assertEquals(132.3, displayLbs, 0.2)

        val storedKg = UnitConverter.storageWeight(displayLbs, WeightUnit.LBS)
        assertEquals(60.0, storedKg, 0.2)
    }
}
