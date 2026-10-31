#!/usr/bin/env bash
set -e

echo "============================================================"
echo "  FitLog Pro — Automated Test Execution Harness"
echo "============================================================"

# 1. Run algorithmic 12-round verification suite
echo ""
echo "[Step 1/2] Running Python 12-Round Algorithmic Verification..."
python3 verify_fitlog_suite.py

# 2. Check for Java environment to execute JVM unit tests
echo ""
echo "[Step 2/2] Checking local JDK availability for Gradle unit tests..."
if command -v java >/dev/null 2>&1; then
    echo "  -> Java found. Executing ./gradlew testDebugUnitTest..."
    ./gradlew testDebugUnitTest --continue
else
    echo "  -> Local JVM not detected in PATH (Termux minimal environment)."
    echo "  -> Note: Full JVM tests execute automatically in GitHub Actions CI via .github/workflows/build-apk.yml on Eclipse Temurin JDK 17."
fi

echo ""
echo "============================================================"
echo "  ✅ All local verification checks passed successfully!"
echo "============================================================"
