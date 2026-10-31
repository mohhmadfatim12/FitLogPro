package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.PersonalRecordDao
import com.fitlogpro.app.data.local.dao.RoutineDao
import com.fitlogpro.app.data.local.dao.WorkoutDao
import com.fitlogpro.app.data.local.entity.PersonalRecordEntity
import com.fitlogpro.app.data.local.entity.WorkoutEntity
import com.fitlogpro.app.data.local.entity.WorkoutExerciseEntity
import com.fitlogpro.app.data.local.entity.WorkoutSetEntity
import com.fitlogpro.app.data.local.entity.WorkoutWithDetails
import com.fitlogpro.app.domain.calculator.OneRepMaxCalculator
import com.fitlogpro.app.domain.calculator.VolumeCalculator
import com.fitlogpro.app.domain.model.LiveWorkoutExercise
import com.fitlogpro.app.domain.model.PRType
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val personalRecordDao: PersonalRecordDao,
    private val routineDao: RoutineDao
) {

    fun getAllWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    fun getAllWorkoutsWithDetails(): Flow<List<WorkoutWithDetails>> =
        workoutDao.getAllWorkoutsWithDetails()

    fun getWorkoutWithDetails(workoutId: Long): Flow<WorkoutWithDetails?> =
        workoutDao.getWorkoutWithDetails(workoutId)

    suspend fun getWorkoutById(workoutId: Long): WorkoutEntity? =
        workoutDao.getWorkoutById(workoutId)

    fun getWorkoutsBetweenDates(startEpochMs: Long, endEpochMs: Long): Flow<List<WorkoutEntity>> =
        workoutDao.getWorkoutsBetweenDates(startEpochMs, endEpochMs)

    suspend fun getAllWorkoutTimestamps(): List<Long> = workoutDao.getAllWorkoutTimestamps()

    fun getAllWorkoutTimestampsFlow(): Flow<List<Long>> = workoutDao.getAllWorkoutTimestampsFlow()

    suspend fun saveCompletedWorkout(
        workoutName: String,
        routineId: Long?,
        startTimeEpochMs: Long,
        endTimeEpochMs: Long,
        durationSeconds: Long,
        workoutNotes: String,
        exercises: List<LiveWorkoutExercise>
    ): Long {
        // 1. Calculate workout volume strictly excluding warm-up sets
        val allCompletedSets = exercises.flatMap { it.sets }.filter { it.isCompleted }
        val totalVolumeKg = VolumeCalculator.calculateTotalWorkoutVolume(allCompletedSets)

        val workoutEntity = WorkoutEntity(
            id = 0L,
            name = workoutName,
            routineId = routineId,
            startTimeEpochMs = startTimeEpochMs,
            endTimeEpochMs = endTimeEpochMs,
            durationSeconds = durationSeconds,
            totalVolumeKg = totalVolumeKg,
            notes = workoutNotes
        )

        val workoutId = workoutDao.insertWorkout(workoutEntity)

        // 2. Insert exercises and sets
        exercises.forEachIndexed { exIndex, liveExercise ->
            val weEntity = WorkoutExerciseEntity(
                id = 0L,
                workoutId = workoutId,
                exerciseId = liveExercise.exerciseId,
                orderIndex = exIndex,
                supersetGroupId = liveExercise.supersetGroupId,
                notes = liveExercise.notes
            )
            val weId = workoutDao.insertWorkoutExercise(weEntity)

            val setsToInsert = liveExercise.sets.map { liveSet ->
                val prTypesJson = if (liveSet.prTypes.isNotEmpty()) {
                    val array = JSONArray()
                    liveSet.prTypes.forEach { array.put(it.name) }
                    array.toString()
                } else "[]"

                WorkoutSetEntity(
                    id = 0L,
                    workoutExerciseId = weId,
                    setNumber = liveSet.setNumber,
                    setType = liveSet.setType,
                    weightKg = liveSet.weightKg,
                    reps = liveSet.reps,
                    rpe = liveSet.rpe,
                    durationSeconds = liveSet.durationSeconds,
                    distanceKm = liveSet.distanceKm,
                    isCompleted = liveSet.isCompleted,
                    isPr = liveSet.isPr,
                    prTypesJson = prTypesJson
                )
            }
            workoutDao.insertWorkoutSets(setsToInsert)

            // 3. Persist broken PR records if any
            liveExercise.sets.filter { it.isCompleted && it.isPr && it.setType.countsForPr }.forEach { set ->
                set.prTypes.forEach { prType ->
                    val prValue = when (prType) {
                        PRType.HEAVIEST_WEIGHT -> set.weightKg
                        PRType.BEST_1RM -> OneRepMaxCalculator.calculateEpley(set.weightKg, set.reps)
                        PRType.BEST_SET_VOLUME -> VolumeCalculator.calculateSetVolume(set.weightKg, set.reps, set.setType)
                        PRType.MOST_REPS -> set.reps.toDouble()
                    }
                    personalRecordDao.insertPr(
                        PersonalRecordEntity(
                            id = 0L,
                            exerciseId = liveExercise.exerciseId,
                            prType = prType,
                            value = prValue,
                            achievedAtEpochMs = endTimeEpochMs,
                            workoutId = workoutId,
                            notes = "${liveExercise.exerciseName} - ${set.weightKg}kg x ${set.reps}"
                        )
                    )
                }
            }
        }

        // 4. Update routine last performed if started from a routine
        if (routineId != null) {
            routineDao.updateLastPerformed(routineId, endTimeEpochMs)
        }

        return workoutId
    }

    suspend fun updateWorkout(workout: WorkoutEntity) = workoutDao.updateWorkout(workout)

    suspend fun deleteWorkout(workout: WorkoutEntity) = workoutDao.deleteWorkout(workout)
}
