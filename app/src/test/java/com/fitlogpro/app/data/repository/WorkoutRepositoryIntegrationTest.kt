package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.ExerciseHistoryRow
import com.fitlogpro.app.data.local.dao.PersonalRecordDao
import com.fitlogpro.app.data.local.dao.RoutineDao
import com.fitlogpro.app.data.local.dao.WorkoutDao
import com.fitlogpro.app.data.local.entity.PersonalRecordEntity
import com.fitlogpro.app.data.local.entity.RoutineEntity
import com.fitlogpro.app.data.local.entity.RoutineExerciseCrossRef
import com.fitlogpro.app.data.local.entity.RoutineExerciseItem
import com.fitlogpro.app.data.local.entity.RoutineWithExercises
import com.fitlogpro.app.data.local.entity.WorkoutEntity
import com.fitlogpro.app.data.local.entity.WorkoutExerciseEntity
import com.fitlogpro.app.data.local.entity.WorkoutSetEntity
import com.fitlogpro.app.data.local.entity.WorkoutWithDetails
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
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutRepositoryIntegrationTest {

    private class FakeWorkoutDao : WorkoutDao {
        val workouts = mutableListOf<WorkoutEntity>()
        val workoutExercises = mutableListOf<WorkoutExerciseEntity>()
        val sets = mutableListOf<WorkoutSetEntity>()
        private var nextWorkoutId = 1L
        private var nextWeId = 1L
        private var nextSetId = 1L

        override fun getAllWorkouts(): Flow<List<WorkoutEntity>> = flowOf(workouts)
        override fun getAllWorkoutsWithDetails(): Flow<List<WorkoutWithDetails>> = flowOf(emptyList())
        override fun getWorkoutWithDetails(workoutId: Long): Flow<WorkoutWithDetails?> = flowOf(null)
        override suspend fun getWorkoutById(workoutId: Long): WorkoutEntity? = workouts.find { it.id == workoutId }
        override fun getWorkoutsBetweenDates(startEpochMs: Long, endEpochMs: Long): Flow<List<WorkoutEntity>> =
            flowOf(workouts.filter { it.startTimeEpochMs in startEpochMs..endEpochMs })
        override suspend fun getAllWorkoutTimestamps(): List<Long> = workouts.map { it.startTimeEpochMs }
        override fun getAllWorkoutTimestampsFlow(): Flow<List<Long>> = flowOf(workouts.map { it.startTimeEpochMs })

        override suspend fun insertWorkout(workout: WorkoutEntity): Long {
            val id = nextWorkoutId++
            workouts.add(workout.copy(id = id))
            return id
        }

        override suspend fun insertWorkoutExercise(workoutExercise: WorkoutExerciseEntity): Long {
            val id = nextWeId++
            workoutExercises.add(workoutExercise.copy(id = id))
            return id
        }

        override suspend fun insertWorkoutSets(setsList: List<WorkoutSetEntity>) {
            setsList.forEach {
                sets.add(it.copy(id = nextSetId++))
            }
        }

        override suspend fun updateWorkout(workout: WorkoutEntity) {}
        override suspend fun deleteWorkout(workout: WorkoutEntity) {}
        override suspend fun deleteWorkoutExercises(workoutId: Long) {}
        override fun getCompletedSetsForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>> = flowOf(emptyList())
        override suspend fun getExercisePerformanceHistory(exerciseId: Long): List<ExerciseHistoryRow> = emptyList()
        override suspend fun getWorkoutCount(): Int = workouts.size
        override suspend fun getTotalVolumeAllTime(): Double = workouts.sumOf { it.totalVolumeKg }
        override suspend fun getTotalDurationAllTime(): Long = workouts.sumOf { it.durationSeconds }
    }

    private class FakePrDao : PersonalRecordDao {
        val prs = mutableListOf<PersonalRecordEntity>()
        private var nextId = 1L

        override fun getAllPrs(): Flow<List<PersonalRecordEntity>> = flowOf(prs)
        override fun getPrsForExercise(exerciseId: Long): Flow<List<PersonalRecordEntity>> =
            flowOf(prs.filter { it.exerciseId == exerciseId })
        override suspend fun getBestPrForExercise(exerciseId: Long, prType: PRType): PersonalRecordEntity? =
            prs.filter { it.exerciseId == exerciseId && it.prType == prType }.maxByOrNull { it.value }
        override suspend fun getAllPrsForExerciseSync(exerciseId: Long): List<PersonalRecordEntity> =
            prs.filter { it.exerciseId == exerciseId }
        override suspend fun insertPr(pr: PersonalRecordEntity): Long {
            val id = nextId++
            prs.add(pr.copy(id = id))
            return id
        }
        override suspend fun insertAll(prsList: List<PersonalRecordEntity>) {
            prsList.forEach { insertPr(it) }
        }
        override suspend fun deletePr(pr: PersonalRecordEntity) {
            prs.removeIf { it.id == pr.id }
        }
        override suspend fun deleteAllForExercise(exerciseId: Long) {
            prs.removeIf { it.exerciseId == exerciseId }
        }
    }

    private class FakeRoutineDao : RoutineDao {
        var lastUpdatedRoutineId: Long? = null
        var lastUpdatedTimestamp: Long? = null

        override fun getAllRoutines(): Flow<List<RoutineEntity>> = flowOf(emptyList())
        override suspend fun getRoutineById(id: Long): RoutineEntity? = null
        override fun getRoutineWithExercises(id: Long): Flow<RoutineWithExercises?> = flowOf(null)
        override fun getRoutineExerciseItems(routineId: Long): Flow<List<RoutineExerciseItem>> = flowOf(emptyList())
        override suspend fun insertRoutine(routine: RoutineEntity): Long = 1L
        override suspend fun updateRoutine(routine: RoutineEntity) {}
        override suspend fun deleteRoutine(routine: RoutineEntity) {}
        override suspend fun insertRoutineExercises(crossRefs: List<RoutineExerciseCrossRef>) {}
        override suspend fun deleteRoutineExercises(routineId: Long) {}
        override suspend fun updateLastPerformed(routineId: Long, timestampMs: Long) {
            lastUpdatedRoutineId = routineId
            lastUpdatedTimestamp = timestampMs
        }
    }

    @Test
    fun saveCompletedWorkout_persistsWorkoutSetsAndCalculatesVolumeExcludingWarmups() = runBlocking {
        val workoutDao = FakeWorkoutDao()
        val prDao = FakePrDao()
        val routineDao = FakeRoutineDao()
        val repository = WorkoutRepository(workoutDao, prDao, routineDao)

        val exercise = LiveWorkoutExercise(
            id = "ex-1",
            exerciseId = 10L,
            exerciseName = "Barbell Bench Press",
            muscleGroup = MuscleGroup.CHEST,
            equipment = Equipment.BARBELL,
            exerciseType = ExerciseType.WEIGHT_AND_REPS,
            sets = listOf(
                LiveWorkoutSet(id = "s-1", setNumber = 1, setType = SetType.WARMUP, weightKg = 60.0, reps = 12, isCompleted = true),
                LiveWorkoutSet(id = "s-2", setNumber = 2, setType = SetType.NORMAL, weightKg = 100.0, reps = 8, isCompleted = true, isPr = true, prTypes = listOf(PRType.HEAVIEST_WEIGHT, PRType.BEST_1RM)),
                LiveWorkoutSet(id = "s-3", setNumber = 3, setType = SetType.NORMAL, weightKg = 100.0, reps = 8, isCompleted = true),
                LiveWorkoutSet(id = "s-4", setNumber = 4, setType = SetType.NORMAL, weightKg = 100.0, reps = 8, isCompleted = false) // uncompleted
            )
        )

        val workoutId = repository.saveCompletedWorkout(
            workoutName = "Push Day A",
            routineId = 5L,
            startTimeEpochMs = 1000000L,
            endTimeEpochMs = 1003600L,
            durationSeconds = 3600L,
            workoutNotes = "Felt great!",
            exercises = listOf(exercise)
        )

        assertEquals(1L, workoutId)
        val savedWorkout = workoutDao.workouts.first()
        assertEquals("Push Day A", savedWorkout.name)
        // Warmup: 60x12 excluded. Set 2: 100x8 = 800. Set 3: 100x8 = 800. Set 4: incomplete = 0. Total = 1600.0
        assertEquals(1600.0, savedWorkout.totalVolumeKg, 0.001)

        // Routine last performed updated
        assertEquals(5L, routineDao.lastUpdatedRoutineId)
        assertEquals(1003600L, routineDao.lastUpdatedTimestamp)

        // PRs recorded
        assertEquals(2, prDao.prs.size)
        assertTrue(prDao.prs.any { it.prType == PRType.HEAVIEST_WEIGHT && it.value == 100.0 })
        assertTrue(prDao.prs.any { it.prType == PRType.BEST_1RM })
    }
}
