package com.fitlogpro.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fitlogpro.app.data.local.dao.ActiveWorkoutDao
import com.fitlogpro.app.data.local.dao.BodyMeasurementDao
import com.fitlogpro.app.data.local.dao.ExerciseDao
import com.fitlogpro.app.data.local.dao.PersonalRecordDao
import com.fitlogpro.app.data.local.dao.RoutineDao
import com.fitlogpro.app.data.local.dao.WorkoutDao
import com.fitlogpro.app.data.local.entity.ActiveWorkoutSessionEntity
import com.fitlogpro.app.data.local.entity.BodyMeasurementEntity
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.data.local.entity.PersonalRecordEntity
import com.fitlogpro.app.data.local.entity.RoutineEntity
import com.fitlogpro.app.data.local.entity.RoutineExerciseCrossRef
import com.fitlogpro.app.data.local.entity.WorkoutEntity
import com.fitlogpro.app.data.local.entity.WorkoutExerciseEntity
import com.fitlogpro.app.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineExerciseCrossRef::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        PersonalRecordEntity::class,
        BodyMeasurementEntity::class,
        ActiveWorkoutSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(FitLogTypeConverters::class)
abstract class FitLogDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun activeWorkoutDao(): ActiveWorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: FitLogDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): FitLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitLogDatabase::class.java,
                    "fitlog_pro.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default exercises and starter routines
                            scope.launch {
                                val database = getInstance(context, scope)
                                database.exerciseDao().insertAll(ExerciseSeedData.getDefaultExercises())
                                seedDefaultRoutines(database)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDefaultRoutines(db: FitLogDatabase) {
            val pushRoutineId = db.routineDao().insertRoutine(
                RoutineEntity(
                    id = 1,
                    name = "Push Day (Chest, Shoulders, Triceps)",
                    description = "Hypertrophy push session targeting pectorals, anterior delts, and triceps."
                )
            )
            db.routineDao().insertRoutineExercises(
                listOf(
                    RoutineExerciseCrossRef(0, pushRoutineId, 1, 1, 4, 8, 80.0, 8.0, null, "Pyramid up"),
                    RoutineExerciseCrossRef(0, pushRoutineId, 2, 2, 3, 10, 26.0, 8.5, null, "30-degree incline"),
                    RoutineExerciseCrossRef(0, pushRoutineId, 16, 3, 4, 15, 12.0, 9.0, "SUPERSET_A", "Superset with rope pushdown"),
                    RoutineExerciseCrossRef(0, pushRoutineId, 33, 4, 4, 15, 25.0, 9.5, "SUPERSET_A", "Superset with lateral raise")
                )
            )

            val pullRoutineId = db.routineDao().insertRoutine(
                RoutineEntity(
                    id = 2,
                    name = "Pull Day (Back, Rear Delts, Biceps)",
                    description = "Heavy back width and thickness combined with bicep curls."
                )
            )
            db.routineDao().insertRoutineExercises(
                listOf(
                    RoutineExerciseCrossRef(0, pullRoutineId, 7, 1, 3, 5, 140.0, 8.5, null, "Conventional deadlift"),
                    RoutineExerciseCrossRef(0, pullRoutineId, 10, 2, 3, 8, 0.0, 9.0, null, "Strict pull-ups"),
                    RoutineExerciseCrossRef(0, pullRoutineId, 8, 3, 3, 10, 70.0, 8.0, null, "Barbell rows"),
                    RoutineExerciseCrossRef(0, pullRoutineId, 29, 4, 3, 12, 30.0, 9.0, null, "Barbell curls")
                )
            )

            val legRoutineId = db.routineDao().insertRoutine(
                RoutineEntity(
                    id = 3,
                    name = "Leg Day (Quads, Hamstrings, Calves)",
                    description = "Lower body strength and hypertrophy."
                )
            )
            db.routineDao().insertRoutineExercises(
                listOf(
                    RoutineExerciseCrossRef(0, legRoutineId, 19, 1, 4, 8, 100.0, 8.5, null, "Back squats below parallel"),
                    RoutineExerciseCrossRef(0, legRoutineId, 23, 2, 3, 10, 80.0, 8.0, null, "Romanian deadlifts"),
                    RoutineExerciseCrossRef(0, legRoutineId, 21, 3, 3, 12, 160.0, 8.5, null, "Leg press"),
                    RoutineExerciseCrossRef(0, legRoutineId, 27, 4, 4, 15, 60.0, 9.0, null, "Calf raises with 1s pause")
                )
            )
        }
    }
}
