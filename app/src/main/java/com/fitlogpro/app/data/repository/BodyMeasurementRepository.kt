package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.BodyMeasurementDao
import com.fitlogpro.app.data.local.entity.BodyMeasurementEntity
import com.fitlogpro.app.domain.model.MeasurementType
import kotlinx.coroutines.flow.Flow

class BodyMeasurementRepository(private val measurementDao: BodyMeasurementDao) {

    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>> =
        measurementDao.getAllMeasurements()

    fun getMeasurementsByType(type: MeasurementType): Flow<List<BodyMeasurementEntity>> =
        measurementDao.getMeasurementsByType(type)

    fun getLatestMeasurementByType(type: MeasurementType): Flow<BodyMeasurementEntity?> =
        measurementDao.getLatestMeasurementByType(type)

    fun getMeasurementTrend(type: MeasurementType): Flow<List<BodyMeasurementEntity>> =
        measurementDao.getMeasurementTrend(type)

    suspend fun saveMeasurement(
        type: MeasurementType,
        valueMetric: Double,
        notes: String = "",
        recordedEpochMs: Long = System.currentTimeMillis()
    ): Long {
        val entity = BodyMeasurementEntity(
            id = 0L,
            measurementType = type,
            value = valueMetric,
            recordedEpochMs = recordedEpochMs,
            notes = notes
        )
        return measurementDao.insertMeasurement(entity)
    }

    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity) =
        measurementDao.deleteMeasurement(measurement)
}
