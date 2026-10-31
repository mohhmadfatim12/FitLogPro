package com.fitlogpro.app.data.export

import com.fitlogpro.app.data.local.entity.BodyMeasurementEntity
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.data.local.entity.WorkoutWithDetails
import com.fitlogpro.app.domain.calculator.UnitConverter
import com.fitlogpro.app.domain.model.DistanceUnit
import com.fitlogpro.app.domain.model.WeightUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun exportWorkoutsToCsv(
        workouts: List<WorkoutWithDetails>,
        weightUnit: WeightUnit = WeightUnit.KG,
        distanceUnit: DistanceUnit = DistanceUnit.KM
    ): String {
        val sb = StringBuilder()
        sb.append("Workout Date,Workout Name,Duration (min),Total Volume,Exercise,Set Number,Set Type,Weight,Weight Unit,Reps,RPE,Cardio Duration (s),Distance,Distance Unit,Is Completed,Is PR\n")

        for (w in workouts) {
            val dateStr = dateFormat.format(Date(w.workout.startTimeEpochMs))
            val durationMin = w.workout.durationSeconds / 60
            val volumeDisplay = UnitConverter.displayWeight(w.workout.totalVolumeKg, weightUnit)

            for (we in w.exercises.sortedBy { it.workoutExercise.orderIndex }) {
                val exerciseName = escapeCsv(we.exercise.name)
                for (s in we.sets.sortedBy { it.setNumber }) {
                    val weightDisplay = UnitConverter.displayWeight(s.weightKg, weightUnit)
                    val distanceDisplay = s.distanceKm?.let { UnitConverter.displayDistance(it, distanceUnit) } ?: ""

                    sb.append("\"$dateStr\",")
                    sb.append("\"${escapeCsv(w.workout.name)}\",")
                    sb.append("$durationMin,")
                    sb.append("$volumeDisplay,")
                    sb.append("\"$exerciseName\",")
                    sb.append("${s.setNumber},")
                    sb.append("\"${s.setType.displayName}\",")
                    sb.append("$weightDisplay,")
                    sb.append("\"${weightUnit.symbol}\",")
                    sb.append("${s.reps},")
                    sb.append("${s.rpe ?: ""},")
                    sb.append("${s.durationSeconds ?: ""},")
                    sb.append("$distanceDisplay,")
                    sb.append("\"${distanceUnit.symbol}\",")
                    sb.append("${s.isCompleted},")
                    sb.append("${s.isPr}\n")
                }
            }
        }
        return sb.toString()
    }

    fun exportExercisesToCsv(exercises: List<ExerciseEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Exercise Name,Muscle Group,Equipment,Exercise Type,Default Rest (s),Is Custom,Instructions\n")
        for (e in exercises) {
            sb.append("${e.id},")
            sb.append("\"${escapeCsv(e.name)}\",")
            sb.append("\"${e.muscleGroup.displayName}\",")
            sb.append("\"${e.equipment.displayName}\",")
            sb.append("\"${e.exerciseType.displayName}\",")
            sb.append("${e.defaultRestSeconds},")
            sb.append("${e.isCustom},")
            sb.append("\"${escapeCsv(e.instructions)}\"\n")
        }
        return sb.toString()
    }

    fun exportMeasurementsToCsv(measurements: List<BodyMeasurementEntity>, isMetric: Boolean = true): String {
        val sb = StringBuilder()
        sb.append("Recorded Date,Measurement Type,Value,Unit,Notes\n")
        for (m in measurements) {
            val dateStr = dateFormat.format(Date(m.recordedEpochMs))
            val displayVal = if (isMetric) m.value else {
                when (m.measurementType) {
                    com.fitlogpro.app.domain.model.MeasurementType.WEIGHT -> UnitConverter.kgToLbs(m.value)
                    com.fitlogpro.app.domain.model.MeasurementType.BODY_FAT -> m.value
                    else -> UnitConverter.cmToInches(m.value)
                }
            }
            sb.append("\"$dateStr\",")
            sb.append("\"${m.measurementType.displayName}\",")
            sb.append("$displayVal,")
            sb.append("\"${m.measurementType.getUnit(isMetric)}\",")
            sb.append("\"${escapeCsv(m.notes)}\"\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
