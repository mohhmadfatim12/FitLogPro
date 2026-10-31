package com.fitlogpro.app.data.backup

import com.fitlogpro.app.data.local.database.FitLogDatabase
import com.fitlogpro.app.data.local.entity.BodyMeasurementEntity
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.data.local.entity.PersonalRecordEntity
import com.fitlogpro.app.data.local.entity.RoutineEntity
import com.fitlogpro.app.data.local.entity.RoutineExerciseCrossRef
import com.fitlogpro.app.data.local.entity.WorkoutEntity
import com.fitlogpro.app.data.local.entity.WorkoutExerciseEntity
import com.fitlogpro.app.data.local.entity.WorkoutSetEntity
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.MeasurementType
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.PRType
import com.fitlogpro.app.domain.model.SetType
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

sealed class BackupValidationResult {
    data class Valid(
        val version: Int,
        val exerciseCount: Int,
        val routineCount: Int,
        val workoutCount: Int,
        val measurementCount: Int
    ) : BackupValidationResult()

    data class Invalid(val reason: String) : BackupValidationResult()
}

class BackupManager(private val database: FitLogDatabase) {

    suspend fun createBackupJson(): String {
        val root = JSONObject()
        root.put("app", "FitLog Pro")
        root.put("version", 1)
        root.put("exportedAtEpochMs", System.currentTimeMillis())

        // 1. Exercises
        val exercises = database.exerciseDao().getAllExercises().first()
        val exercisesArray = JSONArray()
        for (e in exercises) {
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("name", e.name)
            obj.put("muscleGroup", e.muscleGroup.name)
            obj.put("equipment", e.equipment.name)
            obj.put("exerciseType", e.exerciseType.name)
            obj.put("defaultRestSeconds", e.defaultRestSeconds)
            obj.put("isCustom", e.isCustom)
            obj.put("instructions", e.instructions)
            obj.put("isFavorite", e.isFavorite)
            exercisesArray.put(obj)
        }
        root.put("exercises", exercisesArray)

        // 2. Routines & Routine Exercises
        val routines = database.routineDao().getAllRoutines().first()
        val routinesArray = JSONArray()
        for (r in routines) {
            val rObj = JSONObject()
            rObj.put("id", r.id)
            rObj.put("name", r.name)
            rObj.put("description", r.description)
            rObj.put("createdEpochMs", r.createdEpochMs)
            rObj.put("lastPerformedEpochMs", r.lastPerformedEpochMs ?: JSONObject.NULL)

            val items = database.routineDao().getRoutineExerciseItems(r.id).first()
            val itemsArray = JSONArray()
            for (item in items) {
                val iObj = JSONObject()
                iObj.put("exerciseId", item.crossRef.exerciseId)
                iObj.put("orderIndex", item.crossRef.orderIndex)
                iObj.put("targetSets", item.crossRef.targetSets)
                iObj.put("targetReps", item.crossRef.targetReps)
                iObj.put("targetWeightKg", item.crossRef.targetWeightKg ?: JSONObject.NULL)
                iObj.put("targetRpe", item.crossRef.targetRpe ?: JSONObject.NULL)
                iObj.put("supersetGroupId", item.crossRef.supersetGroupId ?: JSONObject.NULL)
                iObj.put("notes", item.crossRef.notes)
                itemsArray.put(iObj)
            }
            rObj.put("items", itemsArray)
            routinesArray.put(rObj)
        }
        root.put("routines", routinesArray)

        // 3. Workouts & Sets
        val workouts = database.workoutDao().getAllWorkoutsWithDetails().first()
        val workoutsArray = JSONArray()
        for (w in workouts) {
            val wObj = JSONObject()
            wObj.put("id", w.workout.id)
            wObj.put("name", w.workout.name)
            wObj.put("routineId", w.workout.routineId ?: JSONObject.NULL)
            wObj.put("startTimeEpochMs", w.workout.startTimeEpochMs)
            wObj.put("endTimeEpochMs", w.workout.endTimeEpochMs)
            wObj.put("durationSeconds", w.workout.durationSeconds)
            wObj.put("totalVolumeKg", w.workout.totalVolumeKg)
            wObj.put("notes", w.workout.notes)

            val exArray = JSONArray()
            for (we in w.exercises) {
                val weObj = JSONObject()
                weObj.put("exerciseId", we.workoutExercise.exerciseId)
                weObj.put("orderIndex", we.workoutExercise.orderIndex)
                weObj.put("supersetGroupId", we.workoutExercise.supersetGroupId ?: JSONObject.NULL)
                weObj.put("notes", we.workoutExercise.notes)

                val setsArray = JSONArray()
                for (s in we.sets) {
                    val sObj = JSONObject()
                    sObj.put("setNumber", s.setNumber)
                    sObj.put("setType", s.setType.name)
                    sObj.put("weightKg", s.weightKg)
                    sObj.put("reps", s.reps)
                    sObj.put("rpe", s.rpe ?: JSONObject.NULL)
                    sObj.put("durationSeconds", s.durationSeconds ?: JSONObject.NULL)
                    sObj.put("distanceKm", s.distanceKm ?: JSONObject.NULL)
                    sObj.put("calories", s.calories ?: JSONObject.NULL)
                    sObj.put("isCompleted", s.isCompleted)
                    sObj.put("isPr", s.isPr)
                    sObj.put("prTypesJson", s.prTypesJson)
                    setsArray.put(sObj)
                }
                weObj.put("sets", setsArray)
                exArray.put(weObj)
            }
            wObj.put("exercises", exArray)
            workoutsArray.put(wObj)
        }
        root.put("workouts", workoutsArray)

        // 4. Personal Records
        val prs = database.personalRecordDao().getAllPrs().first()
        val prsArray = JSONArray()
        for (pr in prs) {
            val prObj = JSONObject()
            prObj.put("exerciseId", pr.exerciseId)
            prObj.put("prType", pr.prType.name)
            prObj.put("value", pr.value)
            prObj.put("achievedAtEpochMs", pr.achievedAtEpochMs)
            prObj.put("notes", pr.notes)
            prsArray.put(prObj)
        }
        root.put("personalRecords", prsArray)

        // 5. Body Measurements
        val measurements = database.bodyMeasurementDao().getAllMeasurements().first()
        val measArray = JSONArray()
        for (m in measurements) {
            val mObj = JSONObject()
            mObj.put("measurementType", m.measurementType.name)
            mObj.put("value", m.value)
            mObj.put("recordedEpochMs", m.recordedEpochMs)
            mObj.put("notes", m.notes)
            measArray.put(mObj)
        }
        root.put("bodyMeasurements", measArray)

        return root.toString(2)
    }

