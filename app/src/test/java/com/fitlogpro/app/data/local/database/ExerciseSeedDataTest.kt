package com.fitlogpro.app.data.local.database

import com.fitlogpro.app.domain.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseSeedDataTest {

    @Test
    fun getDefaultExercises_containsSubstantialLibrary() {
        val exercises = ExerciseSeedData.getDefaultExercises()
        assertTrue("Seed library must contain at least 40 exercises", exercises.size >= 40)
    }

    @Test
    fun getDefaultExercises_hasNoDuplicateIds() {
        val exercises = ExerciseSeedData.getDefaultExercises()
        val ids = exercises.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun getDefaultExercises_coversAllMajorMuscleGroups() {
        val exercises = ExerciseSeedData.getDefaultExercises()
        val presentMuscleGroups = exercises.map { it.muscleGroup }.toSet()

        assertTrue(presentMuscleGroups.contains(MuscleGroup.CHEST))
        assertTrue(presentMuscleGroups.contains(MuscleGroup.BACK))
        assertTrue(presentMuscleGroups.contains(MuscleGroup.SHOULDERS))
        assertTrue(presentMuscleGroups.contains(MuscleGroup.QUADS))
        assertTrue(presentMuscleGroups.contains(MuscleGroup.HAMSTRINGS))
        assertTrue(presentMuscleGroups.contains(MuscleGroup.BICEPS))
        assertTrue(presentMuscleGroups.contains(MuscleGroup.TRICEPS))
        assertTrue(presentMuscleGroups.contains(MuscleGroup.CORE))
        assertTrue(presentMuscleGroups.contains(MuscleGroup.CARDIO))
    }

    @Test
    fun getDefaultExercises_allHavePositiveRestTimersAndValidNames() {
        val exercises = ExerciseSeedData.getDefaultExercises()
        exercises.forEach { ex ->
            assertTrue("Exercise name must not be blank", ex.name.isNotBlank())
            assertTrue("Default rest timer must be positive", ex.defaultRestSeconds > 0)
            assertFalse("Seed exercises should not be marked as custom", ex.isCustom)
        }
    }
}
