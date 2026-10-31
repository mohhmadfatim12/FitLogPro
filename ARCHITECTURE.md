# FitLog Pro — System Architecture & Design Specification

## 1. Architectural Philosophy

FitLog Pro is built around **Clean Architecture**, **Unidirectional Data Flow (UDF)**, and **Strict Offline-First Operation**. Designed specifically for gym environments where cellular data may be spotty or absent, the app treats the local device database as the single, authoritative source of truth.

```
┌─────────────────────────────────────────────────────────┐
│                       UI Layer                          │
│   Jetpack Compose Screens + Material 3 Gym Components   │
│                 StateFlow / UI Events                   │
└────────────────────────────┬────────────────────────────┘
                             │ Observes State / Emits Events
┌────────────────────────────▼────────────────────────────┐
│                    ViewModel Layer                      │
│   ActiveWorkoutViewModel, RoutinesViewModel, etc.       │
└──────────────┬────────────────────────────┬─────────────┘
               │ Calls Use Cases / Domain   │ Reads/Writes Data
┌──────────────▼─────────────┐ ┌────────────▼─────────────┐
│       Domain Layer         │ │        Data Layer        │
│ • OneRepMaxCalculator      │ │ • Room Database & DAOs   │
│ • VolumeCalculator         │ │ • Repositories           │
│ • PersonalRecordDetector   │ │ • UserPreferencesManager │
│ • RestTimerManager         │ │ • CsvExporter & Backup   │
│ • Cardio & RpeCalculators  │ └──────────────────────────┘
└────────────────────────────┘
```

---

## 2. Layered Breakdown

### 2.1 UI Layer (Jetpack Compose & Material 3)
- **Declarative Composable Functions**: Screens are stateless functions accepting UI state and event callbacks.
- **Gym-Friendly Design System**: High-contrast, large touch targets (minimum 48dp), numeric keypad optimization, and high-visibility badges (`SetTypeBadge`, `PRBadge`).
- **Dynamic Theming**: Full Material 3 dark/light palettes optimized for OLED power conservation during long gym sessions.
- **Keep-Screen-On**: Integrates with Android `FLAG_KEEP_SCREEN_ON` via window management during active workout logging.

### 2.2 ViewModel Layer
- **Unidirectional Data Flow (UDF)**: ViewModels expose immutable `StateFlow<T>` or `SharedFlow<T>` to composables.
- **State Restoration**: Interacts with `ActiveWorkoutRepository` to restore active workout state upon process recreation or configuration change.
- **Coroutines & Scopes**: Structured concurrency bound to `viewModelScope` to prevent memory leaks.

### 2.3 Domain Layer (Pure Kotlin Business Logic)
- **Zero Android Framework Dependencies**: Calculators and algorithms are written in standard Kotlin with 100% JVM unit testability.
- **OneRepMaxCalculator**: Implements the validated Epley formula `Weight × (1 + Reps / 30)` with single-rep identity `1 RM = Weight`.
- **VolumeCalculator**: Computes workload volume `Sum(Weight × Reps)`, enforcing strict exclusion of warm-up sets (`countsForVolume = false`).
- **PersonalRecordDetector**: Evaluates 4 distinct PR metrics (Heaviest Weight, Best 1RM, Best Set Volume, Most Reps) with warm-up immunity.
- **RestTimerManager**: Background-tolerant timer using epoch target timestamps (`System.currentTimeMillis() + remainingMs`) to eliminate background drift.
- **RpeCalculator & CardioCalculator**: RPE half-step rounding, RIR conversion, and running pace/speed calculators.

### 2.4 Data Layer (Room SQLite & DataStore)
- **Room Database 2.6.1**: Relational SQLite database with foreign keys, cascading deletions, and indexed queries.
- **Active Workout Autosave**: Live sessions are continuously serialized to Room every 5 seconds and on every set completion.
- **DataStore Preferences**: Fast asynchronous storage for user unit settings (Metric vs. Imperial), dark mode, and default timer intervals.
- **Relational Integrity**: Workouts cascade delete associated exercises and sets, preventing orphan records.

---

## 3. High-Risk Resilience Systems

### 3.1 Process-Death Crash Recovery
1. When a workout starts, an `ActiveWorkoutSession` entity is saved with ID `"CURRENT_ACTIVE_SESSION"`.
2. As sets are edited, toggled, or added, the complete JSON snapshot is synced to SQLite.
3. If the Android OS terminates the process due to memory pressure or the user switches apps:
   - Upon relaunch, `ActiveWorkoutViewModel` detects the pending session in Room.
   - The session reconstitutes completely: elapsed time, logged weights, completed checkboxes, and superset associations.
   - Upon workout completion or deliberate discard, the active session row is purged.

### 3.2 Rest Timer Backgrounding
- Traditional Android timers stop or drift when the device sleeps.
- FitLog Pro computes `targetEndTimeEpochMs = nowMs + durationMs`.
- When the screen wakes or the app returns to foreground, the remaining seconds are calculated via `(targetEndTimeEpochMs - nowMs) / 1000`.
- System notification / vibration alerts fire upon target epoch arrival.

---

## 4. Testing & Verification Architecture
- **Algorithmic JVM Unit Tests**: High-speed, regression-proof tests for all mathematical calculators.
- **Repository Integration Tests**: Fake DAOs and in-memory databases verifying relational mapping and transactional bounds.
- **Automated Verification Suite**: Self-contained 10-round verification script (`verify_fitlog_suite.py`) validating critical edge cases.
