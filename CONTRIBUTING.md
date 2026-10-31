# Contributing to FitLog Pro

Thank you for contributing to **FitLog Pro — Workout Tracker**! To maintain clean code quality, stability, and data integrity in gym environments, please adhere to these guidelines.

---

## 1. Development Principles

1. **Offline-First:** All user data (exercises, routines, logs, PRs, body metrics) must persist locally in Room SQLite without requiring network access.
2. **Crash Resilience:** Never allow in-progress workout sessions to be lost on process death or configuration change. Always sync active state via `ActiveWorkoutRepository`.
3. **Strict Math Integrity:**
   - Estimated 1RM strictly follows Epley: `Weight × (1 + Reps / 30)`.
   - Single rep identity: `1 rep = exact weight`.
   - Workout volume strictly excludes warm-up sets (`countsForVolume = false`).
   - Warm-up sets must never generate PR badges (`countsForPr = false`).
4. **PDF-Free Scope:** Do not add PDF generation libraries or cloud dependencies. Export data via standard CSV and JSON backups.

---

## 2. Commit Message Conventions

We follow the **Conventional Commits** specification:

```
<type>(<scope>): <short description>
```

### Allowed Types:
- `feat`: A new user-facing feature or domain capability.
- `fix`: A bug fix or calculation correction.
- `test`: Adding or updating unit/integration tests.
- `docs`: Documentation updates, architecture guides, schemas.
- `refactor`: Code reorganization without behavior change.
- `perf`: Performance improvement (e.g. database indexing).
- `chore`: Build script, dependencies, or metadata changes.

### Atomic Commits Rule:
Each commit should represent a single logical unit of work with a descriptive message and passing test verification.

---

## 3. Pull Request & Verification Checklist

Before submitting a pull request or merging into `main`:
1. [ ] Run unit tests: `./gradlew testDebugUnitTest`
2. [ ] Run 10-round verification: `python3 verify_fitlog_suite.py` (Must pass 25/25 checks)
3. [ ] Verify debug build: `./gradlew assembleDebug`
4. [ ] Ensure no regressions in warm-up exclusion or rest timer backgrounding.
