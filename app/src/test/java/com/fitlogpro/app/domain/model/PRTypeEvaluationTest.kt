package com.fitlogpro.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PRTypeEvaluationTest {

    @Test
    fun unitSuffix_formatsWeightOrRepsCorrectly() {
        // Weight-based PRs
        assertEquals("kg", PRType.HEAVIEST_WEIGHT.unitSuffix(WeightUnit.KG))
        assertEquals("lbs", PRType.HEAVIEST_WEIGHT.unitSuffix(WeightUnit.LBS))

        assertEquals("kg", PRType.BEST_1RM.unitSuffix(WeightUnit.KG))
        assertEquals("lbs", PRType.BEST_1RM.unitSuffix(WeightUnit.LBS))

        assertEquals("kg", PRType.BEST_SET_VOLUME.unitSuffix(WeightUnit.KG))
        assertEquals("lbs", PRType.BEST_SET_VOLUME.unitSuffix(WeightUnit.LBS))

        // Reps-based PR
        assertEquals("reps", PRType.MOST_REPS.unitSuffix(WeightUnit.KG))
        assertEquals("reps", PRType.MOST_REPS.unitSuffix(WeightUnit.LBS))
    }

    @Test
    fun prTypes_haveDescriptiveMetadata() {
        for (pr in PRType.entries) {
            assertTrue(pr.title.isNotBlank())
            assertTrue(pr.description.isNotBlank())
        }
    }

    @Test
    fun fromString_resolvesAllTypes() {
        assertEquals(PRType.HEAVIEST_WEIGHT, PRType.fromString("HEAVIEST_WEIGHT"))
        assertEquals(PRType.BEST_1RM, PRType.fromString("best_1rm"))
        assertEquals(PRType.BEST_SET_VOLUME, PRType.fromString("BEST_SET_VOLUME"))
        assertEquals(PRType.MOST_REPS, PRType.fromString("most_reps"))
        assertEquals(PRType.HEAVIEST_WEIGHT, PRType.fromString("invalid_code"))
    }
}
