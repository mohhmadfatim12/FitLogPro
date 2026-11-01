package com.fitlogpro.app.ui.activeworkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.data.local.preferences.UserPreferencesManager
import com.fitlogpro.app.data.repository.ActiveWorkoutRepository
import com.fitlogpro.app.data.repository.ExerciseRepository
import com.fitlogpro.app.data.repository.RoutineRepository
import com.fitlogpro.app.data.repository.WorkoutRepository
import com.fitlogpro.app.domain.calculator.ExistingExercisePrs
import com.fitlogpro.app.domain.calculator.PersonalRecordDetector
import com.fitlogpro.app.domain.model.ActiveWorkoutSession
import com.fitlogpro.app.domain.model.LiveWorkoutExercise
import com.fitlogpro.app.domain.model.LiveWorkoutSet
import com.fitlogpro.app.domain.model.SetType
import com.fitlogpro.app.domain.model.WeightUnit
import com.fitlogpro.app.domain.timer.RestTimerManager
import com.fitlogpro.app.domain.timer.RestTimerSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ActiveWorkoutViewModel(
    private val activeWorkoutRepository: ActiveWorkoutRepository,
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val restTimerManager: RestTimerManager
) : ViewModel() {

    private val _sessionState = MutableStateFlow<ActiveWorkoutSession?>(null)
    val sessionState: StateFlow<ActiveWorkoutSession?> = _sessionState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _restTimerState = MutableStateFlow(restTimerManager.getSnapshot())
    val restTimerState: StateFlow<RestTimerSnapshot> = _restTimerState.asStateFlow()

    private val _showRestTimerModal = MutableStateFlow(false)
    val showRestTimerModal: StateFlow<Boolean> = _showRestTimerModal.asStateFlow()

    val weightUnit: StateFlow<WeightUnit> = userPreferencesManager.weightUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUnit.KG)

    val keepScreenOn: StateFlow<Boolean> = userPreferencesManager.keepScreenOnFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private var timerJob: Job? = null
    private var restTimerJob: Job? = null

    init {
        // Check for existing active workout session (Process Recovery)
        viewModelScope.launch {
            val savedSession = activeWorkoutRepository.getActiveSession()
            if (savedSession != null) {
                _elapsedSeconds.value = savedSession.elapsedSeconds
                _sessionState.value = savedSession
                startElapsedTimer()
            }
        }
        startRestTimerTicker()
    }

    fun startBlankWorkout(customName: String = "Workout") {
        val now = System.currentTimeMillis()
        _elapsedSeconds.value = 0L
        val newSession = ActiveWorkoutSession(
            sessionId = "CURRENT_ACTIVE_SESSION",
            routineId = null,
            workoutName = customName,
            startTimeEpochMs = now,
            elapsedSeconds = 0,
            isPaused = false,
            exercises = emptyList()
        )
        updateSession(newSession)
        startElapsedTimer()
    }

    fun startWorkoutFromRoutine(routineId: Long) {
        viewModelScope.launch {
            val routine = routineRepository.getRoutineById(routineId)
            val routineName = routine?.name ?: "Workout"
            val items = routineRepository.getRoutineExerciseItems(routineId).first()
            val now = System.currentTimeMillis()
            _elapsedSeconds.value = 0L

            val liveExercises = items.sortedBy { it.crossRef.orderIndex }.map { item ->
                val prevPerf = loadPreviousPerformance(item.exercise.id)
                val targetSetsCount = if (item.crossRef.targetSets > 0) item.crossRef.targetSets else 3
                val sets = (1..targetSetsCount).map { setNum ->
                    LiveWorkoutSet(
                        id = UUID.randomUUID().toString(),
                        setNumber = setNum,
                        setType = SetType.NORMAL,
                        weightKg = item.crossRef.targetWeightKg ?: 0.0,
                        reps = item.crossRef.targetReps,
                        rpe = item.crossRef.targetRpe,
                        isCompleted = false,
                        previousPerformance = prevPerf
                    )
                }

                LiveWorkoutExercise(
                    id = UUID.randomUUID().toString(),
                    exerciseId = item.exercise.id,
                    exerciseName = item.exercise.name,
                    muscleGroup = item.exercise.muscleGroup,
                    equipment = item.exercise.equipment,
                    exerciseType = item.exercise.exerciseType,
                    defaultRestSeconds = item.exercise.defaultRestSeconds,
                    supersetGroupId = item.crossRef.supersetGroupId,
                    notes = item.crossRef.notes,
                    sets = sets
                )
            }

            val newSession = ActiveWorkoutSession(
                sessionId = "CURRENT_ACTIVE_SESSION",
                routineId = routineId,
                workoutName = routineName,
                startTimeEpochMs = now,
                elapsedSeconds = 0,
                isPaused = false,
                exercises = liveExercises
            )
            updateSession(newSession)
            startElapsedTimer()
        }
    }

    fun addExerciseToWorkout(exercise: ExerciseEntity) {
        val current = _sessionState.value ?: return
        viewModelScope.launch {
            val prevPerf = loadPreviousPerformance(exercise.id)
            val initialSet = LiveWorkoutSet(
                id = UUID.randomUUID().toString(),
                setNumber = 1,
                setType = SetType.NORMAL,
                weightKg = 0.0,
                reps = 10,
                isCompleted = false,
                previousPerformance = prevPerf
            )

            val liveExercise = LiveWorkoutExercise(
                id = UUID.randomUUID().toString(),
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                muscleGroup = exercise.muscleGroup,
                equipment = exercise.equipment,
                exerciseType = exercise.exerciseType,
                defaultRestSeconds = exercise.defaultRestSeconds,
                sets = listOf(initialSet)
            )

            val updated = current.copy(exercises = current.exercises + liveExercise)
            updateSession(updated)
        }
    }

    fun addSetToExercise(exerciseUniqueId: String) {
        val current = _sessionState.value ?: return
        val updatedExercises = current.exercises.map { ex ->
            if (ex.id == exerciseUniqueId) {
                val nextSetNum = ex.sets.size + 1
                val lastSet = ex.sets.lastOrNull()
                val newSet = LiveWorkoutSet(
                    id = UUID.randomUUID().toString(),
                    setNumber = nextSetNum,
                    setType = SetType.NORMAL,
                    weightKg = lastSet?.weightKg ?: 0.0,
                    reps = lastSet?.reps ?: 10,
                    rpe = lastSet?.rpe,
                    isCompleted = false,
                    previousPerformance = lastSet?.previousPerformance
                )
                ex.copy(sets = ex.sets + newSet)
            } else ex
        }
        updateSession(current.copy(exercises = updatedExercises))
    }

    fun removeSet(exerciseUniqueId: String, setUniqueId: String) {
        val current = _sessionState.value ?: return
        val updatedExercises = current.exercises.map { ex ->
            if (ex.id == exerciseUniqueId) {
                val filteredSets = ex.sets.filter { it.id != setUniqueId }
                    .mapIndexed { idx, s -> s.copy(setNumber = idx + 1) }
                ex.copy(sets = filteredSets)
            } else ex
        }
        updateSession(current.copy(exercises = updatedExercises))
    }

    fun removeExercise(exerciseUniqueId: String) {
        val current = _sessionState.value ?: return
        val updatedExercises = current.exercises.filter { it.id != exerciseUniqueId }
        updateSession(current.copy(exercises = updatedExercises))
    }

    fun updateSet(
        exerciseUniqueId: String,
        setUniqueId: String,
        weightKg: Double? = null,
        reps: Int? = null,
        rpe: Double? = null,
        setType: SetType? = null,
        durationSeconds: Int? = null,
        distanceKm: Double? = null
    ) {
        val current = _sessionState.value ?: return
        val updatedExercises = current.exercises.map { ex ->
            if (ex.id == exerciseUniqueId) {
                val updatedSets = ex.sets.map { s ->
                    if (s.id == setUniqueId) {
                        s.copy(
                            weightKg = weightKg ?: s.weightKg,
                            reps = reps ?: s.reps,
                            rpe = rpe ?: s.rpe,
                            setType = setType ?: s.setType,
                            durationSeconds = durationSeconds ?: s.durationSeconds,
                            distanceKm = distanceKm ?: s.distanceKm
                        )
                    } else s
                }
                ex.copy(sets = updatedSets)
            } else ex
        }
        updateSession(current.copy(exercises = updatedExercises))
    }

    fun toggleSetCompletion(exerciseUniqueId: String, setUniqueId: String) {
        val current = _sessionState.value ?: return
        viewModelScope.launch {
            val targetExercise = current.exercises.find { it.id == exerciseUniqueId } ?: return@launch
            val targetSet = targetExercise.sets.find { it.id == setUniqueId } ?: return@launch
            val willBeCompleted = !targetSet.isCompleted

            var isPr = false
            var brokenPrTypes = emptyList<com.fitlogpro.app.domain.model.PRType>()

            if (willBeCompleted && targetSet.setType.countsForPr) {
                // Check PR logic: strictly excludes warmups
                val existingPrs = loadExistingPrs(targetExercise.exerciseId)
                brokenPrTypes = PersonalRecordDetector.detectNewPrs(
                    weightKg = targetSet.weightKg,
                    reps = targetSet.reps,
                    setType = targetSet.setType,
                    existingPrs = existingPrs
                )
                isPr = brokenPrTypes.isNotEmpty()

                // Trigger Rest Timer
                val restSec = if (targetExercise.defaultRestSeconds > 0) targetExercise.defaultRestSeconds else 90
                restTimerManager.start(restSec, targetExercise.exerciseName)
                _restTimerState.value = restTimerManager.getSnapshot()
            }

            val updatedExercises = current.exercises.map { ex ->
                if (ex.id == exerciseUniqueId) {
                    val updatedSets = ex.sets.map { s ->
                        if (s.id == setUniqueId) {
                            s.copy(
                                isCompleted = willBeCompleted,
                                isPr = isPr,
                                prTypes = brokenPrTypes
                            )
                        } else s
                    }
                    ex.copy(sets = updatedSets)
                } else ex
            }
            updateSession(current.copy(exercises = updatedExercises))
        }
    }

    fun toggleWorkoutPause() {
        val current = _sessionState.value ?: return
        val isPaused = !current.isPaused
        val updated = current.copy(
            isPaused = isPaused,
            elapsedSeconds = _elapsedSeconds.value,
            lastPauseTimestampMs = if (isPaused) System.currentTimeMillis() else null
        )
        updateSession(updated)
    }

    fun updateWorkoutNotes(notes: String) {
        val current = _sessionState.value ?: return
        updateSession(current.copy(workoutNotes = notes))
    }

    fun finishWorkout(onFinished: (Long) -> Unit) {
        val current = _sessionState.value ?: return
        val finalDuration = _elapsedSeconds.value
        viewModelScope.launch {
            val endTime = System.currentTimeMillis()
            val workoutId = workoutRepository.saveCompletedWorkout(
                workoutName = current.workoutName,
                routineId = current.routineId,
                startTimeEpochMs = current.startTimeEpochMs,
                endTimeEpochMs = endTime,
                durationSeconds = finalDuration,
                workoutNotes = current.workoutNotes,
                exercises = current.exercises
            )
            // Clear active session from Room database
            activeWorkoutRepository.clearActiveSession()
            restTimerManager.stop()
            _sessionState.value = null
            timerJob?.cancel()
            onFinished(workoutId)
        }
    }

    fun discardWorkout(onDiscarded: () -> Unit) {
        viewModelScope.launch {
            activeWorkoutRepository.clearActiveSession()
            restTimerManager.stop()
            _sessionState.value = null
            timerJob?.cancel()
            onDiscarded()
        }
    }

    // Rest Timer Controls
    fun pauseRestTimer() {
        restTimerManager.pause()
        _restTimerState.value = restTimerManager.getSnapshot()
    }

    fun resumeRestTimer() {
        restTimerManager.resume()
        _restTimerState.value = restTimerManager.getSnapshot()
    }

    fun addRestTimerSeconds(seconds: Int) {
        restTimerManager.addTime(seconds)
        _restTimerState.value = restTimerManager.getSnapshot()
    }

    fun subtractRestTimerSeconds(seconds: Int) {
        restTimerManager.subtractTime(seconds)
        _restTimerState.value = restTimerManager.getSnapshot()
    }

    fun skipRestTimer() {
        restTimerManager.skip()
        _restTimerState.value = restTimerManager.getSnapshot()
    }

    fun setRestTimerModalVisible(visible: Boolean) {
        _showRestTimerModal.value = visible
    }

    private fun updateSession(session: ActiveWorkoutSession) {
        val withElapsed = session.copy(elapsedSeconds = _elapsedSeconds.value)
        _sessionState.value = withElapsed
        viewModelScope.launch {
            activeWorkoutRepository.saveActiveSession(withElapsed)
        }
    }

    private fun startElapsedTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _sessionState.value ?: break
                if (!current.isPaused) {
                    val newElapsed = _elapsedSeconds.value + 1
                    _elapsedSeconds.value = newElapsed
                    // Periodic save every 10 seconds to Room for crash recovery without modifying _sessionState
                    // This prevents re-rendering all exercise cards and sets every single second!
                    if (newElapsed % 10 == 0L) {
                        activeWorkoutRepository.saveActiveSession(current.copy(elapsedSeconds = newElapsed))
                    }
                }
            }
        }
    }

    private fun startRestTimerTicker() {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (true) {
                delay(500)
                val snap = restTimerManager.getSnapshot()
                if (snap != _restTimerState.value) {
                    _restTimerState.value = snap
                }
            }
        }
    }

    private suspend fun loadPreviousPerformance(exerciseId: Long): String? {
        val history = workoutRepository.getAllWorkoutsWithDetails().first()
        for (w in history) {
            val ex = w.exercises.find { it.workoutExercise.exerciseId == exerciseId }
            if (ex != null && ex.sets.isNotEmpty()) {
                val completedNonWarmups = ex.sets.filter { it.isCompleted && it.setType != SetType.WARMUP }
                val set = completedNonWarmups.lastOrNull() ?: ex.sets.lastOrNull()
                if (set != null && set.weightKg > 0) {
                    return "${set.weightKg}kg × ${set.reps}"
                }
            }
        }
        return null
    }

    private suspend fun loadExistingPrs(exerciseId: Long): ExistingExercisePrs {
        val history = workoutRepository.getAllWorkoutsWithDetails().first()
        var maxWeight = 0.0
        var max1Rm = 0.0
        var maxVol = 0.0
        var maxReps = 0

        history.forEach { w ->
            w.exercises.filter { it.workoutExercise.exerciseId == exerciseId }.forEach { we ->
                we.sets.filter { it.isCompleted && it.setType.countsForPr }.forEach { s ->
                    if (s.weightKg > maxWeight) maxWeight = s.weightKg
                    val rm = com.fitlogpro.app.domain.calculator.OneRepMaxCalculator.calculateEpley(s.weightKg, s.reps)
                    if (rm > max1Rm) max1Rm = rm
                    val vol = com.fitlogpro.app.domain.calculator.VolumeCalculator.calculateSetVolume(s.weightKg, s.reps, s.setType)
                    if (vol > maxVol) maxVol = vol
                    if (s.reps > maxReps) maxReps = s.reps
                }
            }
        }

        return ExistingExercisePrs(
            exerciseId = exerciseId,
            heaviestWeightKg = maxWeight,
            bestEstimated1RmKg = max1Rm,
            bestSetVolumeKg = maxVol,
            mostReps = maxReps
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restTimerJob?.cancel()
        val current = _sessionState.value
        if (current != null) {
            val withElapsed = current.copy(elapsedSeconds = _elapsedSeconds.value)
            viewModelScope.launch {
                activeWorkoutRepository.saveActiveSession(withElapsed)
            }
        }
    }

    class Factory(
        private val activeWorkoutRepository: ActiveWorkoutRepository,
        private val workoutRepository: WorkoutRepository,
        private val routineRepository: RoutineRepository,
        private val exerciseRepository: ExerciseRepository,
        private val userPreferencesManager: UserPreferencesManager,
        private val restTimerManager: RestTimerManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActiveWorkoutViewModel(
                activeWorkoutRepository,
                workoutRepository,
                routineRepository,
                exerciseRepository,
                userPreferencesManager,
                restTimerManager
            ) as T
        }
    }
}
