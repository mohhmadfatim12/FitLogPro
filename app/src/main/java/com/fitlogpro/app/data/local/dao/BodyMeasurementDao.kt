package com.fitlogpro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fitlogpro.app.data.local.entity.BodyMeasurementEntity
import com.fitlogpro.app.domain.model.MeasurementType
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {

    @Query("SELECT * FROM body_measurements ORDER BY recordedEpochMs DESC")
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements WHERE measurementType = :type ORDER BY recordedEpochMs DESC")
    fun getMeasurementsByType(type: MeasurementType): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements WHERE measurementType = :type ORDER BY recordedEpochMs DESC LIMIT 1")
    fun getLatestMeasurementByType(type: MeasurementType): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements WHERE measurementType = :type ORDER BY recordedEpochMs ASC")
    fun getMeasurementTrend(type: MeasurementType): Flow<List<BodyMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: BodyMeasurementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(measurements: List<BodyMeasurementEntity>)

    @Update
    suspend fun updateMeasurement(measurement: BodyMeasurementEntity)

    @Delete
    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity)
}
