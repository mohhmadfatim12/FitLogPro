package com.fitlogpro.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitlogpro.app.domain.model.MeasurementType

@Entity(
    tableName = "body_measurements",
    indices = [
        Index("measurementType"),
        Index("recordedEpochMs")
    ]
)
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val measurementType: MeasurementType,
    val value: Double, // Stored in metric (kg, cm, %)
    val recordedEpochMs: Long,
    val notes: String = ""
)
