package com.fitlogpro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fitlogpro.app.data.local.entity.WorkoutEntity
import com.fitlogpro.app.data.local.entity.WorkoutExerciseEntity
import com.fitlogpro.app.data.local.entity.WorkoutSetEntity
import com.fitlogpro.app.data.local.entity.WorkoutWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workouts ORDER BY startTimeEpochMs DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY startTimeEpochMs DESC")
    fun getAllWorkoutsWithDetails(): Flow<List<WorkoutWithDetails>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId LIMIT 1")
    fun getWorkoutWithDetails(workoutId: Long): Flow<WorkoutWithDetails?>

    @Query("SELECT * FROM workouts WHERE id = :workoutId LIMIT 1")
    suspend fun getWorkoutById(workoutId: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE startTimeEpochMs >= :startEpochMs AND startTimeEpochMs <= :endEpochMs ORDER BY startTimeEpochMs ASC")
    fun getWorkoutsBetweenDates(startEpochMs: Long, endEpochMs: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT startTimeEpochMs FROM workouts ORDER BY startTimeEpochMs ASC")
    suspend fun getAllWorkoutTimestamps(): List<Long>

    @Query("SELECT startTimeEpochMs FROM workouts ORDER BY startTimeEpochMs ASC")
    fun getAllWorkoutTimestampsFlow(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSets(sets: List<WorkoutSetEntity>)

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteWorkoutExercises(workoutId: Long)

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        WHERE we.exerciseId = :exerciseId AND ws.isCompleted = 1
        ORDER BY ws.id ASC
    """)
    fun getCompletedSetsForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Query("""
        SELECT ws.weightKg, ws.reps, ws.setType, w.startTimeEpochMs 
        FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE we.exerciseId = :exerciseId AND ws.isCompleted = 1
        ORDER BY w.startTimeEpochMs ASC
    """)
    suspend fun getExercisePerformanceHistory(exerciseId: Long): List<ExerciseHistoryRow>

    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun getWorkoutCount(): Int

    @Query("SELECT COALESCE(SUM(totalVolumeKg), 0.0) FROM workouts")
    suspend fun getTotalVolumeAllTime(): Double

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM workouts")
    suspend fun getTotalDurationAllTime(): Long
}

data class ExerciseHistoryRow(
    val weightKg: Double,
    val reps: Int,
    val setType: com.fitlogpro.app.domain.model.SetType,
    val startTimeEpochMs: Long
)
