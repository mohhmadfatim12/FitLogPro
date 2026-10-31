#!/bin/sh
# FitLog Pro - GitHub Push Helper Script
# Usage: ./push_to_github.sh <your-github-username> <repo-name>

if [ -z "$1" ]; then
    echo "==========================================================="
    echo "  FitLog Pro — GitHub Push Helper"
    echo "==========================================================="
    echo "Usage: ./push_to_github.sh <github-username> [repo-name]"
    echo ""
    echo "Example: ./push_to_github.sh john-doe FitLogPro"
    echo "==========================================================="
    exit 1
fi

USERNAME=$1
REPO=${2:-FitLogPro}

echo "Setting remote origin to https://github.com/$USERNAME/$REPO.git ..."
git remote remove origin 2>/dev/null
git remote add origin "https://github.com/$USERNAME/$REPO.git"
git branch -M main

echo "Pushing code to main branch..."
git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Successfully pushed to GitHub!"
    echo "👉 GitHub Actions is now building your APK at:"
    echo "   https://github.com/$USERNAME/$REPO/actions"
else
    echo ""
    echo "⚠️ Push failed. Please verify that:"
    echo " 1. The repository https://github.com/$USERNAME/$REPO exists on your GitHub."
    echo " 2. You have entered valid GitHub credentials or Personal Access Token (PAT)."
fi
