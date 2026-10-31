package com.fitlogpro.app.data.local.database

import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.MuscleGroup

object ExerciseSeedData {

    fun getDefaultExercises(): List<ExerciseEntity> = listOf(
        // Chest
        ExerciseEntity(1, "Barbell Bench Press", MuscleGroup.CHEST, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 120, false, "Lie on bench, grip bar slightly wider than shoulder width, lower bar to mid-chest, press up.", true),
        ExerciseEntity(2, "Incline Dumbbell Press", MuscleGroup.CHEST, Equipment.DUMBBELL, ExerciseType.WEIGHT_AND_REPS, 90, false, "Set bench to 30-45 degrees, press dumbbells overhead with control."),
        ExerciseEntity(3, "Dumbbell Flyes", MuscleGroup.CHEST, Equipment.DUMBBELL, ExerciseType.WEIGHT_AND_REPS, 75, false, "Lie flat, open arms in arc motion, feel deep stretch in chest, squeeze together at top."),
        ExerciseEntity(4, "Cable Crossover", MuscleGroup.CHEST, Equipment.CABLE, ExerciseType.WEIGHT_AND_REPS, 60, false, "High cables, bring hands down and forward in hugging motion."),
        ExerciseEntity(5, "Push-Up", MuscleGroup.CHEST, Equipment.BODYWEIGHT, ExerciseType.BODYWEIGHT_REPS, 60, false, "Plank position, lower chest to floor, push back up keeping core tight."),
        ExerciseEntity(6, "Dips (Chest Focus)", MuscleGroup.CHEST, Equipment.BODYWEIGHT, ExerciseType.BODYWEIGHT_REPS, 90, false, "Lean torso forward slightly to target pectoral fibers."),

        // Back
        ExerciseEntity(7, "Barbell Deadlift", MuscleGroup.BACK, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 180, false, "Feet hip-width, grip bar outside legs, lift keeping spine neutral and hips driving forward.", true),
        ExerciseEntity(8, "Barbell Bent-Over Row", MuscleGroup.BACK, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 90, false, "Hinge at hips, pull bar to lower ribcage keeping elbows tucked."),
        ExerciseEntity(9, "Lat Pulldown", MuscleGroup.BACK, Equipment.CABLE, ExerciseType.WEIGHT_AND_REPS, 75, false, "Grip wide, pull bar down towards upper chest, retracting shoulder blades."),
        ExerciseEntity(10, "Pull-Up", MuscleGroup.BACK, Equipment.BODYWEIGHT, ExerciseType.BODYWEIGHT_REPS, 90, false, "Overhand grip, pull chest to bar with strict form.", true),
        ExerciseEntity(11, "Seated Cable Row", MuscleGroup.BACK, Equipment.CABLE, ExerciseType.WEIGHT_AND_REPS, 75, false, "Sit upright, pull handle towards abdomen, squeezing lats."),
        ExerciseEntity(12, "One-Arm Dumbbell Row", MuscleGroup.BACK, Equipment.DUMBBELL, ExerciseType.WEIGHT_AND_REPS, 60, false, "One knee on bench, pull dumbbell to hip keeping back flat."),
        ExerciseEntity(13, "Face Pull", MuscleGroup.BACK, Equipment.CABLE, ExerciseType.WEIGHT_AND_REPS, 60, false, "Rope attachment at eye level, pull towards ears, rotate shoulders externally."),

        // Shoulders
        ExerciseEntity(14, "Overhead Barbell Press", MuscleGroup.SHOULDERS, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 120, false, "Stand tall, press bar overhead to full lockout without excessive arching.", true),
        ExerciseEntity(15, "Seated Dumbbell Shoulder Press", MuscleGroup.SHOULDERS, Equipment.DUMBBELL, ExerciseType.WEIGHT_AND_REPS, 90, false, "Press dumbbells overhead from ear level."),
        ExerciseEntity(16, "Dumbbell Lateral Raise", MuscleGroup.SHOULDERS, Equipment.DUMBBELL, ExerciseType.WEIGHT_AND_REPS, 60, false, "Raise dumbbells to side until parallel to floor, slight bend in elbows."),
        ExerciseEntity(17, "Cable Lateral Raise", MuscleGroup.SHOULDERS, Equipment.CABLE, ExerciseType.WEIGHT_AND_REPS, 60, false, "Constant tension on lateral deltoid fibers."),
        ExerciseEntity(18, "Rear Delt Reverse Fly", MuscleGroup.SHOULDERS, Equipment.DUMBBELL, ExerciseType.WEIGHT_AND_REPS, 60, false, "Bent forward, raise arms outward squeezing rear delts."),

        // Legs - Quads & Hamstrings & Glutes & Calves
        ExerciseEntity(19, "Barbell Back Squat", MuscleGroup.QUADS, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 180, false, "Bar on upper traps, squat below parallel, knees tracking over toes.", true),
        ExerciseEntity(20, "Front Squat", MuscleGroup.QUADS, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 120, false, "Bar across front shoulders, upright torso, deep quad activation."),
        ExerciseEntity(21, "Leg Press", MuscleGroup.QUADS, Equipment.MACHINE, ExerciseType.WEIGHT_AND_REPS, 90, false, "Feet shoulder-width on platform, lower until knees at 90 degrees, press through heels."),
        ExerciseEntity(22, "Leg Extension", MuscleGroup.QUADS, Equipment.MACHINE, ExerciseType.WEIGHT_AND_REPS, 60, false, "Extend knees to full contraction, control eccentric lowering."),
        ExerciseEntity(23, "Romanian Deadlift", MuscleGroup.HAMSTRINGS, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 90, false, "Slight knee bend, hinge hips backward until hamstrings stretch, drive hips forward."),
        ExerciseEntity(24, "Lying Leg Curl", MuscleGroup.HAMSTRINGS, Equipment.MACHINE, ExerciseType.WEIGHT_AND_REPS, 60, false, "Curl pad towards glutes, hold for peak contraction."),
        ExerciseEntity(25, "Barbell Hip Thrust", MuscleGroup.GLUTES, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 90, false, "Back against bench, drive hips upward until thighs and torso align, squeeze glutes.", true),
        ExerciseEntity(26, "Bulgarian Split Squat", MuscleGroup.QUADS, Equipment.DUMBBELL, ExerciseType.WEIGHT_AND_REPS, 90, false, "Rear foot elevated on bench, lower front knee to floor."),
        ExerciseEntity(27, "Standing Calf Raise", MuscleGroup.CALVES, Equipment.MACHINE, ExerciseType.WEIGHT_AND_REPS, 60, false, "Full stretch at bottom, press up onto balls of feet, hold 1s."),
        ExerciseEntity(28, "Seated Calf Raise", MuscleGroup.CALVES, Equipment.MACHINE, ExerciseType.WEIGHT_AND_REPS, 60, false, "Isolates the soleus muscle with knees bent."),

        // Arms - Biceps & Triceps
        ExerciseEntity(29, "Barbell Bicep Curl", MuscleGroup.BICEPS, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 60, false, "Elbows pinned to sides, curl bar to chin level with no swinging."),
        ExerciseEntity(30, "Dumbbell Hammer Curl", MuscleGroup.BICEPS, Equipment.DUMBBELL, ExerciseType.WEIGHT_AND_REPS, 60, false, "Neutral grip, targets brachialis and forearms."),
        ExerciseEntity(31, "Incline Dumbbell Curl", MuscleGroup.BICEPS, Equipment.DUMBBELL, ExerciseType.WEIGHT_AND_REPS, 60, false, "Incline bench provides maximum stretch on long head of bicep."),
        ExerciseEntity(32, "Preacher Curl", MuscleGroup.BICEPS, Equipment.MACHINE, ExerciseType.WEIGHT_AND_REPS, 60, false, "Arms locked against pad for strict isolation."),
        ExerciseEntity(33, "Tricep Rope Pushdown", MuscleGroup.TRICEPS, Equipment.CABLE, ExerciseType.WEIGHT_AND_REPS, 60, false, "Extend arms downward, flare rope at bottom for full tricep lockout."),
        ExerciseEntity(34, "Skull Crushers (Lying Triceps Extension)", MuscleGroup.TRICEPS, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 75, false, "Lie flat, lower EZ bar to forehead/behind head, extend elbows."),
        ExerciseEntity(35, "Overhead Dumbbell Tricep Extension", MuscleGroup.TRICEPS, Equipment.DUMBBELL, ExerciseType.WEIGHT_AND_REPS, 60, false, "Lower dumbbell behind neck, press upward."),
        ExerciseEntity(36, "Close-Grip Bench Press", MuscleGroup.TRICEPS, Equipment.BARBELL, ExerciseType.WEIGHT_AND_REPS, 90, false, "Grip at shoulder width, keeps triceps under heavy mechanical tension."),

        // Core
        ExerciseEntity(37, "Hanging Leg Raise", MuscleGroup.CORE, Equipment.BODYWEIGHT, ExerciseType.BODYWEIGHT_REPS, 60, false, "Hang from pullup bar, raise legs to horizontal without swinging."),
        ExerciseEntity(38, "Plank", MuscleGroup.CORE, Equipment.BODYWEIGHT, ExerciseType.CARDIO, 60, false, "Hold forearm plank maintaining neutral spine and engaged glutes."),
        ExerciseEntity(39, "Cable Woodchopper", MuscleGroup.CORE, Equipment.CABLE, ExerciseType.WEIGHT_AND_REPS, 60, false, "Rotational power exercise targeting obliques."),
        ExerciseEntity(40, "Ab Wheel Rollout", MuscleGroup.CORE, Equipment.BODYWEIGHT, ExerciseType.BODYWEIGHT_REPS, 60, false, "Roll wheel forward from knees, keeping lower back hollow."),

        // Cardio
        ExerciseEntity(41, "Treadmill Running", MuscleGroup.CARDIO, Equipment.CARDIO_MACHINE, ExerciseType.CARDIO, 60, false, "Track duration, distance, pace, and calories."),
        ExerciseEntity(42, "Stationary Bike", MuscleGroup.CARDIO, Equipment.CARDIO_MACHINE, ExerciseType.CARDIO, 60, false, "Low-impact cycling cardio."),
        ExerciseEntity(43, "Rowing Machine", MuscleGroup.CARDIO, Equipment.CARDIO_MACHINE, ExerciseType.CARDIO, 60, false, "Full-body aerobic conditioning."),
        ExerciseEntity(44, "Stair Climber", MuscleGroup.CARDIO, Equipment.CARDIO_MACHINE, ExerciseType.CARDIO, 60, false, "Cardiovascular and glute endurance."),
        ExerciseEntity(45, "Jump Rope", MuscleGroup.CARDIO, Equipment.OTHER, ExerciseType.CARDIO, 60, false, "High-intensity agility and cardio conditioning.")
    )
}
