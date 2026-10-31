package com.fitlogpro.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitlogpro.app.data.local.entity.WorkoutEntity
import com.fitlogpro.app.data.local.entity.WorkoutWithDetails
import com.fitlogpro.app.data.repository.AnalyticsRepository
import com.fitlogpro.app.data.repository.WorkoutRepository
import com.fitlogpro.app.domain.model.CalendarWorkoutDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryViewModel(
    private val workoutRepository: WorkoutRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    val workoutsWithDetails: StateFlow<List<WorkoutWithDetails>> = workoutRepository.getAllWorkoutsWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val cal = Calendar.getInstance()
    private val _selectedYear = MutableStateFlow(cal.get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(cal.get(Calendar.MONTH)) // 0-indexed
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val calendarDays: StateFlow<List<CalendarWorkoutDay>> = kotlinx.coroutines.flow.combine(
        _selectedYear,
        _selectedMonth
    ) { year, month -> Pair(year, month) }
        .flatMapLatest { (y, m) -> analyticsRepository.getCalendarDays(y, m) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun previousMonth() {
        if (_selectedMonth.value == 0) {
            _selectedMonth.value = 11
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
    }

    fun nextMonth() {
        if (_selectedMonth.value == 11) {
            _selectedMonth.value = 0
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workout)
        }
    }

    fun updateWorkoutNotes(workout: WorkoutEntity, newNotes: String) {
        viewModelScope.launch {
            workoutRepository.updateWorkout(workout.copy(notes = newNotes))
        }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val analyticsRepository: AnalyticsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(workoutRepository, analyticsRepository) as T
        }
    }
}
