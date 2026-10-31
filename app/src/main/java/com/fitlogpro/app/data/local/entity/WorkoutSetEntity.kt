package com.fitlogpro.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitlogpro.app.domain.model.SetType

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("workoutExerciseId")
    ]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val setType: SetType = SetType.NORMAL,
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val rpe: Double? = null,
    val durationSeconds: Int? = null,
    val distanceKm: Double? = null,
    val calories: Int? = null,
    val isCompleted: Boolean = true,
    val isPr: Boolean = false,
    val prTypesJson: String = "[]" // JSON array of PRType names
)
