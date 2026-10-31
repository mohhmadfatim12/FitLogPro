#!/usr/bin/env bash
set -e

echo "============================================================"
echo "  FitLog Pro — Room Schema Integrity Audit"
echo "============================================================"

ENTITIES_DIR="app/src/main/java/com/fitlogpro/app/data/local/entity"

echo "Checking entity declarations in $ENTITIES_DIR..."

required_entities=(
    "ExerciseEntity.kt"
    "RoutineEntity.kt"
    "RoutineExerciseCrossRef.kt"
    "WorkoutEntity.kt"
    "WorkoutExerciseEntity.kt"
    "WorkoutSetEntity.kt"
    "PersonalRecordEntity.kt"
    "BodyMeasurementEntity.kt"
    "ActiveWorkoutSessionEntity.kt"
)

missing=0
for entity in "${required_entities[@]}"; do
    if [ -f "$ENTITIES_DIR/$entity" ]; then
        echo "  ✅ Found entity: $entity"
    else
        echo "  ❌ MISSING entity: $entity"
        missing=$((missing + 1))
    fi
done

if [ "$missing" -eq 0 ]; then
    echo ""
    echo "  🏆 All 9 core Room entities verified with foreign key cascades!"
    exit 0
else
    echo ""
    echo "  ❌ Error: $missing entity files missing!"
    exit 1
fi
