package com.fitlogpro.app.data.backup

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRestoreSafetyTest {

    // Pure validation logic mirroring BackupManager.validateBackupJson without requiring Room instance
    private fun validate(jsonString: String): BackupValidationResult {
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

            val exercises = root.optJSONArray("exercises") ?: org.json.JSONArray()
            val routines = root.optJSONArray("routines") ?: org.json.JSONArray()
            val workouts = root.optJSONArray("workouts") ?: org.json.JSONArray()
            val measurements = root.optJSONArray("bodyMeasurements") ?: org.json.JSONArray()

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

    @Test
    fun malformedJsonString_returnsInvalidWithSyntaxError() {
        val brokenJson = "{ \"app\": \"FitLog Pro\", \"version\": 1, \"exercises\": [ "
        val result = validate(brokenJson)
        assertTrue(result is BackupValidationResult.Invalid)
        assertTrue((result as BackupValidationResult.Invalid).reason.contains("Malformed JSON payload"))
    }

    @Test
    fun missingAppKey_returnsInvalidSource() {
        val json = """
            {
                "version": 1,
                "exercises": []
            }
        """.trimIndent()
        val result = validate(json)
        assertTrue(result is BackupValidationResult.Invalid)
        assertTrue((result as BackupValidationResult.Invalid).reason.contains("Invalid backup source"))
    }

    @Test
    fun invalidVersionZeroOrNegative_returnsInvalidVersion() {
        val json = """
            {
                "app": "FitLog Pro",
                "version": 0
            }
        """.trimIndent()
        val result = validate(json)
        assertTrue(result is BackupValidationResult.Invalid)
        assertTrue((result as BackupValidationResult.Invalid).reason.contains("Unsupported backup version"))
    }

    @Test
    fun validBackupWithMissingArrays_defaultsToZeroCounts() {
        val json = """
            {
                "app": "FitLog Pro",
                "version": 1
            }
        """.trimIndent()
        val result = validate(json)
        assertTrue(result is BackupValidationResult.Valid)
        val valid = result as BackupValidationResult.Valid
        assertEquals(1, valid.version)
        assertEquals(0, valid.exerciseCount)
        assertEquals(0, valid.routineCount)
        assertEquals(0, valid.workoutCount)
        assertEquals(0, valid.measurementCount)
    }

    @Test
    fun validCompletePayload_accuratelyCountsAllEntities() {
        val json = """
            {
                "app": "FitLog Pro",
                "version": 1,
                "exportedAtEpochMs": 1700000000000,
                "exercises": [
                    {"name": "Bench Press"},
                    {"name": "Squat"},
                    {"name": "Deadlift"}
                ],
                "routines": [
                    {"name": "Push Day"},
                    {"name": "Pull Day"}
                ],
                "workouts": [
                    {"name": "Monday Push", "exercises": []}
                ],
                "bodyMeasurements": [
                    {"measurementType": "WEIGHT", "value": 82.5},
                    {"measurementType": "WAIST", "value": 84.0}
                ]
            }
        """.trimIndent()
        val result = validate(json)
        assertTrue(result is BackupValidationResult.Valid)
        val valid = result as BackupValidationResult.Valid
        assertEquals(3, valid.exerciseCount)
        assertEquals(2, valid.routineCount)
        assertEquals(1, valid.workoutCount)
        assertEquals(2, valid.measurementCount)
    }
}
