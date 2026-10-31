package com.fitlogpro.app.data.export

import com.fitlogpro.app.data.local.entity.BodyMeasurementEntity
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.data.local.entity.WorkoutEntity
import com.fitlogpro.app.data.local.entity.WorkoutExerciseEntity
import com.fitlogpro.app.data.local.entity.WorkoutExerciseWithSets
import com.fitlogpro.app.data.local.entity.WorkoutSetEntity
import com.fitlogpro.app.data.local.entity.WorkoutWithDetails
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.MeasurementType
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.SetType
import com.fitlogpro.app.domain.model.WeightUnit
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {

    @Test
    fun exportExercisesToCsv_createsValidHeadersAndRows() {
        val exercises = listOf(
            ExerciseEntity(1, "Bench Press", MuscleGroup.CHEST, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 90, false, "Bar on chest"),
            ExerciseEntity(2, "Pull Up", MuscleGroup.BACK, Equipment.BODYWEIGHT, ExerciseType.BODYWEIGHT_REPS, 60, false, "Overhand grip")
        )

        val csv = CsvExporter.exportExercisesToCsv(exercises)
        assertTrue(csv.contains("ID,Exercise Name,Muscle Group,Equipment,Exercise Type,Default Rest (s),Is Custom,Instructions"))
        assertTrue(csv.contains("\"Bench Press\""))
        assertTrue(csv.contains("\"Pull Up\""))
    }

    @Test
    fun exportWorkoutsToCsv_exportsWorkoutHierarchy() {
        val exercise = ExerciseEntity(1, "Squat", MuscleGroup.QUADS, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 120, false, "")
        val sets = listOf(
            WorkoutSetEntity(1, 10, 1, SetType.WARMUP, 60.0, 10, 6.0, null, null, null, true, false),
            WorkoutSetEntity(2, 10, 2, SetType.NORMAL, 100.0, 5, 8.5, null, null, null, true, true)
        )
        val workoutExercise = WorkoutExerciseWithSets(
            workoutExercise = WorkoutExerciseEntity(10, 100, 1, 1, null, ""),
            exercise = exercise,
            sets = sets
        )
        val workoutWithDetails = WorkoutWithDetails(
            workout = WorkoutEntity(100, "Heavy Leg Session", null, 1700000000000L, 1700003600000L, 3600, 500.0, "Great leg day"),
            exercises = listOf(workoutExercise)
        )

        val csv = CsvExporter.exportWorkoutsToCsv(listOf(workoutWithDetails), WeightUnit.KG)
        assertTrue(csv.contains("Workout Date,Workout Name,Duration (min)"))
        assertTrue(csv.contains("\"Heavy Leg Session\""))
        assertTrue(csv.contains("\"Squat\""))
        assertTrue(csv.contains("\"Warm-up\""))
        assertTrue(csv.contains("\"Normal\""))
        assertTrue(csv.contains("true")) // isPr for set 2
    }

    @Test
    fun exportMeasurementsToCsv_includesTypeAndValues() {
        val measurements = listOf(
            BodyMeasurementEntity(1, MeasurementType.WEIGHT, 80.5, 1700000000000L, "Morning weigh-in"),
            BodyMeasurementEntity(2, MeasurementType.BODY_FAT, 14.2, 1700000000000L, "DEXA scan")
        )

        val csv = CsvExporter.exportMeasurementsToCsv(measurements, true)
        assertTrue(csv.contains("Recorded Date,Measurement Type,Value,Unit,Notes"))
        assertTrue(csv.contains("\"Body Weight\""))
        assertTrue(csv.contains("80.5"))
        assertTrue(csv.contains("\"Body Fat\""))
        assertTrue(csv.contains("14.2"))
    }
}
