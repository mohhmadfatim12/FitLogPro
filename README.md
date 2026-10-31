# FitLog Pro — Workout Tracker & Gym Log

[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-purple)]()
[![Room](https://img.shields.io/badge/Room-2.6.1-blue)]()
[![Compose](https://img.shields.io/badge/Compose-Material%203-teal)]()
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange)]()
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34-green)]()

**FitLog Pro** is a medium-to-high complexity Android fitness tracking application engineered specifically for serious gym lifters. Unlike simplistic beginner CRUD apps, FitLog Pro features relational data modeling, crash-resilient live workout logging with process-death recovery, background-tolerant rest timers, personal record (PR) detection, estimated 1RM calculations, progress charts, body circumference tracking, and complete JSON/CSV data safety without cloud dependencies.

---

## 🏋️ Final Locked Feature Set

- **Exercise Library:** 45+ built-in exercises categorized by muscle group and equipment, plus custom exercise creation.
- **Routines & Supersets:** Ordered exercises, target sets/reps/weight, superset grouping (A1, A2), and drag-and-drop reordering.
- **Live Workout Logging:** Start from routines or blank workouts, log weight, reps, RPE, set types (`Normal`, `Warm-up`, `Drop Set`, `Failure`).
- **Cardio Logging:** Distance, duration, pace calculations (`min:sec /km`), and speed (`km/h`).
- **Rest Timer:** Configurable default and per-exercise timer, background-safe target epoch math, pause/resume/skip, +30s/-15s.
- **Previous Performance:** Instant inline preview of previous workout weights and reps while logging sets.
- **Personal Records (PRs):** Automatic detection across 4 categories: *Heaviest Weight*, *Best 1RM*, *Best Set Volume*, and *Most Reps*.
- **Estimated 1RM (Epley):** Calculated using validated formula `Weight × (1 + Reps / 30)` with single-rep identity `1 rep = exact weight`.
- **Workout Volume:** `Sum(Weight × Reps)`, with **strict exclusion of warm-up sets**.
- **History & Calendar:** Dual-view chronological log and interactive calendar heatmap with full workout editing.
- **Analytics & Progress Charts:** Smooth Canvas curves for 1RM, volume, max weight, and muscle group distribution percentages.
- **Body Measurements:** Track body weight, body fat %, chest, waist, arms, thighs, calves, shoulders, and neck over time.
- **Unit Systems:** Full bidirectional support for Metric (`kg`, `km`, `cm`) and Imperial (`lbs`, `miles`, `inches`).
- **Data Safety:** RFC-compliant CSV export for workouts, exercises, and measurements; validated JSON backup & restore.
- **Gym-Friendly UI:** High-contrast Dark/Light Material 3 themes, large touch controls, and keep-screen-on support.

---

## 🚫 Explicit Exclusions (Out of Scope by Design)

- **PDF Generation:** Strictly PDF-free scope. Exports are delivered via universally compatible CSV and JSON.
- **No Cloud/Firebase/Login:** 100% offline-first. Your gym data belongs to you on your device.
- **No Wear OS / Heart Rate Sensors**
- **No Social Feeds or AI Workout Generators**
- **No Food / Calorie Tracking**

---

## 🛡️ Highest-Risk Areas & Countermeasures

| High-Risk Area | Countermeasure | Verification |
|---|---|---|
| **1. Process-Death Recovery** | Room SQLite `ActiveWorkoutSession` autosaves every 5s and on set toggles. Reconstitutes entire workout after OS kill. | `ActiveWorkoutRecoveryTest.kt` |
| **2. Rest Timer Drift** | Uses target epoch timestamps (`nowMs + durationMs`) rather than ticking intervals. Survives deep sleep without drift. | `RestTimerBackgroundToleranceTest.kt` |
| **3. PR Warm-up Immunity** | Domain guard rules: warm-ups have `countsForPr = false` and `countsForVolume = false`. Zero PRs from warm-ups. | `PersonalRecordDetectorTest.kt` |
| **4. Chart Safety** | Coordinate normalization applies default bounds `[0.0, 10.0]` for empty or zero history to prevent divide-by-zero crashes. | `verify_fitlog_suite.py` Round 10 |
| **5. PDF-Free Scope** | Zero PDF libraries in dependencies. Strict clean architecture using CSV and JSON formats. | Audited in `app/build.gradle.kts` |

---

## 💰 Commercial Valuation & Price Recommendation

Because FitLog Pro represents a medium-to-high complexity, production-ready Android codebase with relational Room architecture and 150+ genuine atomic commits, standard pricing is recommended as follows:

| Tier | Price | Scope Included |
|---|:---:|---|
| **Asking Price** | **PKR 12,000** | Full source code, complete git history (150+ commits), CI workflow, and support. |
| **Strong Acceptable Price** | **PKR 10,000** | Full source code, verified APK, documentation, and fixture files. |
| **Minimum Floor Price** | **PKR 9,000** | Source code only, no post-delivery modifications. |

*Note: Do not quote below PKR 9,000 unless removing core modules such as charts, routines, or backup systems.*

---

## 🛠️ Build & Verification Instructions

### 1. Run All Unit & Integration Tests:
```bash
./gradlew testDebugUnitTest
```

### 2. Build Debug APK:
```bash
./gradlew clean assembleDebug
```
Output APK location:
`app/build/outputs/apk/debug/app-debug.apk`

### 3. Run Automated 10-Round Verification Suite:
```bash
python3 verify_fitlog_suite.py
```

### 4. Run Interactive Local Web Preview:
```bash
./start_preview.sh
# Open http://localhost:8081 in any browser
```

---

## 🏛️ Architecture & Documentation Links
- [System Architecture](ARCHITECTURE.md)
- [Database Schema & ER Diagrams](DATABASE_SCHEMA.md)
- [Risk Mitigation Matrix](RISK_MITIGATION.md)
- [Testing & Verification Guide](TESTING_GUIDE.md)
- [10-Round Verification Report](VERIFICATION_REPORT.md)
- [Contributing Guidelines](CONTRIBUTING.md)
- [Third-Party Notices](THIRD_PARTY_NOTICES.md)
