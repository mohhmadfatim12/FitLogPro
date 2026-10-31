package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.ExerciseHistoryRow
import com.fitlogpro.app.data.local.dao.WorkoutDao
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.data.local.entity.WorkoutEntity
import com.fitlogpro.app.data.local.entity.WorkoutExerciseEntity
import com.fitlogpro.app.data.local.entity.WorkoutExerciseWithSets
import com.fitlogpro.app.data.local.entity.WorkoutSetEntity
import com.fitlogpro.app.data.local.entity.WorkoutWithDetails
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.SetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AnalyticsRepositoryAggregatesTest {

    private class MockWorkoutDaoForAnalytics(
        val workoutsList: List<WorkoutEntity> = emptyList(),
        val workoutsWithDetailsList: List<WorkoutWithDetails> = emptyList(),
        val perfRows: List<ExerciseHistoryRow> = emptyList()
    ) : WorkoutDao {
        override fun getAllWorkouts(): Flow<List<WorkoutEntity>> = flowOf(workoutsList)
        override fun getAllWorkoutsWithDetails(): Flow<List<WorkoutWithDetails>> = flowOf(workoutsWithDetailsList)
        override fun getWorkoutWithDetails(workoutId: Long): Flow<WorkoutWithDetails?> = flowOf(null)
        override suspend fun getWorkoutById(workoutId: Long): WorkoutEntity? = null
        override fun getWorkoutsBetweenDates(startEpochMs: Long, endEpochMs: Long): Flow<List<WorkoutEntity>> = flowOf(emptyList())
        override suspend fun getAllWorkoutTimestamps(): List<Long> = emptyList()
        override fun getAllWorkoutTimestampsFlow(): Flow<List<Long>> = flowOf(emptyList())
        override suspend fun insertWorkout(workout: WorkoutEntity): Long = 1L
        override suspend fun insertWorkoutExercise(exercise: WorkoutExerciseEntity): Long = 1L
        override suspend fun insertWorkoutSets(sets: List<WorkoutSetEntity>) {}
        override suspend fun updateWorkout(workout: WorkoutEntity) {}
        override suspend fun deleteWorkout(workout: WorkoutEntity) {}
        override suspend fun deleteWorkoutExercises(workoutId: Long) {}
        override fun getCompletedSetsForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>> = flowOf(emptyList())
        override suspend fun getExercisePerformanceHistory(exerciseId: Long): List<ExerciseHistoryRow> = perfRows
        override suspend fun getWorkoutCount(): Int = workoutsList.size
        override suspend fun getTotalVolumeAllTime(): Double = workoutsList.sumOf { it.totalVolumeKg }
        override suspend fun getTotalDurationAllTime(): Long = workoutsList.sumOf { it.durationSeconds }
    }

    @Test
    fun muscleDistribution_calculatesPercentagesAccurately() = runBlocking {
        val chestEx = ExerciseEntity(1, "Bench Press", MuscleGroup.CHEST, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 90, false, "")
        val backEx = ExerciseEntity(2, "Pull Up", MuscleGroup.BACK, Equipment.BODYWEIGHT, ExerciseType.BODYWEIGHT_REPS, 60, false, "")

        val chestSets = listOf(
            WorkoutSetEntity(1, 10, 1, SetType.NORMAL, 100.0, 10, 8.0, null, null, null, isCompleted = true, isPr = false),
            WorkoutSetEntity(2, 10, 2, SetType.NORMAL, 100.0, 10, 8.0, null, null, null, isCompleted = true, isPr = false),
            WorkoutSetEntity(3, 10, 3, SetType.NORMAL, 100.0, 10, 8.0, null, null, null, isCompleted = true, isPr = false)
        ) // 3 chest sets

        val backSets = listOf(
            WorkoutSetEntity(4, 20, 1, SetType.NORMAL, 0.0, 10, 8.0, null, null, null, isCompleted = true, isPr = false)
        ) // 1 back set

        val workout = WorkoutWithDetails(
            workout = WorkoutEntity(1, "Upper Day", null, 1700000000000L, 1700003600000L, 3600, 3000.0, ""),
            exercises = listOf(
                WorkoutExerciseWithSets(WorkoutExerciseEntity(10, 1, 1, 1, null, ""), chestEx, chestSets),
                WorkoutExerciseWithSets(WorkoutExerciseEntity(20, 1, 2, 2, null, ""), backEx, backSets)
            )
        )

        val repo = AnalyticsRepository(MockWorkoutDaoForAnalytics(workoutsWithDetailsList = listOf(workout)))
        val distribution = repo.getMuscleGroupDistribution().first()

        assertEquals(2, distribution.size)
        // Chest = 3/4 = 75%
        val chestDist = distribution.find { it.muscleGroup == MuscleGroup.CHEST }!!
        assertEquals(75.0f, chestDist.percentage, 0.01f)
        assertEquals(3, chestDist.totalSets)

        // Back = 1/4 = 25%
        val backDist = distribution.find { it.muscleGroup == MuscleGroup.BACK }!!
        assertEquals(25.0f, backDist.percentage, 0.01f)
        assertEquals(1, backDist.totalSets)
    }

    @Test
    fun emptyHistory_returnsEmptyListsWithoutThrowing() = runBlocking {
        val repo = AnalyticsRepository(MockWorkoutDaoForAnalytics())
        val weekly = repo.getWeeklyVolumeHistory().first()
        val distribution = repo.getMuscleGroupDistribution().first()
        val exerciseProgress = repo.getExerciseProgress(999L)

        assertTrue(weekly.isEmpty())
        assertTrue(distribution.isEmpty())
        assertTrue(exerciseProgress.isEmpty())
    }
}
