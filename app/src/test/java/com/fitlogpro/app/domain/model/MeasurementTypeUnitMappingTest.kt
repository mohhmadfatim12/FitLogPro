package com.fitlogpro.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementTypeUnitMappingTest {

    @Test
    fun weight_mapsToKgOrLbs() {
        assertEquals("kg", MeasurementType.WEIGHT.getUnit(isMetric = true))
        assertEquals("lbs", MeasurementType.WEIGHT.getUnit(isMetric = false))
    }

    @Test
    fun bodyFat_alwaysMapsToPercentage() {
        assertEquals("%", MeasurementType.BODY_FAT.getUnit(isMetric = true))
        assertEquals("%", MeasurementType.BODY_FAT.getUnit(isMetric = false))
    }

    @Test
    fun circumferenceTypes_mapToCmOrIn() {
        val circumferences = listOf(
            MeasurementType.CHEST,
            MeasurementType.WAIST,
            MeasurementType.ARMS,
            MeasurementType.THIGHS,
            MeasurementType.CALVES,
            MeasurementType.SHOULDERS,
            MeasurementType.HIPS,
            MeasurementType.NECK
        )

        for (type in circumferences) {
            assertEquals("${type.name} metric unit must be cm", "cm", type.getUnit(isMetric = true))
            assertEquals("${type.name} imperial unit must be in", "in", type.getUnit(isMetric = false))
        }
    }

    @Test
    fun fromString_resolvesByNameAndDisplayName() {
        assertEquals(MeasurementType.WEIGHT, MeasurementType.fromString("WEIGHT"))
        assertEquals(MeasurementType.WEIGHT, MeasurementType.fromString("Body Weight"))
        assertEquals(MeasurementType.CHEST, MeasurementType.fromString("chest"))
        assertEquals(MeasurementType.BODY_FAT, MeasurementType.fromString("Body Fat"))
        assertEquals(MeasurementType.WEIGHT, MeasurementType.fromString("unknown_val"))
    }
}
