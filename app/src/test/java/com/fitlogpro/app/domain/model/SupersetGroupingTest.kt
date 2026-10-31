package com.fitlogpro.app.domain.model

import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.data.local.entity.RoutineExerciseCrossRef
import com.fitlogpro.app.data.local.entity.RoutineExerciseItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SupersetGroupingTest {

    // Helper function that resolves superset labels A1, A2, B1, etc.
    private fun assignSupersetLabels(items: List<RoutineExerciseCrossRef>): Map<Long, String> {
        val labelMap = mutableMapOf<Long, String>()
        val groupCounters = mutableMapOf<String, Int>()
        val groupLetters = mutableMapOf<String, Char>()
        var nextLetter = 'A'

        for (item in items) {
            val groupId = item.supersetGroupId
            if (!groupId.isNullOrEmpty()) {
                val letter = groupLetters.getOrPut(groupId) { nextLetter++ }
                val count = groupCounters.getOrDefault(groupId, 0) + 1
                groupCounters[groupId] = count
                labelMap[item.exerciseId] = "$letter$count"
            }
        }
        return labelMap
    }

    @Test
    fun assignSupersetLabels_correctlyLabelsPairedExercises() {
        val crossRefs = listOf(
            RoutineExerciseCrossRef(id = 1, routineId = 1, exerciseId = 10, orderIndex = 0, supersetGroupId = "GROUP_PUSH_PULL"),
            RoutineExerciseCrossRef(id = 2, routineId = 1, exerciseId = 20, orderIndex = 1, supersetGroupId = "GROUP_PUSH_PULL"),
            RoutineExerciseCrossRef(id = 3, routineId = 1, exerciseId = 30, orderIndex = 2, supersetGroupId = null), // Standalone
            RoutineExerciseCrossRef(id = 4, routineId = 1, exerciseId = 40, orderIndex = 3, supersetGroupId = "GROUP_ARMS"),
            RoutineExerciseCrossRef(id = 5, routineId = 1, exerciseId = 50, orderIndex = 4, supersetGroupId = "GROUP_ARMS")
        )

        val labels = assignSupersetLabels(crossRefs)

        // Group 1: A1, A2
        assertEquals("A1", labels[10L])
        assertEquals("A2", labels[20L])

        // Standalone has no superset label
        assertNull(labels[30L])

        // Group 2: B1, B2
        assertEquals("B1", labels[40L])
        assertEquals("B2", labels[50L])
    }

    @Test
    fun standaloneExercises_produceNoSupersetLabels() {
        val crossRefs = listOf(
            RoutineExerciseCrossRef(id = 1, routineId = 1, exerciseId = 10, orderIndex = 0, supersetGroupId = null),
            RoutineExerciseCrossRef(id = 2, routineId = 1, exerciseId = 20, orderIndex = 1, supersetGroupId = null)
        )

        val labels = assignSupersetLabels(crossRefs)
        assertTrue(labels.isEmpty())
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}
