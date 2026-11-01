package com.fitlogpro.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.fitlogpro.app.di.AppContainer
import com.fitlogpro.app.ui.activeworkout.ActiveWorkoutScreen
import com.fitlogpro.app.ui.activeworkout.ActiveWorkoutViewModel
import com.fitlogpro.app.ui.analytics.AnalyticsScreen
import com.fitlogpro.app.ui.analytics.AnalyticsViewModel
import com.fitlogpro.app.ui.exercises.AddEditExerciseScreen
import com.fitlogpro.app.ui.exercises.ExerciseDetailScreen
import com.fitlogpro.app.ui.exercises.ExerciseListScreen
import com.fitlogpro.app.ui.exercises.ExerciseViewModel
import com.fitlogpro.app.ui.history.EditWorkoutScreen
import com.fitlogpro.app.ui.history.HistoryScreen
import com.fitlogpro.app.ui.history.HistoryViewModel
import com.fitlogpro.app.ui.history.WorkoutDetailScreen
import com.fitlogpro.app.ui.measurements.BodyMeasurementScreen
import com.fitlogpro.app.ui.measurements.BodyMeasurementViewModel
import com.fitlogpro.app.ui.settings.SettingsScreen
import com.fitlogpro.app.ui.settings.SettingsViewModel
import com.fitlogpro.app.ui.workouts.EditRoutineScreen
import com.fitlogpro.app.ui.workouts.RoutineDetailScreen
import com.fitlogpro.app.ui.workouts.RoutineListScreen
import com.fitlogpro.app.ui.workouts.RoutinesViewModel

