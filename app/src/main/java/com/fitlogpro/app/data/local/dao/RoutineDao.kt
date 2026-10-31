package com.fitlogpro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fitlogpro.app.data.local.entity.RoutineEntity
import com.fitlogpro.app.data.local.entity.RoutineExerciseCrossRef
import com.fitlogpro.app.data.local.entity.RoutineExerciseItem
import com.fitlogpro.app.data.local.entity.RoutineWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Query("SELECT * FROM routines ORDER BY name ASC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: Long): RoutineEntity?

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    fun getRoutineWithExercises(id: Long): Flow<RoutineWithExercises?>

    @Transaction
    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun getRoutineExerciseItems(routineId: Long): Flow<List<RoutineExerciseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(crossRefs: List<RoutineExerciseCrossRef>)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteRoutineExercises(routineId: Long)

    @Transaction
    suspend fun updateRoutineWithExercises(
        routine: RoutineEntity,
        exercises: List<RoutineExerciseCrossRef>
    ) {
        updateRoutine(routine)
        deleteRoutineExercises(routine.id)
        insertRoutineExercises(exercises)
    }

    @Query("UPDATE routines SET lastPerformedEpochMs = :timestampMs WHERE id = :routineId")
    suspend fun updateLastPerformed(routineId: Long, timestampMs: Long)
}
