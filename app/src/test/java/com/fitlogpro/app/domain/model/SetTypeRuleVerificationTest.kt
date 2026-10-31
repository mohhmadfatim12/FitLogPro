package com.fitlogpro.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetTypeRuleVerificationTest {

    @Test
    fun warmupSet_strictlyExcludedFromVolumeAndPrs() {
        val warmup = SetType.WARMUP
        assertTrue(warmup.isWarmup)
        assertFalse(warmup.countsForVolume)
        assertFalse(warmup.countsForPr)
        assertEquals("W", warmup.shortCode)
        assertEquals("Warm-up", warmup.displayName)
    }

    @Test
    fun workingSets_includedInVolumeAndPrs() {
        val workingSets = listOf(SetType.NORMAL, SetType.DROP_SET, SetType.FAILURE)

        for (st in workingSets) {
            assertFalse("${st.name} should not be marked as warmup", st.isWarmup)
            assertTrue("${st.name} must count toward total volume", st.countsForVolume)
            assertTrue("${st.name} must be eligible for personal records", st.countsForPr)
        }
    }

    @Test
    fun fromString_resolvesShortCodesAndNames() {
        assertEquals(SetType.NORMAL, SetType.fromString("N"))
        assertEquals(SetType.NORMAL, SetType.fromString("Normal"))
        assertEquals(SetType.NORMAL, SetType.fromString("NORMAL"))

        assertEquals(SetType.WARMUP, SetType.fromString("W"))
        assertEquals(SetType.WARMUP, SetType.fromString("w"))
        assertEquals(SetType.WARMUP, SetType.fromString("Warm-up"))
        assertEquals(SetType.WARMUP, SetType.fromString("WARMUP"))

        assertEquals(SetType.DROP_SET, SetType.fromString("D"))
        assertEquals(SetType.DROP_SET, SetType.fromString("Drop Set"))
        assertEquals(SetType.DROP_SET, SetType.fromString("DROP_SET"))

        assertEquals(SetType.FAILURE, SetType.fromString("F"))
        assertEquals(SetType.FAILURE, SetType.fromString("Failure"))
        assertEquals(SetType.FAILURE, SetType.fromString("FAILURE"))

        // Unknown fallback
        assertEquals(SetType.NORMAL, SetType.fromString("XYZ"))
    }

    @Test
    fun allEntries_haveUniqueShortCodes() {
        val codes = SetType.entries.map { it.shortCode }
        assertEquals(codes.distinct().size, codes.size)
    }
}
