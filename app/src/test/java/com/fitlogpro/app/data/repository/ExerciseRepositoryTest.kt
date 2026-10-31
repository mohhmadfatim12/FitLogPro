package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.ExerciseDao
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.MuscleGroup
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExerciseRepositoryTest {

    @Test
    fun getExerciseById_returnsMatchingExercise() = runBlocking {
        val fakeDao = FakeExerciseDao()
        val repo = ExerciseRepository(fakeDao)

        val exercise = repo.getExerciseById(1L)
        assertNotNull(exercise)
        assertEquals("Barbell Bench Press", exercise?.name)
        assertEquals(MuscleGroup.CHEST, exercise?.muscleGroup)
    }

    @Test
    fun toggleFavorite_updatesFavoriteState() = runBlocking {
        val fakeDao = FakeExerciseDao()
        val repo = ExerciseRepository(fakeDao)

        repo.toggleFavorite(1L, true)
        val updated = repo.getExerciseById(1L)
        assertEquals(true, updated?.isFavorite)
    }

    private class FakeExerciseDao : ExerciseDao {
        private val list = mutableListOf(
            ExerciseEntity(1L, "Barbell Bench Press", MuscleGroup.CHEST, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 120, false, "", false),
            ExerciseEntity(2L, "Barbell Squat", MuscleGroup.QUADS, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 180, false, "", false)
        )

        override fun getAllExercises() = flowOf(list.toList())
        override fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup) = flowOf(list.filter { it.muscleGroup == muscleGroup })
        override fun searchExercises(query: String) = flowOf(list.filter { it.name.contains(query, ignoreCase = true) })
        override suspend fun getExerciseById(id: Long) = list.find { it.id == id }
        override suspend fun insertExercise(exercise: ExerciseEntity): Long {
            list.add(exercise)
            return exercise.id
        }
        override suspend fun insertAll(exercises: List<ExerciseEntity>) { list.addAll(exercises) }
        override suspend fun updateExercise(exercise: ExerciseEntity) {
            val idx = list.indexOfFirst { it.id == exercise.id }
            if (idx != -1) list[idx] = exercise
        }
        override suspend fun deleteExercise(exercise: ExerciseEntity) { list.removeAll { it.id == exercise.id } }
        override suspend fun updateFavorite(id: Long, isFavorite: Boolean) {
            val idx = list.indexOfFirst { it.id == id }
            if (idx != -1) list[idx] = list[idx].copy(isFavorite = isFavorite)
        }
        override suspend fun getCount() = list.size
    }
}
