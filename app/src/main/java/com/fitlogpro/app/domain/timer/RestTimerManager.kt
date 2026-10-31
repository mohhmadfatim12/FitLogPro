package com.fitlogpro.app.domain.timer

import kotlin.math.ceil
import kotlin.math.max

data class RestTimerSnapshot(
    val exerciseName: String? = null,
    val totalDurationSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val progressFraction: Float = 0f,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false
)

class RestTimerManager {

    private var currentExerciseName: String? = null
    private var totalDurationSeconds: Int = 0
    private var targetEndTimeEpochMs: Long? = null
    private var pausedRemainingSeconds: Int? = null
    private var isFinished: Boolean = false

    /**
     * Starts or resets rest timer with the given duration in seconds.
     * Background-safe: uses target epoch timestamp so sleeping/backgrounding does not drift.
     */
    fun start(seconds: Int, exerciseName: String? = null, nowMs: Long = System.currentTimeMillis()) {
        if (seconds <= 0) {
            stop()
            return
        }
        currentExerciseName = exerciseName
        totalDurationSeconds = seconds
        targetEndTimeEpochMs = nowMs + (seconds * 1000L)
        pausedRemainingSeconds = null
        isFinished = false
    }

    fun pause(nowMs: Long = System.currentTimeMillis()) {
        val target = targetEndTimeEpochMs ?: return
        val remaining = getRemainingSeconds(nowMs)
        if (remaining > 0) {
            pausedRemainingSeconds = remaining
            targetEndTimeEpochMs = null
        } else {
            isFinished = true
            targetEndTimeEpochMs = null
            pausedRemainingSeconds = null
        }
    }

    fun resume(nowMs: Long = System.currentTimeMillis()) {
        val paused = pausedRemainingSeconds ?: return
        targetEndTimeEpochMs = nowMs + (paused * 1000L)
        pausedRemainingSeconds = null
    }

    fun addTime(secondsToAdd: Int, nowMs: Long = System.currentTimeMillis()) {
        if (isFinished || (targetEndTimeEpochMs == null && pausedRemainingSeconds == null)) {
            start(secondsToAdd, currentExerciseName, nowMs)
            return
        }

        totalDurationSeconds += secondsToAdd

        if (pausedRemainingSeconds != null) {
            pausedRemainingSeconds = (pausedRemainingSeconds ?: 0) + secondsToAdd
        } else if (targetEndTimeEpochMs != null) {
            targetEndTimeEpochMs = (targetEndTimeEpochMs ?: nowMs) + (secondsToAdd * 1000L)
        }
    }

    fun subtractTime(secondsToSubtract: Int, nowMs: Long = System.currentTimeMillis()) {
        if (targetEndTimeEpochMs == null && pausedRemainingSeconds == null) return

        if (pausedRemainingSeconds != null) {
            val updated = max(0, (pausedRemainingSeconds ?: 0) - secondsToSubtract)
            pausedRemainingSeconds = updated
            if (updated == 0) {
                isFinished = true
                pausedRemainingSeconds = null
            }
        } else if (targetEndTimeEpochMs != null) {
            val remaining = getRemainingSeconds(nowMs)
            if (remaining <= secondsToSubtract) {
                isFinished = true
                targetEndTimeEpochMs = null
            } else {
                targetEndTimeEpochMs = (targetEndTimeEpochMs ?: nowMs) - (secondsToSubtract * 1000L)
            }
        }
    }

    fun skip() {
        stop()
    }

    fun stop() {
        currentExerciseName = null
        totalDurationSeconds = 0
        targetEndTimeEpochMs = null
        pausedRemainingSeconds = null
        isFinished = false
    }

    fun getRemainingSeconds(nowMs: Long = System.currentTimeMillis()): Int {
        if (pausedRemainingSeconds != null) {
            return pausedRemainingSeconds ?: 0
        }
        val target = targetEndTimeEpochMs ?: return 0
        val diff = target - nowMs
        return if (diff <= 0) {
            0
        } else {
            ceil(diff / 1000.0).toInt()
        }
    }

    fun getSnapshot(nowMs: Long = System.currentTimeMillis()): RestTimerSnapshot {
        val remaining = getRemainingSeconds(nowMs)
        val isRunning = targetEndTimeEpochMs != null && remaining > 0
        val isPaused = pausedRemainingSeconds != null

        if (targetEndTimeEpochMs != null && remaining <= 0) {
            isFinished = true
            targetEndTimeEpochMs = null
        }

        val fraction = if (totalDurationSeconds > 0) {
            max(0f, 1f - (remaining.toFloat() / totalDurationSeconds.toFloat()))
        } else 0f

        return RestTimerSnapshot(
            exerciseName = currentExerciseName,
            totalDurationSeconds = totalDurationSeconds,
            remainingSeconds = remaining,
            progressFraction = fraction,
            isRunning = isRunning,
            isPaused = isPaused,
            isFinished = isFinished
        )
    }
}