    fun validateBackupJson(jsonString: String): BackupValidationResult {
        return try {
            val root = JSONObject(jsonString)
            val app = root.optString("app")
            if (app != "FitLog Pro") {
                return BackupValidationResult.Invalid("Invalid backup source: expected 'FitLog Pro' but found '$app'")
            }

            val version = root.optInt("version", -1)
            if (version < 1) {
                return BackupValidationResult.Invalid("Unsupported backup version: $version")
            }

            val exercises = root.optJSONArray("exercises") ?: JSONArray()
            val routines = root.optJSONArray("routines") ?: JSONArray()
            val workouts = root.optJSONArray("workouts") ?: JSONArray()
            val measurements = root.optJSONArray("bodyMeasurements") ?: JSONArray()

            BackupValidationResult.Valid(
                version = version,
                exerciseCount = exercises.length(),
                routineCount = routines.length(),
                workoutCount = workouts.length(),
                measurementCount = measurements.length()
            )
        } catch (e: Exception) {
            BackupValidationResult.Invalid("Malformed JSON payload: ${e.message}")
        }
    }

    suspend fun restoreBackup(jsonString: String, overwriteExisting: Boolean = false): Boolean {
        val validation = validateBackupJson(jsonString)
        if (validation !is BackupValidationResult.Valid) return false

        return try {
            val root = JSONObject(jsonString)

            // 1. Exercises
            val exercisesArray = root.optJSONArray("exercises") ?: JSONArray()
            for (i in 0 until exercisesArray.length()) {
                val obj = exercisesArray.getJSONObject(i)
                val exercise = ExerciseEntity(
                    id = if (overwriteExisting) obj.optLong("id") else 0L,
                    name = obj.getString("name"),
                    muscleGroup = MuscleGroup.fromString(obj.getString("muscleGroup")),
                    equipment = Equipment.fromString(obj.getString("equipment")),
                    exerciseType = ExerciseType.fromString(obj.getString("exerciseType")),
                    defaultRestSeconds = obj.optInt("defaultRestSeconds", 90),
                    isCustom = obj.optBoolean("isCustom", true),
                    instructions = obj.optString("instructions", ""),
                    isFavorite = obj.optBoolean("isFavorite", false)
                )
                database.exerciseDao().insertExercise(exercise)
            }

            // 2. Routines
            val routinesArray = root.optJSONArray("routines") ?: JSONArray()
            for (i in 0 until routinesArray.length()) {
                val rObj = routinesArray.getJSONObject(i)
                val routine = RoutineEntity(
                    id = if (overwriteExisting) rObj.optLong("id") else 0L,
                    name = rObj.getString("name"),
                    description = rObj.optString("description", ""),
                    createdEpochMs = rObj.optLong("createdEpochMs", System.currentTimeMillis()),
                    lastPerformedEpochMs = if (rObj.isNull("lastPerformedEpochMs")) null else rObj.optLong("lastPerformedEpochMs")
                )
                val routineId = database.routineDao().insertRoutine(routine)

                val itemsArray = rObj.optJSONArray("items") ?: JSONArray()
                val crossRefs = mutableListOf<RoutineExerciseCrossRef>()
                for (j in 0 until itemsArray.length()) {
                    val iObj = itemsArray.getJSONObject(j)
                    crossRefs.add(
                        RoutineExerciseCrossRef(
                            id = 0L,
                            routineId = routineId,
                            exerciseId = iObj.getLong("exerciseId"),
                            orderIndex = iObj.getInt("orderIndex"),
                            targetSets = iObj.optInt("targetSets", 3),
                            targetReps = iObj.optInt("targetReps", 10),
                            targetWeightKg = if (iObj.isNull("targetWeightKg")) null else iObj.optDouble("targetWeightKg"),
                            targetRpe = if (iObj.isNull("targetRpe")) null else iObj.optDouble("targetRpe"),
                            supersetGroupId = if (iObj.isNull("supersetGroupId")) null else iObj.optString("supersetGroupId"),
                            notes = iObj.optString("notes", "")
                        )
                    )
                }
                database.routineDao().insertRoutineExercises(crossRefs)
            }

            // 3. Workouts
            val workoutsArray = root.optJSONArray("workouts") ?: JSONArray()
            for (i in 0 until workoutsArray.length()) {
                val wObj = workoutsArray.getJSONObject(i)
                val workout = WorkoutEntity(
                    id = if (overwriteExisting) wObj.optLong("id") else 0L,
                    name = wObj.getString("name"),
                    routineId = if (wObj.isNull("routineId")) null else wObj.optLong("routineId"),
                    startTimeEpochMs = wObj.getLong("startTimeEpochMs"),
                    endTimeEpochMs = wObj.getLong("endTimeEpochMs"),
                    durationSeconds = wObj.getLong("durationSeconds"),
                    totalVolumeKg = wObj.optDouble("totalVolumeKg", 0.0),
                    notes = wObj.optString("notes", "")
                )
                val workoutId = database.workoutDao().insertWorkout(workout)

                val exArray = wObj.optJSONArray("exercises") ?: JSONArray()
                for (j in 0 until exArray.length()) {
                    val weObj = exArray.getJSONObject(j)
                    val workoutExercise = WorkoutExerciseEntity(
                        id = 0L,
                        workoutId = workoutId,
                        exerciseId = weObj.getLong("exerciseId"),
                        orderIndex = weObj.getInt("orderIndex"),
                        supersetGroupId = if (weObj.isNull("supersetGroupId")) null else weObj.optString("supersetGroupId"),
                        notes = weObj.optString("notes", "")
                    )
                    val weId = database.workoutDao().insertWorkoutExercise(workoutExercise)

                    val setsArray = weObj.optJSONArray("sets") ?: JSONArray()
                    val setsList = mutableListOf<WorkoutSetEntity>()
                    for (k in 0 until setsArray.length()) {
                        val sObj = setsArray.getJSONObject(k)
                        setsList.add(
                            WorkoutSetEntity(
                                id = 0L,
                                workoutExerciseId = weId,
                                setNumber = sObj.getInt("setNumber"),
                                setType = SetType.fromString(sObj.optString("setType")),
                                weightKg = sObj.optDouble("weightKg", 0.0),
                                reps = sObj.optInt("reps", 0),
                                rpe = if (sObj.isNull("rpe")) null else sObj.optDouble("rpe"),
                                durationSeconds = if (sObj.isNull("durationSeconds")) null else sObj.optInt("durationSeconds"),
                                distanceKm = if (sObj.isNull("distanceKm")) null else sObj.optDouble("distanceKm"),
                                calories = if (sObj.isNull("calories")) null else sObj.optInt("calories"),
                                isCompleted = sObj.optBoolean("isCompleted", true),
                                isPr = sObj.optBoolean("isPr", false),
                                prTypesJson = sObj.optString("prTypesJson", "[]")
                            )
                        )
                    }
                    database.workoutDao().insertWorkoutSets(setsList)
                }
            }

            // 4. Personal Records
            val prsArray = root.optJSONArray("personalRecords") ?: JSONArray()
            val prList = mutableListOf<PersonalRecordEntity>()
            for (i in 0 until prsArray.length()) {
                val prObj = prsArray.getJSONObject(i)
                prList.add(
                    PersonalRecordEntity(
                        id = 0L,
                        exerciseId = prObj.getLong("exerciseId"),
                        prType = PRType.fromString(prObj.getString("prType")),
                        value = prObj.getDouble("value"),
                        achievedAtEpochMs = prObj.getLong("achievedAtEpochMs"),
                        notes = prObj.optString("notes", "")
                    )
                )
            }
            database.personalRecordDao().insertAll(prList)

            // 5. Body Measurements
            val measArray = root.optJSONArray("bodyMeasurements") ?: JSONArray()
            val mList = mutableListOf<BodyMeasurementEntity>()
            for (i in 0 until measArray.length()) {
                val mObj = measArray.getJSONObject(i)
                mList.add(
                    BodyMeasurementEntity(
                        id = 0L,
                        measurementType = MeasurementType.fromString(mObj.getString("measurementType")),
                        value = mObj.getDouble("value"),
                        recordedEpochMs = mObj.getLong("recordedEpochMs"),
                        notes = mObj.optString("notes", "")
                    )
                )
            }
            database.bodyMeasurementDao().insertAll(mList)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
