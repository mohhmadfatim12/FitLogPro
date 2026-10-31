package com.fitlogpro.app.domain.timer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestTimerStateTransitionTest {

    @Test
    fun idleState_hasDefaultSnapshot() {
        val manager = RestTimerManager()
        val snapshot = manager.getSnapshot(1000L)

        assertFalse(snapshot.isRunning)
        assertFalse(snapshot.isPaused)
        assertFalse(snapshot.isFinished)
        assertEquals(0, snapshot.remainingSeconds)
        assertEquals(0, snapshot.totalDurationSeconds)
    }

    @Test
    fun startTransition_movesToRunning() {
        val manager = RestTimerManager()
        manager.start(90, "Bench Press", 1000L)

        val snapshot = manager.getSnapshot(1000L)
        assertTrue(snapshot.isRunning)
        assertFalse(snapshot.isPaused)
        assertFalse(snapshot.isFinished)
        assertEquals(90, snapshot.remainingSeconds)
        assertEquals(90, snapshot.totalDurationSeconds)
        assertEquals("Bench Press", snapshot.exerciseName)
    }

    @Test
    fun pauseAndResumeLifecycle_preservesExactSeconds() {
        val manager = RestTimerManager()
        manager.start(60, "Squat", 1000L)

        // Advance 20 seconds
        val pausedAt = 21000L
        manager.pause(pausedAt)

        val pausedSnapshot = manager.getSnapshot(pausedAt)
        assertFalse(pausedSnapshot.isRunning)
        assertTrue(pausedSnapshot.isPaused)
        assertFalse(pausedSnapshot.isFinished)
        assertEquals(40, pausedSnapshot.remainingSeconds)

        // Stay paused for 5 minutes (300,000 ms)
        val resumeAt = pausedAt + 300000L
        val stillPausedSnapshot = manager.getSnapshot(resumeAt)
        assertEquals(40, stillPausedSnapshot.remainingSeconds)

        // Resume
        manager.resume(resumeAt)
        val resumedSnapshot = manager.getSnapshot(resumeAt)
        assertTrue(resumedSnapshot.isRunning)
        assertFalse(resumedSnapshot.isPaused)
        assertEquals(40, resumedSnapshot.remainingSeconds)

        // Advance 30 more seconds
        val after30s = resumeAt + 30000L
        val finalRunningSnapshot = manager.getSnapshot(after30s)
        assertEquals(10, finalRunningSnapshot.remainingSeconds)
    }

    @Test
    fun addTimeInPausedState_increasesRemainingSeconds() {
        val manager = RestTimerManager()
        manager.start(60, "Deadlift", 1000L)
        manager.pause(21000L) // 40s remaining

        manager.addTime(30, 25000L)
        val snapshot = manager.getSnapshot(25000L)
        assertTrue(snapshot.isPaused)
        assertEquals(70, snapshot.remainingSeconds)
        assertEquals(90, snapshot.totalDurationSeconds)
    }

    @Test
    fun expirationTransition_marksFinished() {
        val manager = RestTimerManager()
        manager.start(30, "Overhead Press", 1000L)

        // Check at 31 seconds later
        val snapshot = manager.getSnapshot(32000L)
        assertFalse(snapshot.isRunning)
        assertFalse(snapshot.isPaused)
        assertTrue(snapshot.isFinished)
        assertEquals(0, snapshot.remainingSeconds)
    }

    @Test
    fun skipTransition_immediatelyStops() {
        val manager = RestTimerManager()
        manager.start(90, "Pull Up", 1000L)
        assertTrue(manager.getSnapshot(1000L).isRunning)

        manager.skip()
        val snapshot = manager.getSnapshot(2000L)
        assertFalse(snapshot.isRunning)
        assertFalse(snapshot.isPaused)
        assertEquals(0, snapshot.remainingSeconds)
    }
}
