package com.fitlogpro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogpro.app.data.local.entity.ActiveWorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveWorkoutDao {

    @Query("SELECT * FROM active_workout_sessions WHERE sessionId = 'CURRENT_ACTIVE_SESSION' LIMIT 1")
    fun getActiveSessionFlow(): Flow<ActiveWorkoutSessionEntity?>

    @Query("SELECT * FROM active_workout_sessions WHERE sessionId = 'CURRENT_ACTIVE_SESSION' LIMIT 1")
    suspend fun getActiveSessionSync(): ActiveWorkoutSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveSession(session: ActiveWorkoutSessionEntity)

    @Query("DELETE FROM active_workout_sessions WHERE sessionId = 'CURRENT_ACTIVE_SESSION'")
    suspend fun clearActiveSession()
}
