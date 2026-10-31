package com.fitlogpro.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.MuscleGroup

@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["name"], unique = false),
        Index(value = ["muscleGroup"]),
        Index(value = ["isCustom"])
    ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipment: Equipment,
    val exerciseType: ExerciseType,
    val defaultRestSeconds: Int = 90,
    val isCustom: Boolean = false,
    val instructions: String = "",
    val isFavorite: Boolean = false
)
