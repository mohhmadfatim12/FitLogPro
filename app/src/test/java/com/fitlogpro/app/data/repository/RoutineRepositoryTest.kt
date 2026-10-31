package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.RoutineDao
import com.fitlogpro.app.data.local.entity.RoutineEntity
import com.fitlogpro.app.data.local.entity.RoutineExerciseCrossRef
import com.fitlogpro.app.data.local.entity.RoutineExerciseItem
import com.fitlogpro.app.data.local.entity.RoutineWithExercises
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RoutineRepositoryTest {

    @Test
    fun createRoutine_insertsRoutineAndSetsCrossRefIds() = runBlocking {
        val fakeDao = FakeRoutineDao()
        val repo = RoutineRepository(fakeDao)

        val routine = RoutineEntity(0L, "Upper Body Power", "Heavy compound bench and row")
        val items = listOf(
            RoutineExerciseCrossRef(0L, 0L, 1L, 1, 4, 6, 90.0, null, null, ""),
            RoutineExerciseCrossRef(0L, 0L, 7L, 2, 4, 6, 120.0, null, null, "")
        )

        val routineId = repo.createRoutine(routine, items)
        assertEquals(1L, routineId)

        val savedRoutine = repo.getRoutineById(1L)
        assertNotNull(savedRoutine)
        assertEquals("Upper Body Power", savedRoutine?.name)
    }

    private class FakeRoutineDao : RoutineDao {
        val routines = mutableListOf<RoutineEntity>()
        val crossRefs = mutableListOf<RoutineExerciseCrossRef>()

        override fun getAllRoutines() = flowOf(routines.toList())
        override suspend fun getRoutineById(id: Long) = routines.find { it.id == id }
        override fun getRoutineWithExercises(id: Long) = flowOf(null)
        override fun getRoutineExerciseItems(routineId: Long) = flowOf(emptyList<RoutineExerciseItem>())
        override suspend fun insertRoutine(routine: RoutineEntity): Long {
            val id = (routines.size + 1).toLong()
            routines.add(routine.copy(id = id))
            return id
        }
        override suspend fun updateRoutine(routine: RoutineEntity) {}
        override suspend fun deleteRoutine(routine: RoutineEntity) { routines.remove(routine) }
        override suspend fun insertRoutineExercises(list: List<RoutineExerciseCrossRef>) { crossRefs.addAll(list) }
        override suspend fun deleteRoutineExercises(routineId: Long) { crossRefs.removeAll { it.routineId == routineId } }
        override suspend fun updateLastPerformed(routineId: Long, timestampMs: Long) {}
    }
}
