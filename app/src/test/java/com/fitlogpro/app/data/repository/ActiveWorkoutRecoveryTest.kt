package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.ActiveWorkoutDao
import com.fitlogpro.app.data.local.entity.ActiveWorkoutSessionEntity
import com.fitlogpro.app.domain.model.ActiveWorkoutSession
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.LiveWorkoutExercise
import com.fitlogpro.app.domain.model.LiveWorkoutSet
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.PRType
import com.fitlogpro.app.domain.model.SetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveWorkoutRecoveryTest {

    private class MockActiveWorkoutDao : ActiveWorkoutDao {
        var currentEntity: ActiveWorkoutSessionEntity? = null

        override fun getActiveSessionFlow(): Flow<ActiveWorkoutSessionEntity?> = flowOf(currentEntity)

        override suspend fun getActiveSessionSync(): ActiveWorkoutSessionEntity? = currentEntity

        override suspend fun saveActiveSession(entity: ActiveWorkoutSessionEntity) {
            currentEntity = entity
        }

        override suspend fun clearActiveSession() {
            currentEntity = null
        }
    }

    @Test
    fun saveAndRecoverPausedSession_preservesPauseStateAndTimestamps() = runBlocking {
        val dao = MockActiveWorkoutDao()
        val repo = ActiveWorkoutRepository(dao)

        val pausedSession = ActiveWorkoutSession(
            sessionId = "CURRENT_ACTIVE_SESSION",
            routineId = null, // Blank workout
            workoutName = "Ad-hoc Core & Conditioning",
            startTimeEpochMs = 1700000000000L,
            elapsedSeconds = 945,
            isPaused = true,
            lastPauseTimestampMs = 1700000945000L,
            workoutNotes = "Interrupted by phone call",
            exercises = listOf(
                LiveWorkoutExercise(
                    id = "e1",
                    exerciseId = 50L,
                    exerciseName = "Plank",
                    muscleGroup = MuscleGroup.CORE,
                    equipment = Equipment.BODYWEIGHT,
                    exerciseType = ExerciseType.CARDIO,
                    defaultRestSeconds = 60,
                    sets = listOf(
                        LiveWorkoutSet(
                            id = "s1",
                            setNumber = 1,
                            setType = SetType.NORMAL,
                            durationSeconds = 60,
                            isCompleted = true
                        ),
                        LiveWorkoutSet(
                            id = "s2",
                            setNumber = 2,
                            setType = SetType.NORMAL,
                            durationSeconds = 45,
                            isCompleted = false
                        )
                    )
                )
            )
        )

        // Simulate save before process termination
        repo.saveActiveSession(pausedSession)

        // Simulate cold app relaunch and recovery
        val recovered = repo.getActiveSession()
        assertNotNull(recovered)
        recovered!!
        assertEquals("Ad-hoc Core & Conditioning", recovered.workoutName)
        assertNull(recovered.routineId)
        assertTrue(recovered.isPaused)
        assertEquals(1700000945000L, recovered.lastPauseTimestampMs)
        assertEquals(945L, recovered.elapsedSeconds)
        assertEquals("Interrupted by phone call", recovered.workoutNotes)
        assertEquals(1, recovered.exercises.size)
        assertEquals(2, recovered.exercises[0].sets.size)
        assertEquals(60, recovered.exercises[0].sets[0].durationSeconds)
        assertTrue(recovered.exercises[0].sets[0].isCompleted)
    }

    @Test
    fun clearActiveSession_removesEntityFromDatabase() = runBlocking {
        val dao = MockActiveWorkoutDao()
        val repo = ActiveWorkoutRepository(dao)

        val session = ActiveWorkoutSession(
            sessionId = "CURRENT_ACTIVE_SESSION",
            routineId = 1L,
            workoutName = "Leg Day",
            startTimeEpochMs = 1000L,
            elapsedSeconds = 120L,
            exercises = emptyList()
        )

        repo.saveActiveSession(session)
        assertNotNull(repo.getActiveSession())

        repo.clearActiveSession()
        assertNull(repo.getActiveSession())
    }
}
