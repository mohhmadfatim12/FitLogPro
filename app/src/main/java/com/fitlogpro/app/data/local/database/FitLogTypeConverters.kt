package com.fitlogpro.app.data.local.database

import androidx.room.TypeConverter
import com.fitlogpro.app.domain.model.DistanceUnit
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.MeasurementType
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.PRType
import com.fitlogpro.app.domain.model.SetType
import com.fitlogpro.app.domain.model.WeightUnit

class FitLogTypeConverters {

    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup?): String? = value?.name

    @TypeConverter
    fun toMuscleGroup(value: String?): MuscleGroup? =
        value?.let { MuscleGroup.fromString(it) }

    @TypeConverter
    fun fromEquipment(value: Equipment?): String? = value?.name

    @TypeConverter
    fun toEquipment(value: String?): Equipment? =
        value?.let { Equipment.fromString(it) }

    @TypeConverter
    fun fromExerciseType(value: ExerciseType?): String? = value?.name

    @TypeConverter
    fun toExerciseType(value: String?): ExerciseType? =
        value?.let { ExerciseType.fromString(it) }

    @TypeConverter
    fun fromSetType(value: SetType?): String? = value?.name

    @TypeConverter
    fun toSetType(value: String?): SetType? =
        value?.let { SetType.fromString(it) }

    @TypeConverter
    fun fromPRType(value: PRType?): String? = value?.name

    @TypeConverter
    fun toPRType(value: String?): PRType? =
        value?.let { PRType.fromString(it) }

    @TypeConverter
    fun fromMeasurementType(value: MeasurementType?): String? = value?.name

    @TypeConverter
    fun toMeasurementType(value: String?): MeasurementType? =
        value?.let { MeasurementType.fromString(it) }

    @TypeConverter
    fun fromWeightUnit(value: WeightUnit?): String? = value?.name

    @TypeConverter
    fun toWeightUnit(value: String?): WeightUnit? =
        value?.let { WeightUnit.fromString(it) }

    @TypeConverter
    fun fromDistanceUnit(value: DistanceUnit?): String? = value?.name

    @TypeConverter
    fun toDistanceUnit(value: String?): DistanceUnit? =
        value?.let { DistanceUnit.fromString(it) }
}
