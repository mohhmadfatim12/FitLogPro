package com.fitlogpro.app.data.local.database

import com.fitlogpro.app.domain.model.DistanceUnit
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.MeasurementType
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.PRType
import com.fitlogpro.app.domain.model.SetType
import com.fitlogpro.app.domain.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RoomTypeConvertersSafetyTest {

    private val converters = FitLogTypeConverters()

    @Test
    fun muscleGroup_roundTripAndNullSafety() {
        for (group in MuscleGroup.entries) {
            val str = converters.fromMuscleGroup(group)
            val converted = converters.toMuscleGroup(str)
            assertEquals(group, converted)
        }
        assertNull(converters.fromMuscleGroup(null))
        assertNull(converters.toMuscleGroup(null))
        assertEquals(MuscleGroup.OTHER, converters.toMuscleGroup("INVALID_GROUP"))
    }

    @Test
    fun equipment_roundTripAndNullSafety() {
        for (eq in Equipment.entries) {
            val str = converters.fromEquipment(eq)
            val converted = converters.toEquipment(str)
            assertEquals(eq, converted)
        }
        assertNull(converters.fromEquipment(null))
        assertNull(converters.toEquipment(null))
        assertEquals(Equipment.OTHER, converters.toEquipment("INVALID_EQUIPMENT"))
    }

    @Test
    fun exerciseType_roundTripAndNullSafety() {
        for (type in ExerciseType.entries) {
            val str = converters.fromExerciseType(type)
            val converted = converters.toExerciseType(str)
            assertEquals(type, converted)
        }
        assertNull(converters.fromExerciseType(null))
        assertNull(converters.toExerciseType(null))
    }

    @Test
    fun setType_roundTripAndNullSafety() {
        for (st in SetType.entries) {
            val str = converters.fromSetType(st)
            val converted = converters.toSetType(str)
            assertEquals(st, converted)
        }
        assertNull(converters.fromSetType(null))
        assertNull(converters.toSetType(null))
        assertEquals(SetType.NORMAL, converters.toSetType("UNKNOWN_SET"))
    }

    @Test
    fun prType_roundTripAndNullSafety() {
        for (pr in PRType.entries) {
            val str = converters.fromPRType(pr)
            val converted = converters.toPRType(str)
            assertEquals(pr, converted)
        }
        assertNull(converters.fromPRType(null))
        assertNull(converters.toPRType(null))
        assertEquals(PRType.HEAVIEST_WEIGHT, converters.toPRType("UNKNOWN_PR"))
    }

    @Test
    fun measurementType_roundTripAndNullSafety() {
        for (m in MeasurementType.entries) {
            val str = converters.fromMeasurementType(m)
            val converted = converters.toMeasurementType(str)
            assertEquals(m, converted)
        }
        assertNull(converters.fromMeasurementType(null))
        assertNull(converters.toMeasurementType(null))
        assertEquals(MeasurementType.WEIGHT, converters.toMeasurementType("UNKNOWN_MEASUREMENT"))
    }

    @Test
    fun unitConverters_roundTripAndNullSafety() {
        assertEquals(WeightUnit.KG, converters.toWeightUnit(converters.fromWeightUnit(WeightUnit.KG)))
        assertEquals(WeightUnit.LBS, converters.toWeightUnit(converters.fromWeightUnit(WeightUnit.LBS)))
        assertNull(converters.fromWeightUnit(null))
        assertNull(converters.toWeightUnit(null))

        assertEquals(DistanceUnit.KM, converters.toDistanceUnit(converters.fromDistanceUnit(DistanceUnit.KM)))
        assertEquals(DistanceUnit.MILES, converters.toDistanceUnit(converters.fromDistanceUnit(DistanceUnit.MILES)))
        assertNull(converters.fromDistanceUnit(null))
        assertNull(converters.toDistanceUnit(null))
    }
}
