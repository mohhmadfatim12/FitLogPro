package com.fitlogpro.app.domain.timer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestTimerBackgroundToleranceTest {

    @Test
    fun subtractTime_pastRemainingSeconds_clampsAtZeroAndFinishes() {
        val timer = RestTimerManager()
        val startMs = 1000000L
        timer.start(20, null, startMs)

        // Subtract 30 seconds from a 20-second timer
        timer.subtractTime(30, startMs)

        val snapshot = timer.getSnapshot(startMs)
        assertEquals(0, snapshot.remainingSeconds)
        assertTrue(snapshot.isFinished)
        assertFalse(snapshot.isRunning)
    }

    @Test
    fun rapidTimeAdditions_accumulatesTotalDuration() {
        val timer = RestTimerManager()
        val startMs = 1000000L
        timer.start(30, null, startMs)

        timer.addTime(15, startMs)
        timer.addTime(30, startMs)
        timer.addTime(60, startMs)

        val snapshot = timer.getSnapshot(startMs)
        // 30 + 15 + 30 + 60 = 135s
        assertEquals(135, snapshot.remainingSeconds)
        assertEquals(135, snapshot.totalDurationSeconds)
    }

    @Test
    fun stopAndSkip_resetsTimerToCleanDefault() {
        val timer = RestTimerManager()
        timer.start(90, "Squats")
        timer.skip()

        val snapshot = timer.getSnapshot()
        assertEquals(0, snapshot.remainingSeconds)
        assertEquals(0, snapshot.totalDurationSeconds)
        assertFalse(snapshot.isRunning)
        assertFalse(snapshot.isPaused)
    }
}
