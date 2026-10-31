package com.fitlogpro.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitlogpro.app.domain.model.PRType

@Entity(
    tableName = "personal_records",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("exerciseId"),
        Index("prType"),
        Index(value = ["exerciseId", "prType"])
    ]
)
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: Long,
    val prType: PRType,
    val value: Double, // Weight in kg, 1RM in kg, volume in kg, or reps count
    val achievedAtEpochMs: Long,
    val workoutId: Long? = null,
    val workoutSetId: Long? = null,
    val notes: String = ""
)
