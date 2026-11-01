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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvFormattingSafetyTest {

    @Test
    fun exportEmptyLists_producesOnlyHeadersWithoutCrashing() {
        val emptyWorkouts = CsvExporter.exportWorkoutsToCsv(emptyList(), WeightUnit.KG)
        val lines = emptyWorkouts.trim().split("\n")
        assertEquals(1, lines.size)
        assertTrue(lines[0].startsWith("Workout Date,Workout Name,Duration (min)"))

        val emptyExercises = CsvExporter.exportExercisesToCsv(emptyList())
        val exLines = emptyExercises.trim().split("\n")
        assertEquals(1, exLines.size)
        assertTrue(exLines[0].startsWith("ID,Exercise Name,Muscle Group"))

        val emptyMeasurements = CsvExporter.exportMeasurementsToCsv(emptyList())
        val measLines = emptyMeasurements.trim().split("\n")
        assertEquals(1, measLines.size)
        assertTrue(measLines[0].contains("Measurement Type,Value,Unit,Notes"))
    }

    @Test
    fun exportWorkoutsWithQuotesAndCommas_escapesCorrectly() {
        val complexExercise = ExerciseEntity(
            id = 1,
            name = "Dumbbell Press, Flat (\"Heavy\")",
            muscleGroup = MuscleGroup.CHEST,
            equipment = Equipment.DUMBBELL,
            exerciseType = ExerciseType.WEIGHT_AND_REPS,
            defaultRestSeconds = 90,
            isCustom = true,
            instructions = "Press up, squeeze \"chest\", don't lock out"
        )

        val set = WorkoutSetEntity(
            id = 1,
            workoutExerciseId = 1,
            setNumber = 1,
            setType = SetType.NORMAL,
            weightKg = 40.0,
            reps = 10,
            rpe = 8.5,
            durationSeconds = null,
            distanceKm = null,
            calories = null,
            isCompleted = true,
            isPr = false
        )

        val workoutExercise = WorkoutExerciseWithSets(
            workoutExercise = WorkoutExerciseEntity(1, 1, 1, 1, null, "Note with, comma and \"quotes\""),
            exercise = complexExercise,
            sets = listOf(set)
        )

        val workout = WorkoutWithDetails(
            workout = WorkoutEntity(
                id = 1,
                name = "Push Day: Chest, Shoulders, & Triceps (\"Phase 1\")",
                routineId = null,
                startTimeEpochMs = 1700000000000L,
                endTimeEpochMs = 1700003600000L,
                durationSeconds = 3600,
                totalVolumeKg = 400.0,
                notes = "Felt \"incredible\", but shoulders were sore, rest next time."
            ),
            exercises = listOf(workoutExercise)
        )

        val csv = CsvExporter.exportWorkoutsToCsv(listOf(workout), WeightUnit.KG)

        // Verifies internal quotes become double-quotes
        assertTrue(csv.contains("\"\"Heavy\"\""))
        assertTrue(csv.contains("\"\"Phase 1\"\""))
        assertTrue(csv.contains("\"\"incredible\"\""))
        // Verifies field with comma is surrounded by outer quotes
        assertTrue(csv.contains("\"Push Day: Chest, Shoulders, & Triceps (\"\"Phase 1\"\")\""))
    }

    @Test
    fun exportWorkoutsInLbs_adjustsHeaderUnitToLbs() {
        val csv = CsvExporter.exportWorkoutsToCsv(emptyList(), WeightUnit.LBS)
        assertTrue(csv.contains("Weight (lbs)"))
        assertFalse(csv.contains("Weight (kg)"))
    }

    @Test
    fun exportMeasurementsWithSpecialChars_formatsSafely() {
        val measurement = BodyMeasurementEntity(
            id = 1,
            measurementType = MeasurementType.WEIGHT,
            value = 85.5,
            recordedEpochMs = 1700000000000L,
            notes = "Morning weight, post-bathroom, \"fasted\" condition"
        )

        val csv = CsvExporter.exportMeasurementsToCsv(listOf(measurement))
        assertTrue(csv.contains("\"Body Weight\""))
        assertTrue(csv.contains("85.5"))
        assertTrue(csv.contains("\"\"fasted\"\""))
    }
}
