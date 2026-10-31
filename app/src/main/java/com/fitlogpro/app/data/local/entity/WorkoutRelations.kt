package com.fitlogpro.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RoutineExerciseItem(
    @Embedded
    val crossRef: RoutineExerciseCrossRef,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity
)

data class RoutineWithExercises(
    @Embedded
    val routine: RoutineEntity,
    @Relation(
        entity = RoutineExerciseCrossRef::class,
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val exerciseRefs: List<RoutineExerciseCrossRef>
)

data class WorkoutExerciseWithSets(
    @Embedded
    val workoutExercise: WorkoutExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutExerciseId"
    )
    val sets: List<WorkoutSetEntity>
)

data class WorkoutWithDetails(
    @Embedded
    val workout: WorkoutEntity,
    @Relation(
        entity = WorkoutExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExerciseWithSets>
)
