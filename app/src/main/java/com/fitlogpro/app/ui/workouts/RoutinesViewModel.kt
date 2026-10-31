package com.fitlogpro.app.ui.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitlogpro.app.data.local.entity.RoutineEntity
import com.fitlogpro.app.data.local.entity.RoutineExerciseCrossRef
import com.fitlogpro.app.data.local.entity.RoutineExerciseItem
import com.fitlogpro.app.data.repository.ExerciseRepository
import com.fitlogpro.app.data.repository.RoutineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutinesViewModel(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    val routines: StateFlow<List<RoutineEntity>> = routineRepository.getAllRoutines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getRoutineItems(routineId: Long): StateFlow<List<RoutineExerciseItem>> =
        routineRepository.getRoutineExerciseItems(routineId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routine)
        }
    }

    fun saveRoutine(
        id: Long,
        name: String,
        description: String,
        exercises: List<RoutineExerciseCrossRef>,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val routine = RoutineEntity(
                id = id,
                name = name,
                description = description
            )
            val routineId = if (id == 0L) {
                routineRepository.createRoutine(routine, exercises)
            } else {
                routineRepository.updateRoutine(routine, exercises)
                id
            }
            onSuccess(routineId)
        }
    }

    class Factory(
        private val routineRepository: RoutineRepository,
        private val exerciseRepository: ExerciseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoutinesViewModel(routineRepository, exerciseRepository) as T
        }
    }
}
