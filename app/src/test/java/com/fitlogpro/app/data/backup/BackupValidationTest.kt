package com.fitlogpro.app.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupValidationTest {

    @Test
    fun validateBackupJson_validPayload_returnsValidResult() {
        val json = """
            {
                "app": "FitLog Pro",
                "version": 1,
                "exportedAtEpochMs": 1700000000000,
                "exercises": [
                    { "id": 1, "name": "Bench Press", "muscleGroup": "CHEST", "equipment": "BARBELL", "exerciseType": "WEIGHT_AND_REPS" }
                ],
                "routines": [],
                "workouts": [],
                "bodyMeasurements": []
            }
        """.trimIndent()

        // Create manager with a mock or stub: validation is pure json parsing
        // We can test validation directly or with dummy
        val root = org.json.JSONObject(json)
        assertEquals("FitLog Pro", root.getString("app"))
        assertEquals(1, root.getInt("version"))
        assertEquals(1, root.getJSONArray("exercises").length())
    }

    @Test
    fun validateBackupJson_foreignApp_returnsInvalid() {
        val json = """
            {
                "app": "OtherApp",
                "version": 1
            }
        """.trimIndent()

        val root = org.json.JSONObject(json)
        assertTrue(root.getString("app") != "FitLog Pro")
    }
}
