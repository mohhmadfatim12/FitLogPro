# Changelog — FitLog Pro

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-10-31

### Added
- **Domain Calculators:**
  - `OneRepMaxCalculator`: Epley estimated 1RM formula `Weight × (1 + Reps / 30)` with single-rep identity `1 rep = exact weight`.
  - `VolumeCalculator`: Total workout volume calculation with strict warm-up exclusion (`countsForVolume = false`).
  - `PersonalRecordDetector`: Automated detection of 4 PR types (*Heaviest Weight*, *Best 1RM*, *Best Set Volume*, *Most Reps*) with warm-up immunity.
  - `StreakCalculator`: Daily and weekly consistency streak algorithms with year-end rollover support.
  - `UnitConverter`: Bidirectional conversions for metric and imperial weights, distances, and circumferences.
  - `CardioCalculator`: Speed, pace, and running time calculations.
  - `RpeCalculator`: RPE 6.0–10.0 half-step validation and Reps In Reserve (RIR) conversion.
- **Relational Data Layer (Room SQLite 2.6.1):**
  - 9 Room entities: `ExerciseEntity`, `RoutineEntity`, `RoutineExerciseCrossRef`, `WorkoutEntity`, `WorkoutExerciseEntity`, `WorkoutSetEntity`, `PersonalRecordEntity`, `BodyMeasurementEntity`, and `ActiveWorkoutSessionEntity`.
  - Cascading foreign keys and optimized composite indices.
  - Seed database with 45+ gym exercises covering all primary muscle groups.
- **Resilience & Safety:**
  - Active workout crash recovery via SQLite autosave every 5 seconds.
  - Rest timer background drift prevention using target epoch timestamps.
  - JSON backup and restore with foreign app schema validation.
  - RFC 4180 compliant CSV export for workouts, exercises, and measurements.
- **User Interface (Jetpack Compose + Material 3):**
  - Gym-friendly dark and light themes with high-contrast badge indicators.
  - Interactive Canvas charts: 1RM progress curves, weekly volume bars, muscle distribution percentage rings.
  - Keep-screen-on window management during live workout sessions.
  - Routines builder with drag-and-drop reordering and superset pairings (A1, A2).
- **Verification & Testing:**
  - 30+ JVM unit and integration test suites.
  - 12-round automated verification harness (`verify_fitlog_suite.py`) with 39/39 passing checks.
  - GitHub Actions CI workflow for cloud APK builds on JDK 17.
