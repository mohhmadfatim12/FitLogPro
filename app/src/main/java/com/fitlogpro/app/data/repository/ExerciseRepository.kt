package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.ExerciseDao
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.domain.model.MuscleGroup
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(private val exerciseDao: ExerciseDao) {

    fun getAllExercises(): Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()

    fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<ExerciseEntity>> =
        exerciseDao.getExercisesByMuscleGroup(muscleGroup)

    fun searchExercises(query: String): Flow<List<ExerciseEntity>> =
        exerciseDao.searchExercises(query)

    suspend fun getExerciseById(id: Long): ExerciseEntity? = exerciseDao.getExerciseById(id)

    suspend fun saveExercise(exercise: ExerciseEntity): Long {
        return if (exercise.id == 0L) {
            exerciseDao.insertExercise(exercise)
        } else {
            exerciseDao.updateExercise(exercise)
            exercise.id
        }
    }

    suspend fun deleteExercise(exercise: ExerciseEntity) = exerciseDao.deleteExercise(exercise)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) =
        exerciseDao.updateFavorite(id, isFavorite)

    suspend fun getExerciseCount(): Int = exerciseDao.getCount()
}
