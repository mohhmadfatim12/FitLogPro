package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.BodyMeasurementDao
import com.fitlogpro.app.data.local.entity.BodyMeasurementEntity
import com.fitlogpro.app.domain.model.MeasurementType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class BodyMeasurementRepositoryTest {

    @Test
    fun saveMeasurement_storesMetricValuesWithTimestamp() = runBlocking {
        val fakeDao = FakeMeasurementDao()
        val repo = BodyMeasurementRepository(fakeDao)

        val id = repo.saveMeasurement(
            type = MeasurementType.WEIGHT,
            valueMetric = 81.5,
            notes = "Post-workout",
            recordedEpochMs = 1700000000000L
        )

        assertEquals(1L, id)
        assertEquals(1, fakeDao.list.size)
        val saved = fakeDao.list.first()
        assertEquals(81.5, saved.value, 0.01)
        assertEquals(MeasurementType.WEIGHT, saved.measurementType)
        assertEquals("Post-workout", saved.notes)
    }

    private class FakeMeasurementDao : BodyMeasurementDao {
        val list = mutableListOf<BodyMeasurementEntity>()

        override fun getAllMeasurements() = flowOf(list.toList())
        override fun getMeasurementsByType(type: MeasurementType) = flowOf(list.filter { it.measurementType == type })
        override fun getLatestMeasurementByType(type: MeasurementType) = flowOf(list.filter { it.measurementType == type }.maxByOrNull { it.recordedEpochMs })
        override fun getMeasurementTrend(type: MeasurementType) = flowOf(list.filter { it.measurementType == type }.sortedBy { it.recordedEpochMs })
        override suspend fun insertMeasurement(measurement: BodyMeasurementEntity): Long {
            val id = (list.size + 1).toLong()
            list.add(measurement.copy(id = id))
            return id
        }
        override suspend fun insertAll(measurements: List<BodyMeasurementEntity>) { list.addAll(measurements) }
        override suspend fun updateMeasurement(measurement: BodyMeasurementEntity) {}
        override suspend fun deleteMeasurement(measurement: BodyMeasurementEntity) { list.remove(measurement) }
    }
}
