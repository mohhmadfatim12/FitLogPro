package com.fitlogpro.app.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.data.repository.AnalyticsRepository
import com.fitlogpro.app.data.repository.ExerciseRepository
import com.fitlogpro.app.domain.model.ExerciseProgressPoint
import com.fitlogpro.app.domain.model.MuscleGroupDistribution
import com.fitlogpro.app.domain.model.VolumeHistoryPoint
import com.fitlogpro.app.domain.model.WorkoutStreakInfo
import com.fitlogpro.app.ui.components.BarChartItem
import com.fitlogpro.app.ui.components.ChartDataPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ChartMetric(val title: String, val unitSuffix: String) {
    MAX_WEIGHT("Max Wt", "kg"),
    ESTIMATED_1RM("1RM", "kg"),
    TOTAL_VOLUME("Volume", "kg"),
    MAX_REPS("Reps", "reps")
}

class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    val streakInfo: StateFlow<WorkoutStreakInfo> = analyticsRepository.getWorkoutStreakInfo()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            WorkoutStreakInfo(0, 0, 0, 0, 0.0, 0L)
        )

    val weeklyVolumeBars: StateFlow<List<BarChartItem>> = analyticsRepository.getWeeklyVolumeHistory()
        .map { points ->
            points.takeLast(8).map {
                BarChartItem(
                    label = it.periodLabel.take(6),
                    value = it.totalVolumeKg,
                    subtext = "${it.workoutCount} workouts"
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val muscleDistribution: StateFlow<List<MuscleGroupDistribution>> = analyticsRepository.getMuscleGroupDistribution()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<ExerciseEntity>> = exerciseRepository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedExerciseId = MutableStateFlow<Long?>(null)
    val selectedExerciseId: StateFlow<Long?> = _selectedExerciseId.asStateFlow()

    private val _selectedMetric = MutableStateFlow(ChartMetric.MAX_WEIGHT)
    val selectedMetric: StateFlow<ChartMetric> = _selectedMetric.asStateFlow()

    private val _exerciseProgressPoints = MutableStateFlow<List<ExerciseProgressPoint>>(emptyList())
    val exerciseProgressPoints: StateFlow<List<ExerciseProgressPoint>> = _exerciseProgressPoints.asStateFlow()

    fun selectExercise(exerciseId: Long) {
        _selectedExerciseId.value = exerciseId
        loadProgress(exerciseId)
    }

    fun selectMetric(metric: ChartMetric) {
        _selectedMetric.value = metric
    }

    fun getChartDataPoints(): List<ChartDataPoint> {
        val metric = _selectedMetric.value
        return _exerciseProgressPoints.value.map { pt ->
            val value = when (metric) {
                ChartMetric.MAX_WEIGHT -> pt.maxWeightKg
                ChartMetric.ESTIMATED_1RM -> pt.estimated1RmKg
                ChartMetric.TOTAL_VOLUME -> pt.totalVolumeKg
                ChartMetric.MAX_REPS -> pt.maxReps.toDouble()
            }
            ChartDataPoint(label = pt.dateLabel, value = value)
        }
    }

    private fun loadProgress(exerciseId: Long) {
        viewModelScope.launch {
            val progress = analyticsRepository.getExerciseProgress(exerciseId)
            _exerciseProgressPoints.value = progress
        }
    }

    class Factory(
        private val analyticsRepository: AnalyticsRepository,
        private val exerciseRepository: ExerciseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnalyticsViewModel(analyticsRepository, exerciseRepository) as T
        }
    }
}
