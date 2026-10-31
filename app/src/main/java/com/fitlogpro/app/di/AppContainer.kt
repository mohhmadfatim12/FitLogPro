package com.fitlogpro.app.di

import android.content.Context
import com.fitlogpro.app.data.backup.BackupManager
import com.fitlogpro.app.data.local.database.FitLogDatabase
import com.fitlogpro.app.data.local.preferences.UserPreferencesManager
import com.fitlogpro.app.data.repository.ActiveWorkoutRepository
import com.fitlogpro.app.data.repository.AnalyticsRepository
import com.fitlogpro.app.data.repository.BodyMeasurementRepository
import com.fitlogpro.app.data.repository.ExerciseRepository
import com.fitlogpro.app.data.repository.RoutineRepository
import com.fitlogpro.app.data.repository.WorkoutRepository
import com.fitlogpro.app.domain.timer.RestTimerManager

interface AppContainer {
    val database: FitLogDatabase
    val exerciseRepository: ExerciseRepository
    val routineRepository: RoutineRepository
    val workoutRepository: WorkoutRepository
    val activeWorkoutRepository: ActiveWorkoutRepository
    val analyticsRepository: AnalyticsRepository
    val bodyMeasurementRepository: BodyMeasurementRepository
    val userPreferencesManager: UserPreferencesManager
    val backupManager: BackupManager
    val restTimerManager: RestTimerManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: FitLogDatabase by lazy {
        FitLogDatabase.getInstance(context)
    }

    override val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepository(database.exerciseDao())
    }

    override val routineRepository: RoutineRepository by lazy {
        RoutineRepository(database.routineDao())
    }

    override val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(
            database.workoutDao(),
            database.personalRecordDao(),
            database.routineDao()
        )
    }

    override val activeWorkoutRepository: ActiveWorkoutRepository by lazy {
        ActiveWorkoutRepository(database.activeWorkoutDao())
    }

    override val analyticsRepository: AnalyticsRepository by lazy {
        AnalyticsRepository(database.workoutDao())
    }

    override val bodyMeasurementRepository: BodyMeasurementRepository by lazy {
        BodyMeasurementRepository(database.bodyMeasurementDao())
    }

    override val userPreferencesManager: UserPreferencesManager by lazy {
        UserPreferencesManager(context)
    }

    override val backupManager: BackupManager by lazy {
        BackupManager(database)
    }

    override val restTimerManager: RestTimerManager by lazy {
        RestTimerManager()
    }
}
