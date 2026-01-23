# AI Instructions for Autorecord App Repository

This file contains instructions for AI assistants (GitHub Copilot, Claude, etc.) working on this repository.

## Global Pull Request Workflow Rules

When creating or working on a pull request (PR):

### 1. Automatic Workflow Triggering

- **Always trigger all relevant check workflows** when creating or updating a PR
- The repository has the following automated workflows that will run automatically:
  - **PR Check Workflow** (`pr-check.yml`): Runs lint checks and build verification
    - Lint Check: Validates code quality and style
    - Build Check: Compiles debug APK and verifies successful build
  - **Android Build Workflow** (`android-build.yml`): Builds debug APK for testing
  
- These workflows are automatically triggered on:
  - Opening a new PR
  - Pushing new commits to an existing PR
  - Reopening a PR
  
- **No manual intervention required** - workflows trigger automatically via GitHub Actions

### 2. Ready for Review Status

- Once a PR is created and initial commits are pushed:
  1. Wait for all automated checks to complete (lint, build, tests)
  2. Monitor the status of all workflows in the PR
  3. If all checks pass successfully:
     - The PR should be marked as **"Ready for review"**
     - This indicates to maintainers that the PR has passed automated validation
  4. If any checks fail:
     - Review the failure logs
     - Fix the issues
     - Push corrected commits
     - Repeat until all checks pass

### 3. Workflow Verification

Before marking a PR as ready:
- ✅ Lint checks must pass
- ✅ Build must complete successfully
- ✅ All automated tests must pass (if applicable)
- ✅ No merge conflicts
- ✅ Code follows repository conventions

### 4. Minimal Manual Interaction

The goal is to minimize manual steps:
- Workflows trigger automatically on PR events
- Status checks update automatically
- Only manual step: marking as "Ready for review" after verifying checks pass
- All build artifacts (APKs, reports) are automatically uploaded

## Repository-Specific Context

### Android Development
- This is an Android application built with Gradle
- Java version: 17 (Temurin distribution)
- Build tool: Gradle wrapper (`./gradlew`)
- Primary build commands:
  - Lint: `./gradlew lint`
  - Build: `./gradlew assembleDebug`

### Code Quality Standards
- All code must pass Android lint checks
- Build must succeed with no errors
- APK must be generated successfully in `app/build/outputs/apk/debug/`

### Workflow Artifacts
- Lint reports are retained for 7 days
- Build APKs are available as workflow artifacts
- PR comments are automatically added with build status

## Best Practices

1. **Before creating a PR:**
   - Ensure local build passes: `./gradlew assembleDebug`
   - Run local lint: `./gradlew lint`
   - Review changes for quality

2. **After creating a PR:**
   - Monitor workflow execution in the "Actions" tab
   - Check for any failures in the PR checks section
   - Address failures promptly

3. **Communication:**
   - Use PR descriptions to explain changes
   - Reference related issues
   - Note any special considerations for reviewers

## Troubleshooting Workflow Failures

If workflows fail:

1. **Check the workflow logs:**
   - Navigate to the Actions tab
   - Find the failed workflow run
   - Review detailed logs for each step

2. **Common issues:**
   - Lint errors: Review lint report artifacts
   - Build failures: Check Gradle build logs
   - Missing dependencies: Ensure `gradle.properties` is configured

3. **Fix and retry:**
   - Make necessary corrections
   - Commit and push
   - Workflows will automatically re-run

## Notes

- These instructions apply to all PRs in this repository
- Workflows are configured in `.github/workflows/`
- For global AI instruction support across repositories, this pattern can be replicated in other repos