@Composable
fun FitLogNavGraph(
    navController: NavHostController,
    container: AppContainer
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Scoped Shared ViewModels
    val routinesViewModel: RoutinesViewModel = viewModel(
        factory = RoutinesViewModel.Factory(container.routineRepository, container.exerciseRepository)
    )
    val activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel(
        factory = ActiveWorkoutViewModel.Factory(
            container.activeWorkoutRepository,
            container.workoutRepository,
            container.routineRepository,
            container.exerciseRepository,
            container.userPreferencesManager,
            container.restTimerManager
        )
    )
    val exerciseViewModel: ExerciseViewModel = viewModel(
        factory = ExerciseViewModel.Factory(container.exerciseRepository, container.database.personalRecordDao())
    )
    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.Factory(container.workoutRepository, container.analyticsRepository)
    )
    val analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModel.Factory(container.analyticsRepository, container.exerciseRepository)
    )
    val measurementViewModel: BodyMeasurementViewModel = viewModel(
        factory = BodyMeasurementViewModel.Factory(container.bodyMeasurementRepository, container.userPreferencesManager)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            container.userPreferencesManager,
            container.workoutRepository,
            container.exerciseRepository,
            container.bodyMeasurementRepository,
            container.backupManager
        )
    )

    val activeSession by activeWorkoutViewModel.sessionState.collectAsState()
    val isFullscreenScreen = currentRoute == NavRoute.ActiveWorkout.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!isFullscreenScreen) {
                FitLogBottomBar(
                    currentRoute = currentRoute,
                    onNavigateToTab = { route ->
                        navController.navigate(route) {
                            popUpTo(NavRoute.Routines.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Routines.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Routines List (Home / Workouts tab)
            composable(NavRoute.Routines.route) {
                RoutineListScreen(
                    viewModel = routinesViewModel,
                    activeSession = activeSession,
                    onStartEmptyWorkout = {
                        activeWorkoutViewModel.startBlankWorkout()
                        navController.navigate(NavRoute.ActiveWorkout.route)
                    },
                    onResumeActiveWorkout = {
                        navController.navigate(NavRoute.ActiveWorkout.route)
                    },
                    onStartRoutine = { routineId ->
                        activeWorkoutViewModel.startWorkoutFromRoutine(routineId)
                        navController.navigate(NavRoute.ActiveWorkout.route)
                    },
                    onRoutineDetail = { routineId ->
                        navController.navigate(NavRoute.RoutineDetail.createRoute(routineId))
                    },
                    onCreateRoutine = {
                        navController.navigate(NavRoute.EditRoutine.createRoute(0L))
                    }
                )
            }

            // 2. Routine Detail Screen
            composable(
                route = NavRoute.RoutineDetail.route,
                arguments = listOf(navArgument("routineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
                RoutineDetailScreen(
                    routineId = routineId,
                    viewModel = routinesViewModel,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(NavRoute.EditRoutine.createRoute(id)) },
                    onStartWorkout = { id ->
                        activeWorkoutViewModel.startWorkoutFromRoutine(id)
                        navController.navigate(NavRoute.ActiveWorkout.route)
                    }
                )
            }

            // 3. Edit / Create Routine Screen
            composable(
                route = NavRoute.EditRoutine.route,
                arguments = listOf(navArgument("routineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
                EditRoutineScreen(
                    routineId = routineId,
                    viewModel = routinesViewModel,
                    exerciseRepository = container.exerciseRepository,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            // 4. Active Workout Screen
            composable(NavRoute.ActiveWorkout.route) {
                ActiveWorkoutScreen(
                    viewModel = activeWorkoutViewModel,
                    exerciseRepository = container.exerciseRepository,
                    onBack = { navController.popBackStack() },
                    onWorkoutFinished = { workoutId ->
                        navController.navigate(NavRoute.WorkoutDetail.createRoute(workoutId)) {
                            popUpTo(NavRoute.Routines.route)
                        }
                    }
                )
            }

            // 5. History & Calendar Screen
            composable(NavRoute.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onWorkoutClick = { workoutId ->
                        navController.navigate(NavRoute.WorkoutDetail.createRoute(workoutId))
                    }
                )
            }

            // 6. Workout Detail Screen
            composable(
                route = NavRoute.WorkoutDetail.route,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
                WorkoutDetailScreen(
                    workoutId = workoutId,
                    viewModel = historyViewModel,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(NavRoute.EditWorkout.createRoute(id)) }
                )
            }

            // 7. Edit Completed Workout Screen
            composable(
                route = NavRoute.EditWorkout.route,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
                EditWorkoutScreen(
                    workoutId = workoutId,
                    viewModel = historyViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 8. Exercise Library Screen
            composable(NavRoute.Exercises.route) {
                ExerciseListScreen(
                    viewModel = exerciseViewModel,
                    onExerciseClick = { exId ->
                        navController.navigate(NavRoute.ExerciseDetail.createRoute(exId))
                    },
                    onCreateExercise = {
                        navController.navigate(NavRoute.AddEditExercise.route)
                    }
                )
            }

            // 9. Exercise Detail Screen
            composable(
                route = NavRoute.ExerciseDetail.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: 0L
                ExerciseDetailScreen(
                    exerciseId = exerciseId,
                    viewModel = exerciseViewModel,
                    onBack = { navController.popBackStack() },
                    onViewCharts = { id ->
                        navController.navigate(NavRoute.Analytics.createRoute(id))
                    }
                )
            }

            // 10. Add/Edit Custom Exercise Screen
            composable(NavRoute.AddEditExercise.route) {
                AddEditExerciseScreen(
                    viewModel = exerciseViewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            // 11. Analytics Screen
            composable(
                route = NavRoute.Analytics.route,
                arguments = listOf(navArgument("exerciseId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val exIdStr = backStackEntry.arguments?.getString("exerciseId")
                val exId = exIdStr?.toLongOrNull()
                if (exId != null) {
                    analyticsViewModel.selectExercise(exId)
                }
                AnalyticsScreen(
                    viewModel = analyticsViewModel
                )
            }

            // 12. Body Measurements Screen
            composable(NavRoute.Measurements.route) {
                BodyMeasurementScreen(viewModel = measurementViewModel)
            }

            // 13. Settings Screen
            composable(NavRoute.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
