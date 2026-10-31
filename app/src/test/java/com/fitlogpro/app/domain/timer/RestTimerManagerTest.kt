package com.fitlogpro.app.domain.timer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestTimerManagerTest {

    @Test
    fun start_setsAccurateTargetTime() {
        val timer = RestTimerManager()
        val startMs = 1000000L
        timer.start(90, "Bench Press", startMs)

        val snapshot = timer.getSnapshot(startMs)
        assertEquals(90, snapshot.totalDurationSeconds)
        assertEquals(90, snapshot.remainingSeconds)
        assertTrue(snapshot.isRunning)
        assertFalse(snapshot.isPaused)
        assertEquals("Bench Press", snapshot.exerciseName)
    }

    @Test
    fun backgrounding_calculatesRemainingTimeAccuratelyAfterSleep() {
        val timer = RestTimerManager()
        val startMs = 1000000L
        timer.start(60, null, startMs)

        // Simulate device sleeping for 25 seconds
        val laterMs = startMs + 25000L
        val snapshot = timer.getSnapshot(laterMs)
        assertEquals(35, snapshot.remainingSeconds)
        assertTrue(snapshot.isRunning)
    }

    @Test
    fun pauseAndResume_freezesAndResumesCountdown() {
        val timer = RestTimerManager()
        val startMs = 1000000L
        timer.start(60, null, startMs)

        // 10 seconds pass
        val pauseMs = startMs + 10000L
        timer.pause(pauseMs)

        val pausedSnapshot = timer.getSnapshot(pauseMs + 30000L) // 30 sec later while paused
        assertTrue(pausedSnapshot.isPaused)
        assertFalse(pausedSnapshot.isRunning)
        assertEquals(50, pausedSnapshot.remainingSeconds)

        // Resume at 50 sec remaining
        val resumeMs = pauseMs + 40000L
        timer.resume(resumeMs)

        val runningSnapshot = timer.getSnapshot(resumeMs + 10000L) // 10 sec after resume
        assertEquals(40, runningSnapshot.remainingSeconds)
        assertTrue(runningSnapshot.isRunning)
    }

    @Test
    fun addTime_extendsRemainingDuration() {
        val timer = RestTimerManager()
        val startMs = 1000000L
        timer.start(30, null, startMs)

        timer.addTime(30, startMs)
        val snapshot = timer.getSnapshot(startMs)
        assertEquals(60, snapshot.remainingSeconds)
        assertEquals(60, snapshot.totalDurationSeconds)
    }

    @Test
    fun timerExpiry_marksFinished() {
        val timer = RestTimerManager()
        val startMs = 1000000L
        timer.start(30, null, startMs)

        val expiredMs = startMs + 31000L
        val snapshot = timer.getSnapshot(expiredMs)
        assertEquals(0, snapshot.remainingSeconds)
        assertTrue(snapshot.isFinished)
        assertFalse(snapshot.isRunning)
    }
}
