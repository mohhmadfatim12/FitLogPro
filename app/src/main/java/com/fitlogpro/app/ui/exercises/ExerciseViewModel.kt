package com.fitlogpro.app.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitlogpro.app.data.local.dao.PersonalRecordDao
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.data.local.entity.PersonalRecordEntity
import com.fitlogpro.app.data.repository.ExerciseRepository
import com.fitlogpro.app.domain.calculator.OneRepMaxCalculator
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.PRType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val personalRecordDao: PersonalRecordDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMuscle = MutableStateFlow<MuscleGroup?>(null)
    val selectedMuscle: StateFlow<MuscleGroup?> = _selectedMuscle.asStateFlow()

    private val _selectedEquipment = MutableStateFlow<Equipment?>(null)
    val selectedEquipment: StateFlow<Equipment?> = _selectedEquipment.asStateFlow()

    private val allExercises = exerciseRepository.getAllExercises()

    val filteredExercises: StateFlow<List<ExerciseEntity>> = combine(
        allExercises,
        _searchQuery,
        _selectedMuscle,
        _selectedEquipment
    ) { list, query, muscle, equip ->
        list.filter { ex ->
            val matchesQuery = query.isEmpty() || ex.name.contains(query, ignoreCase = true)
            val matchesMuscle = muscle == null || ex.muscleGroup == muscle
            val matchesEquip = equip == null || ex.equipment == equip
            matchesQuery && matchesMuscle && matchesEquip
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectMuscleGroup(muscleGroup: MuscleGroup?) {
        _selectedMuscle.value = if (_selectedMuscle.value == muscleGroup) null else muscleGroup
    }

    fun selectEquipment(equipment: Equipment?) {
        _selectedEquipment.value = if (_selectedEquipment.value == equipment) null else equipment
    }

    fun toggleFavorite(exercise: ExerciseEntity) {
        viewModelScope.launch {
            exerciseRepository.toggleFavorite(exercise.id, !exercise.isFavorite)
        }
    }

    fun saveCustomExercise(
        id: Long = 0L,
        name: String,
        muscleGroup: MuscleGroup,
        equipment: Equipment,
        exerciseType: ExerciseType,
        defaultRestSeconds: Int,
        instructions: String,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val entity = ExerciseEntity(
                id = id,
                name = name.trim(),
                muscleGroup = muscleGroup,
                equipment = equipment,
                exerciseType = exerciseType,
                defaultRestSeconds = defaultRestSeconds,
                isCustom = true,
                instructions = instructions.trim(),
                isFavorite = false
            )
            val newId = exerciseRepository.saveExercise(entity)
            onSuccess(newId)
        }
    }

    fun getPrsForExercise(exerciseId: Long): StateFlow<List<PersonalRecordEntity>> =
        personalRecordDao.getPrsForExercise(exerciseId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    class Factory(
        private val exerciseRepository: ExerciseRepository,
        private val personalRecordDao: PersonalRecordDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExerciseViewModel(exerciseRepository, personalRecordDao) as T
        }
    }
}
