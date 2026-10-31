# FitLog Pro — High-Risk Mitigation Matrix & Guarantees

This document details the engineering countermeasures implemented for the 5 highest-risk areas defined in the FitLog Pro product specification.

---

## 1. Risk Area 1: Active Workout Recovery (Process Death)

### The Risk
If a gym user receives a phone call, switches to Spotify/camera, or the Android OS kills the app in the background due to low memory, unsaved workout sets, weights, and elapsed time could be permanently lost.

### Mitigation Architecture
- **Persistent Recovery Entity (`ActiveWorkoutSessionEntity`)**: The active workout state is stored in a dedicated Room SQLite table under the fixed key `"CURRENT_ACTIVE_SESSION"`.
- **Autosave Cadence**:
  - Automatically written on every set completion toggle.
  - Automatically written whenever weights, reps, or notes change.
  - Automatically synced every 5 seconds during active timer running.
- **Full Model Reconstitution**: Upon app cold start, `ActiveWorkoutViewModel` checks for an existing session in SQLite and reinstates the entire exercise list, sets, RPE values, superset groupings, and elapsed workout time.
- **Purge on Resolution**: The row is explicitly cleared only when the user taps "Finish Workout" (after successful write to `workouts` table) or "Discard Workout".

### Verification Proof
- `ActiveWorkoutRecoveryTest.kt`: Tests saving and restoring sessions with mixed set types, cardio metrics, and pause timestamps.
- `ProcessRecoveryStateTest.kt`: Validates complete JSON serialization/deserialization fidelity.

---

## 2. Risk Area 2: Rest Timer Drift & Backgrounding

### The Risk
Standard Android coroutine timers (`delay(1000)`) stop or drift when the device sleeps, causing the rest timer to freeze at the state it was in when the screen turned off.

### Mitigation Architecture
- **Epoch Target Math**: `RestTimerManager` does not count down in one-second ticks. Instead, it computes `targetEndTimeEpochMs = System.currentTimeMillis() + (seconds * 1000L)`.
- **Absolute Time Delta**: When the app wakes or draws the UI, remaining seconds are calculated via `ceil((targetEndTimeEpochMs - nowMs) / 1000)`.
- **Pause & Resume Stability**: When paused, the exact remaining seconds are frozen. When resumed, a new target epoch is calculated from the current timestamp plus the saved remaining seconds.
- **Rapid Adjustment Safety**: Clamping ensures `addTime(+30s)` and `subtractTime(-15s)` never produce negative remaining values or exceed the maximum allowed rest time.

### Verification Proof
- `RestTimerManagerTest.kt`: Verifies epoch calculations during simulated sleeps.
- `RestTimerBackgroundToleranceTest.kt`: Tests wake events across deep sleep intervals.
- `RestTimerStateTransitionTest.kt`: Validates state machine transitions between Idle, Running, Paused, and Expired states.

---

## 3. Risk Area 3: Personal Record (PR) Logic & Warm-up Immunity

### The Risk
Warm-up sets (e.g. 60kg for 12 reps) performed before heavy working sets (100kg for 8 reps) could inadvertently trigger false "Most Reps" PRs or artificially inflate total workout volume.

### Mitigation Architecture
- **Explicit Domain Flags on `SetType`**:
  - `WARMUP`: `isWarmup = true`, `countsForVolume = false`, `countsForPr = false`.
  - `NORMAL`, `DROP_SET`, `FAILURE`: `isWarmup = false`, `countsForVolume = true`, `countsForPr = true`.
- **Personal Record Detector Guard**: `PersonalRecordDetector.detectPersonalRecords` immediately aborts and returns an empty list if `isWarmup == true` or `weight <= 0.0` or `reps <= 0`.
- **Workout Volume Guard**: `VolumeCalculator.calculateTotalWorkoutVolume` iterates through completed sets, filtering out warm-ups prior to summation: `sets.filter { it.setType.countsForVolume }`.

### Verification Proof
- `PersonalRecordDetectorTest.kt`: Proves warm-up sets breaking all historic records yield zero PR badges.
- `VolumeCalculatorTest.kt`: Proves warm-up set volumes are strictly evaluated as `0.0`.
- `SetTypeRuleVerificationTest.kt`: Verifies the exhaustive truth table for all set types.

---

## 4. Risk Area 4: Analytics Charts, Ranges, & Empty History

### The Risk
New users opening the analytics or progress screen with zero logged workouts could cause crashes, divide-by-zero exceptions, or NaN rendering on Canvas charts.

### Mitigation Architecture
- **Fallback Min/Max Bounds**: In `CustomLineChart.kt` and `CustomBarChart.kt`, if data points are empty or all values are equal (e.g., all 0.0), default safe bounds `[0.0, 10.0]` are applied to prevent divide-by-zero during coordinate normalization.
- **Empty State Previews**: If an exercise or workout period has no logged history, screens render friendly, motivating empty states (`EmptyStateView`) instead of empty Canvas containers.
- **Date Boundary Normalization**: Weekly volume buckets safely handle year-end week transitions (Week 52 to Week 1) without sorting anomalies.

### Verification Proof
- `AnalyticsRepositoryAggregatesTest.kt`: Validates empty DAO results return empty lists without throwing.
- `WorkoutCalculationsComprehensiveTest.kt`: Stress tests zero and negative values.
- `verify_fitlog_suite.py` (Round 10): Confirms chart range safety under empty history conditions.

---

## 5. Risk Area 5: PDF-Free Scope Enforcement

### The Risk
Adding bloated PDF generation libraries introduces heavy binary dependencies (e.g., iText, PdfDocument), increases APK size, and conflicts with the user's explicit requirement: "This app does not need PDF generation; do not let the agent add unnecessary features."

### Mitigation Architecture
- **Zero PDF Dependencies**: No PDF libraries or classes exist in `build.gradle.kts` or the codebase.
- **Standard CSV & JSON Exports**: FitLog Pro delivers data safety through lightweight, universally portable CSV files (`CsvExporter.kt`) and structured JSON backups (`BackupManager.kt`).
- **Clean Scope**: Strict adherence to the locked feature set prevents scope bloat.
