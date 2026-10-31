package com.fitlogpro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogpro.app.data.local.entity.PersonalRecordEntity
import com.fitlogpro.app.domain.model.PRType
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {

    @Query("SELECT * FROM personal_records ORDER BY achievedAtEpochMs DESC")
    fun getAllPrs(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId ORDER BY achievedAtEpochMs DESC")
    fun getPrsForExercise(exerciseId: Long): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND prType = :prType ORDER BY value DESC LIMIT 1")
    suspend fun getBestPrForExercise(exerciseId: Long, prType: PRType): PersonalRecordEntity?

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId")
    suspend fun getAllPrsForExerciseSync(exerciseId: Long): List<PersonalRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPr(pr: PersonalRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prs: List<PersonalRecordEntity>)

    @Delete
    suspend fun deletePr(pr: PersonalRecordEntity)

    @Query("DELETE FROM personal_records WHERE exerciseId = :exerciseId")
    suspend fun deleteAllForExercise(exerciseId: Long)
}
