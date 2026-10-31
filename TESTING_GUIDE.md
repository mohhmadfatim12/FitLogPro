# FitLog Pro — Comprehensive Testing & Verification Guide

## 1. Testing Strategy

FitLog Pro utilizes a multi-layered testing strategy to guarantee mathematical correctness, relational data safety, and crash-resilient UI state.

```
┌────────────────────────────────────────────────────────┐
│           10-Round Verification Suite                  │
│       verify_fitlog_suite.py (25/25 automated)         │
├────────────────────────────────────────────────────────┤
│           JVM Unit & Integration Tests                 │
│       ./gradlew testDebugUnitTest                      │
├────────────────────────────────────────────────────────┤
│           CI Cloud Automated Build Pipeline            │
│       .github/workflows/build-apk.yml (Temurin JDK 17) │
└────────────────────────────────────────────────────────┘
```

---

## 2. Test Execution Commands

### 2.1 Standard Gradle Unit Tests
To run all unit and integration tests across the domain, repository, database, and export layers:
```bash
./gradlew testDebugUnitTest
```

### 2.2 Standard APK Build
To build the complete debug APK:
```bash
./gradlew clean assembleDebug
```
The output artifact is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2.3 On-Device Automated Verification Suite
For instant algorithmic verification in environments without a full local JDK installation:
```bash
python3 verify_fitlog_suite.py
```

### 2.4 Live Web Preview Server
To test and interact with the full FitLog Pro mobile app UI in any browser:
```bash
./start_preview.sh
# Open http://localhost:8081 in Chrome, Brave, or Firefox
```

---

## 3. Test Suite Inventory

### Domain Layer Tests
| Test Class | Focus |
|---|---|
| `OneRepMaxCalculatorTest.kt` | Epley formula, single-rep identity, 0/negative weights |
| `OneRepMaxFormulaComparisonTest.kt` | Epley vs. Brzycki formula convergence across 1–12 reps |
| `VolumeCalculatorTest.kt` | Strict warm-up exclusion from workout and set volume |
| `PersonalRecordDetectorTest.kt` | 4 PR types, warm-up immunity, first-time exercise detection |
| `UnitConverterTest.kt` | Bidirectional kg/lbs, km/miles, cm/inches round-trips |
| `UnitSystemFormattingTest.kt` | Metric/imperial unit resolution and display strings |
| `StreakCalculatorTest.kt` | Daily and weekly workout streaks, missed weeks, current week |
| `StreakCalculatorComprehensiveTest.kt` | Year-end boundary rollovers, multi-workout single days |
| `RestTimerManagerTest.kt` | Background-tolerant target epoch calculation, pause/resume |
| `RestTimerBackgroundToleranceTest.kt` | Deep-sleep wake simulations, rapid +30s / -15s boundary clamps |
| `RestTimerStateTransitionTest.kt` | Idle, running, paused, expired state machine lifecycle |
| `CardioCalculationsTest.kt` | Running pace (`min:sec /km`), speed (`km/h`), duration formatting |
| `RpeValidationTest.kt` | RPE 6.0–10.0 clamping, 0.5 step rounding, Reps In Reserve (RIR) |
| `WorkoutDurationCalculatorTest.kt` | Active gym duration vs wall-clock elapsed time with pauses |
| `SupersetGroupingTest.kt` | Superset grouping logic, pair/triad labeling ("A1", "A2") |
| `SetTypeRuleVerificationTest.kt` | Truth tables for `isWarmup`, `countsForVolume`, `countsForPr` |
| `PRTypeEvaluationTest.kt` | PR metadata, unit suffixes, and string parsing |
| `MeasurementTypeUnitMappingTest.kt` | Metric vs imperial unit resolution across all body parts |

### Data, Repository & Export Tests
| Test Class | Focus |
|---|---|
| `WorkoutCalculationsComprehensiveTest.kt` | End-to-end workout volume and PR detection stress tests |
| `CsvExporterTest.kt` | Workouts, exercises, and measurements CSV export schemas |
| `CsvFormattingSafetyTest.kt` | Quotes, commas, and newlines in workout notes escaping |
| `BackupValidationTest.kt` | JSON backup parsing and foreign app rejection |
| `BackupRestoreSafetyTest.kt` | Corrupted JSON, invalid versions, missing arrays handling |
| `ProcessRecoveryStateTest.kt` | Active session JSON serialization / deserialization fidelity |
| `ActiveWorkoutRecoveryTest.kt` | Crash recovery of paused workouts, cardio metrics, and clear |
| `ExerciseRepositoryTest.kt` | Exercise search, muscle filtering, custom exercise insertion |
| `RoutineRepositoryTest.kt` | CrossRef mapping, superset groups, last performed timestamps |
| `WorkoutRepositoryIntegrationTest.kt` | Transactional workout saving, set insertion, PR updates |
| `BodyMeasurementRepositoryTest.kt` | Measurement history and timestamp ordering |
| `AnalyticsRepositoryAggregatesTest.kt` | Muscle distribution percentages and empty history handling |
| `ExerciseSeedDataTest.kt` | 45+ built-in exercise integrity and muscle group coverage |
| `RoomTypeConvertersSafetyTest.kt` | TypeConverter enum round-trips, null safety, corrupt fallbacks |

---

## 4. Continuous Integration (CI) Matrix

Automated GitHub Actions workflow located at `.github/workflows/build-apk.yml`:
- **Runner:** `ubuntu-latest`
- **JDK:** Eclipse Temurin JDK 17
- **Steps:**
  1. Checkout repository with full depth.
  2. Setup JDK 17 with Gradle caching.
  3. Validate Gradle Wrapper integrity.
  4. Run `./gradlew testDebugUnitTest --continue`.
  5. Run `./gradlew assembleDebug`.
  6. Upload debug APK as build artifact.
