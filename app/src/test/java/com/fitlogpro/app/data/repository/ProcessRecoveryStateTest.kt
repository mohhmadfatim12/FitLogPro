package com.fitlogpro.app.data.repository

import com.fitlogpro.app.domain.model.ActiveWorkoutSession
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.LiveWorkoutExercise
import com.fitlogpro.app.domain.model.LiveWorkoutSet
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.PRType
import com.fitlogpro.app.domain.model.SetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessRecoveryStateTest {

    @Test
    fun serializeAndDeserialize_restoresFullActiveSessionFidelity() {
        // Create an ActiveWorkoutRepository instance with a dummy DAO (we only test serialize & deserialize)
        val repo = ActiveWorkoutRepository(DummyActiveWorkoutDao())

        val set1 = LiveWorkoutSet(
            id = "s-1",
            setNumber = 1,
            setType = SetType.WARMUP,
            weightKg = 60.0,
            reps = 12,
            isCompleted = true,
            previousPerformance = "50kg x 12"
        )
        val set2 = LiveWorkoutSet(
            id = "s-2",
            setNumber = 2,
            setType = SetType.NORMAL,
            weightKg = 100.0,
            reps = 8,
            rpe = 8.5,
            isCompleted = true,
            isPr = true,
            prTypes = listOf(PRType.HEAVIEST_WEIGHT, PRType.BEST_1RM),
            previousPerformance = "95kg x 8"
        )

        val exercise = LiveWorkoutExercise(
            id = "ex-1",
            exerciseId = 1L,
            exerciseName = "Barbell Bench Press",
            muscleGroup = MuscleGroup.CHEST,
            equipment = Equipment.BARBELL,
            exerciseType = ExerciseType.WEIGHT_AND_REPS,
            defaultRestSeconds = 90,
            supersetGroupId = "SS_1",
            supersetLabel = "A1",
            notes = "Pause at bottom",
            sets = listOf(set1, set2)
        )

        val session = ActiveWorkoutSession(
            sessionId = "CURRENT_ACTIVE_SESSION",
            routineId = 10L,
            workoutName = "Heavy Push Day",
            startTimeEpochMs = 1700000000000L,
            elapsedSeconds = 1240L,
            isPaused = false,
            lastPauseTimestampMs = null,
            exercises = listOf(exercise),
            workoutNotes = "Felt super energized"
        )

        val serialized = repo.serialize(session)
        assertNotNull(serialized)
        assertTrue(serialized.contains("Heavy Push Day"))

        val restored = repo.deserialize(serialized)
        assertNotNull(restored)
        assertEquals("CURRENT_ACTIVE_SESSION", restored?.sessionId)
        assertEquals(10L, restored?.routineId)
        assertEquals("Heavy Push Day", restored?.workoutName)
        assertEquals(1240L, restored?.elapsedSeconds)
        assertEquals("Felt super energized", restored?.workoutNotes)
        assertEquals(1, restored?.exercises?.size)

        val restoredEx = restored?.exercises?.first()
        assertEquals("Barbell Bench Press", restoredEx?.exerciseName)
        assertEquals("SS_1", restoredEx?.supersetGroupId)
        assertEquals("A1", restoredEx?.supersetLabel)
        assertEquals(2, restoredEx?.sets?.size)

        val restoredSet1 = restoredEx?.sets?.get(0)
        assertEquals(SetType.WARMUP, restoredSet1?.setType)
        assertEquals(60.0, restoredSet1?.weightKg ?: 0.0, 0.01)

        val restoredSet2 = restoredEx?.sets?.get(1)
        assertEquals(SetType.NORMAL, restoredSet2?.setType)
        assertEquals(100.0, restoredSet2?.weightKg ?: 0.0, 0.01)
        assertEquals(8.5, restoredSet2?.rpe ?: 0.0, 0.01)
        assertTrue(restoredSet2?.isPr == true)
        assertEquals(2, restoredSet2?.prTypes?.size)
        assertTrue(restoredSet2?.prTypes?.contains(PRType.HEAVIEST_WEIGHT) == true)
    }

    private class DummyActiveWorkoutDao : com.fitlogpro.app.data.local.dao.ActiveWorkoutDao {
        override fun getActiveSessionFlow() = kotlinx.coroutines.flow.flowOf(null)
        override suspend fun getActiveSessionSync() = null
        override suspend fun saveActiveSession(session: com.fitlogpro.app.data.local.entity.ActiveWorkoutSessionEntity) {}
        override suspend fun clearActiveSession() {}
    }
}
