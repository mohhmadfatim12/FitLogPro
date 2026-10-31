package com.fitlogpro.app.ui.navigation

sealed class NavRoute(val route: String) {
    data object Routines : NavRoute("routines")
    data object RoutineDetail : NavRoute("routine_detail/{routineId}") {
        fun createRoute(routineId: Long) = "routine_detail/$routineId"
    }
    data object EditRoutine : NavRoute("edit_routine/{routineId}") {
        fun createRoute(routineId: Long) = "edit_routine/$routineId"
    }
    data object ActiveWorkout : NavRoute("active_workout")
    data object History : NavRoute("history")
    data object WorkoutDetail : NavRoute("workout_detail/{workoutId}") {
        fun createRoute(workoutId: Long) = "workout_detail/$workoutId"
    }
    data object EditWorkout : NavRoute("edit_workout/{workoutId}") {
        fun createRoute(workoutId: Long) = "edit_workout/$workoutId"
    }
    data object Exercises : NavRoute("exercises")
    data object ExerciseDetail : NavRoute("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: Long) = "exercise_detail/$exerciseId"
    }
    data object AddEditExercise : NavRoute("add_edit_exercise")
    data object Analytics : NavRoute("analytics?exerciseId={exerciseId}") {
        fun createRoute(exerciseId: Long? = null) = if (exerciseId != null) "analytics?exerciseId=$exerciseId" else "analytics"
    }
    data object Measurements : NavRoute("measurements")
    data object Settings : NavRoute("settings")
}
