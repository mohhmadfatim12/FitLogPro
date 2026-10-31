package com.fitlogpro.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitlogpro.app.data.backup.BackupManager
import com.fitlogpro.app.data.backup.BackupValidationResult
import com.fitlogpro.app.data.export.CsvExporter
import com.fitlogpro.app.data.local.preferences.UserPreferencesManager
import com.fitlogpro.app.data.repository.BodyMeasurementRepository
import com.fitlogpro.app.data.repository.ExerciseRepository
import com.fitlogpro.app.data.repository.WorkoutRepository
import com.fitlogpro.app.domain.model.DistanceUnit
import com.fitlogpro.app.domain.model.WeightUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesManager: UserPreferencesManager,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val measurementRepository: BodyMeasurementRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    val weightUnit: StateFlow<WeightUnit> = userPreferencesManager.weightUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUnit.KG)

    val distanceUnit: StateFlow<DistanceUnit> = userPreferencesManager.distanceUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DistanceUnit.KM)

    val darkTheme: StateFlow<Boolean> = userPreferencesManager.darkThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val keepScreenOn: StateFlow<Boolean> = userPreferencesManager.keepScreenOnFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val defaultRestSeconds: StateFlow<Int> = userPreferencesManager.defaultRestSecondsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 90)

    val restVibration: StateFlow<Boolean> = userPreferencesManager.restVibrationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val restSound: StateFlow<Boolean> = userPreferencesManager.restSoundFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch { userPreferencesManager.setWeightUnit(unit) }
    }

    fun setDistanceUnit(unit: DistanceUnit) {
        viewModelScope.launch { userPreferencesManager.setDistanceUnit(unit) }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { userPreferencesManager.setDarkTheme(enabled) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { userPreferencesManager.setKeepScreenOn(enabled) }
    }

    fun setDefaultRestSeconds(seconds: Int) {
        viewModelScope.launch { userPreferencesManager.setDefaultRestSeconds(seconds) }
    }

    fun setRestVibration(enabled: Boolean) {
        viewModelScope.launch { userPreferencesManager.setRestVibration(enabled) }
    }

    fun setRestSound(enabled: Boolean) {
        viewModelScope.launch { userPreferencesManager.setRestSound(enabled) }
    }

    suspend fun generateWorkoutsCsv(): String {
        val workouts = workoutRepository.getAllWorkoutsWithDetails().first()
        return CsvExporter.exportWorkoutsToCsv(workouts, weightUnit.value, distanceUnit.value)
    }

    suspend fun generateExercisesCsv(): String {
        val exercises = exerciseRepository.getAllExercises().first()
        return CsvExporter.exportExercisesToCsv(exercises)
    }

    suspend fun generateMeasurementsCsv(): String {
        val measurements = measurementRepository.getAllMeasurements().first()
        val isMetric = weightUnit.value == WeightUnit.KG
        return CsvExporter.exportMeasurementsToCsv(measurements, isMetric)
    }

    suspend fun createJsonBackup(): String {
        return backupManager.createBackupJson()
    }

    fun validateBackup(jsonString: String): BackupValidationResult {
        return backupManager.validateBackupJson(jsonString)
    }

    suspend fun restoreBackup(jsonString: String, overwrite: Boolean): Boolean {
        return backupManager.restoreBackup(jsonString, overwrite)
    }

    class Factory(
        private val userPreferencesManager: UserPreferencesManager,
        private val workoutRepository: WorkoutRepository,
        private val exerciseRepository: ExerciseRepository,
        private val measurementRepository: BodyMeasurementRepository,
        private val backupManager: BackupManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                userPreferencesManager,
                workoutRepository,
                exerciseRepository,
                measurementRepository,
                backupManager
            ) as T
        }
    }
}
