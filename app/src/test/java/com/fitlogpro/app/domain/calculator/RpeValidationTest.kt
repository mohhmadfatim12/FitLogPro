package com.fitlogpro.app.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RpeValidationTest {

    @Test
    fun parseAndValidateRpe_validInputs_roundsToHalfSteps() {
        assertEquals(8.0, RpeCalculator.parseAndValidateRpe("8.0"))
        assertEquals(8.5, RpeCalculator.parseAndValidateRpe("8.5"))
        assertEquals(8.5, RpeCalculator.parseAndValidateRpe("8,5")) // Comma decimal separator
        assertEquals(9.0, RpeCalculator.parseAndValidateRpe("8.9")) // Rounds to 9.0
        assertEquals(7.5, RpeCalculator.parseAndValidateRpe("7.4")) // Rounds to 7.5
    }

    @Test
    fun parseAndValidateRpe_outOfBounds_clampsCorrectly() {
        assertEquals(10.0, RpeCalculator.parseAndValidateRpe("12.0")) // Clamped to 10
        assertEquals(6.0, RpeCalculator.parseAndValidateRpe("4.0"))   // Clamped to 6
    }

    @Test
    fun parseAndValidateRpe_invalidInputs_returnsNull() {
        assertNull(RpeCalculator.parseAndValidateRpe(""))
        assertNull(RpeCalculator.parseAndValidateRpe("   "))
        assertNull(RpeCalculator.parseAndValidateRpe("abc"))
    }

    @Test
    fun calculateRir_calculatesAccurateRepsInReserve() {
        assertEquals(2.0, RpeCalculator.calculateRir(8.0))
        assertEquals(1.5, RpeCalculator.calculateRir(8.5))
        assertEquals(1.0, RpeCalculator.calculateRir(9.0))
        assertEquals(0.5, RpeCalculator.calculateRir(9.5))
        assertEquals(0.0, RpeCalculator.calculateRir(10.0))
        assertNull(RpeCalculator.calculateRir(null))
    }

    @Test
    fun formatRirLabel_formatsCleanText() {
        assertEquals("2.0 RIR", RpeCalculator.formatRirLabel(8.0))
        assertEquals("0.5 RIR", RpeCalculator.formatRirLabel(9.5))
        assertEquals("Failure (0 RIR)", RpeCalculator.formatRirLabel(10.0))
        assertNull(RpeCalculator.formatRirLabel(null))
    }
}
