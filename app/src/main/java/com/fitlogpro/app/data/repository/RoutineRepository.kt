package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.RoutineDao
import com.fitlogpro.app.data.local.entity.RoutineEntity
import com.fitlogpro.app.data.local.entity.RoutineExerciseCrossRef
import com.fitlogpro.app.data.local.entity.RoutineExerciseItem
import com.fitlogpro.app.data.local.entity.RoutineWithExercises
import kotlinx.coroutines.flow.Flow

class RoutineRepository(private val routineDao: RoutineDao) {

    fun getAllRoutines(): Flow<List<RoutineEntity>> = routineDao.getAllRoutines()

    fun getRoutineWithExercises(id: Long): Flow<RoutineWithExercises?> =
        routineDao.getRoutineWithExercises(id)

    fun getRoutineExerciseItems(routineId: Long): Flow<List<RoutineExerciseItem>> =
        routineDao.getRoutineExerciseItems(routineId)

    suspend fun getRoutineById(id: Long): RoutineEntity? = routineDao.getRoutineById(id)

    suspend fun createRoutine(
        routine: RoutineEntity,
        exercises: List<RoutineExerciseCrossRef>
    ): Long {
        val routineId = routineDao.insertRoutine(routine)
        val crossRefsWithId = exercises.map { it.copy(routineId = routineId) }
        routineDao.insertRoutineExercises(crossRefsWithId)
        return routineId
    }

    suspend fun updateRoutine(
        routine: RoutineEntity,
        exercises: List<RoutineExerciseCrossRef>
    ) {
        val crossRefsWithId = exercises.map { it.copy(routineId = routine.id) }
        routineDao.updateRoutineWithExercises(routine, crossRefsWithId)
    }

    suspend fun deleteRoutine(routine: RoutineEntity) = routineDao.deleteRoutine(routine)

    suspend fun updateLastPerformed(routineId: Long, timestampMs: Long) =
        routineDao.updateLastPerformed(routineId, timestampMs)
}
