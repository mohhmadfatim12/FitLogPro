package com.fitlogpro.app.data.repository

import com.fitlogpro.app.data.local.dao.ActiveWorkoutDao
import com.fitlogpro.app.data.local.entity.ActiveWorkoutSessionEntity
import com.fitlogpro.app.domain.model.ActiveWorkoutSession
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.LiveWorkoutExercise
import com.fitlogpro.app.domain.model.LiveWorkoutSet
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.PRType
import com.fitlogpro.app.domain.model.SetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class ActiveWorkoutRepository(private val activeWorkoutDao: ActiveWorkoutDao) {

    val activeSessionFlow: Flow<ActiveWorkoutSession?> = activeWorkoutDao.getActiveSessionFlow().map { entity ->
        entity?.let { deserialize(it.sessionJson) }
    }

    suspend fun getActiveSession(): ActiveWorkoutSession? {
        val entity = activeWorkoutDao.getActiveSessionSync() ?: return null
        return deserialize(entity.sessionJson)
    }

    suspend fun saveActiveSession(session: ActiveWorkoutSession) {
        val json = serialize(session)
        val entity = ActiveWorkoutSessionEntity(
            sessionId = "CURRENT_ACTIVE_SESSION",
            routineId = session.routineId,
            workoutName = session.workoutName,
            startTimeEpochMs = session.startTimeEpochMs,
            elapsedSeconds = session.elapsedSeconds,
            isPaused = session.isPaused,
            lastPauseTimestampMs = session.lastPauseTimestampMs,
            sessionJson = json,
            lastUpdatedEpochMs = System.currentTimeMillis()
        )
        activeWorkoutDao.saveActiveSession(entity)
    }

    suspend fun clearActiveSession() {
        activeWorkoutDao.clearActiveSession()
    }

    fun serialize(session: ActiveWorkoutSession): String {
        val root = JSONObject()
        root.put("sessionId", session.sessionId)
        root.put("routineId", session.routineId ?: JSONObject.NULL)
        root.put("workoutName", session.workoutName)
        root.put("startTimeEpochMs", session.startTimeEpochMs)
        root.put("elapsedSeconds", session.elapsedSeconds)
        root.put("isPaused", session.isPaused)
        root.put("lastPauseTimestampMs", session.lastPauseTimestampMs ?: JSONObject.NULL)
        root.put("workoutNotes", session.workoutNotes)

        val exercisesArray = JSONArray()
        for (ex in session.exercises) {
            val exObj = JSONObject()
            exObj.put("id", ex.id)
            exObj.put("exerciseId", ex.exerciseId)
            exObj.put("exerciseName", ex.exerciseName)
            exObj.put("muscleGroup", ex.muscleGroup.name)
            exObj.put("equipment", ex.equipment.name)
            exObj.put("exerciseType", ex.exerciseType.name)
            exObj.put("defaultRestSeconds", ex.defaultRestSeconds)
            exObj.put("supersetGroupId", ex.supersetGroupId ?: JSONObject.NULL)
            exObj.put("supersetLabel", ex.supersetLabel ?: JSONObject.NULL)
            exObj.put("notes", ex.notes)

            val setsArray = JSONArray()
            for (s in ex.sets) {
                val sObj = JSONObject()
                sObj.put("id", s.id)
                sObj.put("setNumber", s.setNumber)
                sObj.put("setType", s.setType.name)
                sObj.put("weightKg", s.weightKg)
                sObj.put("reps", s.reps)
                sObj.put("rpe", s.rpe ?: JSONObject.NULL)
                sObj.put("durationSeconds", s.durationSeconds ?: JSONObject.NULL)
                sObj.put("distanceKm", s.distanceKm ?: JSONObject.NULL)
                sObj.put("isCompleted", s.isCompleted)
                sObj.put("isPr", s.isPr)
                sObj.put("previousPerformance", s.previousPerformance ?: JSONObject.NULL)

                val prsArray = JSONArray()
                s.prTypes.forEach { prsArray.put(it.name) }
                sObj.put("prTypes", prsArray)

                setsArray.put(sObj)
            }
            exObj.put("sets", setsArray)
            exercisesArray.put(exObj)
        }
        root.put("exercises", exercisesArray)

        return root.toString()
    }

    fun deserialize(jsonString: String): ActiveWorkoutSession? {
        return try {
            val root = JSONObject(jsonString)
            val exercisesArray = root.getJSONArray("exercises")
            val exercises = mutableListOf<LiveWorkoutExercise>()

            for (i in 0 until exercisesArray.length()) {
                val exObj = exercisesArray.getJSONObject(i)
                val setsArray = exObj.getJSONArray("sets")
                val sets = mutableListOf<LiveWorkoutSet>()

                for (j in 0 until setsArray.length()) {
                    val sObj = setsArray.getJSONObject(j)
                    val prsArray = sObj.optJSONArray("prTypes") ?: JSONArray()
                    val prTypes = mutableListOf<PRType>()
                    for (k in 0 until prsArray.length()) {
                        prTypes.add(PRType.fromString(prsArray.getString(k)))
                    }

                    sets.add(
                        LiveWorkoutSet(
                            id = sObj.getString("id"),
                            setNumber = sObj.getInt("setNumber"),
                            setType = SetType.fromString(sObj.getString("setType")),
                            weightKg = sObj.optDouble("weightKg", 0.0),
                            reps = sObj.optInt("reps", 0),
                            rpe = if (sObj.isNull("rpe")) null else sObj.optDouble("rpe"),
                            durationSeconds = if (sObj.isNull("durationSeconds")) null else sObj.optInt("durationSeconds"),
                            distanceKm = if (sObj.isNull("distanceKm")) null else sObj.optDouble("distanceKm"),
                            isCompleted = sObj.optBoolean("isCompleted", false),
                            isPr = sObj.optBoolean("isPr", false),
                            prTypes = prTypes,
                            previousPerformance = if (sObj.isNull("previousPerformance")) null else sObj.optString("previousPerformance")
                        )
                    )
                }

                exercises.add(
                    LiveWorkoutExercise(
                        id = exObj.getString("id"),
                        exerciseId = exObj.getLong("exerciseId"),
                        exerciseName = exObj.getString("exerciseName"),
                        muscleGroup = MuscleGroup.fromString(exObj.getString("muscleGroup")),
                        equipment = Equipment.fromString(exObj.getString("equipment")),
                        exerciseType = ExerciseType.fromString(exObj.getString("exerciseType")),
                        defaultRestSeconds = exObj.optInt("defaultRestSeconds", 90),
                        supersetGroupId = if (exObj.isNull("supersetGroupId")) null else exObj.optString("supersetGroupId"),
                        supersetLabel = if (exObj.isNull("supersetLabel")) null else exObj.optString("supersetLabel"),
                        notes = exObj.optString("notes", ""),
                        sets = sets
                    )
                )
            }

            ActiveWorkoutSession(
                sessionId = root.getString("sessionId"),
                routineId = if (root.isNull("routineId")) null else root.optLong("routineId"),
                workoutName = root.getString("workoutName"),
                startTimeEpochMs = root.getLong("startTimeEpochMs"),
                elapsedSeconds = root.optLong("elapsedSeconds", 0),
                isPaused = root.optBoolean("isPaused", false),
                lastPauseTimestampMs = if (root.isNull("lastPauseTimestampMs")) null else root.optLong("lastPauseTimestampMs"),
                exercises = exercises,
                workoutNotes = root.optString("workoutNotes", "")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
