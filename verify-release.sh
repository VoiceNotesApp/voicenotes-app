#!/bin/bash

# Release verification script for Voice Notes Android App
# Verifies that the release build is properly configured before distribution

set -e  # Exit on error

echo "========================================"
echo "Voice Notes - Release Verification"
echo "========================================"
echo ""

ERRORS=0
WARNINGS=0

# Function to report error
error() {
    echo "❌ ERROR: $1"
    ERRORS=$((ERRORS + 1))
}

# Function to report warning
warning() {
    echo "⚠️  WARNING: $1"
    WARNINGS=$((WARNINGS + 1))
}

# Function to report success
success() {
    echo "✅ $1"
}

echo "Checking build configuration..."
echo ""

# Check 1: ProGuard is enabled
echo "1. ProGuard Configuration"
if grep -q "minifyEnabled true" voicenotes/build.gradle; then
    success "ProGuard is enabled"
else
    error "ProGuard is NOT enabled - release builds must have minifyEnabled true"
fi

if grep -q "shrinkResources true" voicenotes/build.gradle; then
    success "Resource shrinking is enabled"
else
    warning "Resource shrinking is NOT enabled - consider enabling for smaller APK"
fi
echo ""

# Check 2: Signing configuration
echo "2. Signing Configuration"
if [ -f "keystore.properties" ]; then
    success "Keystore configuration found"
    
    # Verify keystore file exists
    if grep -q "storeFile=" keystore.properties; then
        KEYSTORE_FILE=$(grep "storeFile=" keystore.properties | cut -d'=' -f2)
        if [ -f "$KEYSTORE_FILE" ]; then
            success "Keystore file exists: $KEYSTORE_FILE"
        else
            error "Keystore file not found: $KEYSTORE_FILE"
        fi
    fi
else
    error "keystore.properties not found - release APK will be unsigned"
fi
echo ""

# Check 3: Version is not "unknown"
echo "3. Version Configuration"
if grep -q '"0.0.0-unknown"' voicenotes/build.gradle; then
    error "Version fallback is still 0.0.0-unknown (should be 1.0.0-unknown)"
elif grep -q '"1.0.0-unknown"' voicenotes/build.gradle; then
    success "Version fallback is correctly set to 1.0.0-unknown"
else
    warning "Unable to verify version fallback"
fi

# Check current version
echo "   Checking current version..."
if command -v git &> /dev/null; then
    CURRENT_VERSION=$(git describe --tags --always --dirty 2>/dev/null || echo "unknown")
    if [[ "$CURRENT_VERSION" == *"unknown"* ]]; then
        warning "Current version is '$CURRENT_VERSION' - consider tagging the release"
    else
        success "Current version: $CURRENT_VERSION"
    fi
else
    warning "Git not available - cannot check version"
fi
echo ""

# Check 4: Credentials configuration
echo "4. API Credentials"
if [ -f "gradle.properties" ]; then
    success "gradle.properties found"
    
    if grep -q "GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON" gradle.properties; then
        success "Google Cloud credentials configured"
    else
        warning "Google Cloud credentials not configured - transcription will not work"
    fi
else
    warning "gradle.properties not found - Google Cloud features will not work"
fi
echo ""

# Check 5: ProGuard rules exist
echo "5. ProGuard Rules"
if [ -f "voicenotes/proguard-rules.pro" ]; then
    success "ProGuard rules file exists"
    
    # Check for critical rules
    if grep -q "androidx.room" voicenotes/proguard-rules.pro; then
        success "Room database rules present"
    else
        warning "Room database rules may be missing"
    fi
    
    if grep -q "com.google.cloud" voicenotes/proguard-rules.pro; then
        success "Google Cloud rules present"
    else
        warning "Google Cloud rules may be missing"
    fi
    
    if grep -q "kotlinx.coroutines" voicenotes/proguard-rules.pro; then
        success "Kotlin coroutines rules present"
    else
        warning "Kotlin coroutines rules may be missing"
    fi
else
    error "ProGuard rules file not found"
fi
echo ""

# Check 6: Lint configuration
echo "6. Lint Configuration"
if [ -f "voicenotes/lint.xml" ]; then
    success "Lint configuration exists"
else
    warning "Lint configuration not found"
fi

# Try to run lint
if command -v ./gradlew &> /dev/null; then
    echo "   Running lint checks..."
    LINT_OUTPUT=$(./gradlew lintRelease --quiet 2>&1)
    LINT_EXIT=$?
    if [ $LINT_EXIT -eq 0 ]; then
        success "Lint checks passed"
    else
        warning "Lint checks failed or have warnings - review lint report"
        echo "   Run './gradlew lintRelease' for details"
    fi
else
    warning "Unable to run lint checks"
fi
echo ""

# Check 7: Unit tests
echo "7. Unit Tests"
if [ -d "voicenotes/src/test" ]; then
    TEST_COUNT=$(find voicenotes/src/test -name "*.kt" | wc -l)
    if [ "$TEST_COUNT" -gt 0 ]; then
        success "Found $TEST_COUNT unit test files"
        
        # Try to run tests
        if command -v ./gradlew &> /dev/null; then
            echo "   Running unit tests..."
            TEST_OUTPUT=$(./gradlew test --quiet 2>&1)
            TEST_EXIT=$?
            if [ $TEST_EXIT -eq 0 ]; then
                success "All tests passed"
            else
                error "Some tests failed - review test report"
                echo "   Run './gradlew test' for details"
            fi
        else
            warning "Unable to run tests"
        fi
    else
        warning "No unit test files found"
    fi
else
    warning "Unit test directory not found"
fi
echo ""

# Check 8: Documentation
echo "8. Documentation"
[ -f "README.md" ] && success "README.md exists" || warning "README.md not found"
[ -f "CHANGELOG.md" ] && success "CHANGELOG.md exists" || warning "CHANGELOG.md not found"
[ -f "LICENSE" ] && success "LICENSE exists" || warning "LICENSE not found"
[ -f "docs/SECURITY.md" ] && success "SECURITY.md exists" || warning "docs/SECURITY.md not found"
[ -f "docs/DEVELOPER_GUIDE.md" ] && success "DEVELOPER_GUIDE.md exists" || warning "docs/DEVELOPER_GUIDE.md not found"
echo ""

# Check 9: .gitignore
echo "9. Security Checks"
if [ -f ".gitignore" ]; then
    success ".gitignore exists"
    
    if grep -q "gradle.properties" .gitignore; then
        success "gradle.properties is in .gitignore"
    else
        error "gradle.properties is NOT in .gitignore - credentials could be leaked!"
    fi
    
    if grep -q "keystore.properties" .gitignore; then
        success "keystore.properties is in .gitignore"
    else
        error "keystore.properties is NOT in .gitignore - signing keys could be leaked!"
    fi
else
    error ".gitignore not found"
fi
echo ""

# Check 10: Release build exists
echo "10. Release APK"
if [ -f "voicenotes/build/outputs/apk/release/voicenotes-release.apk" ]; then
    success "Release APK found"
    
    # Check APK size
    APK_SIZE=$(stat -f%z "voicenotes/build/outputs/apk/release/voicenotes-release.apk" 2>/dev/null || stat -c%s "voicenotes/build/outputs/apk/release/voicenotes-release.apk" 2>/dev/null || echo "0")
    APK_SIZE_MB=$((APK_SIZE / 1024 / 1024))
    echo "   APK size: ${APK_SIZE_MB} MB"
    
    if [ "$APK_SIZE_MB" -gt 50 ]; then
        warning "APK is quite large (${APK_SIZE_MB} MB) - consider optimizing"
    fi
    
    # Check ProGuard mapping
    if [ -f "voicenotes/build/outputs/mapping/release/mapping.txt" ]; then
        success "ProGuard mapping file exists"
        echo "   ⚠️  REMEMBER: Save mapping.txt for crash report deobfuscation!"
    else
        warning "ProGuard mapping file not found"
    fi
else
    warning "Release APK not found - run ./build-release.sh first"
fi
echo ""

# Summary
echo "========================================"
echo "VERIFICATION SUMMARY"
echo "========================================"
echo ""

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo "🎉 Perfect! All checks passed."
    echo ""
    echo "Your release is ready for distribution!"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo "⚠️  $WARNINGS warning(s) found."
    echo ""
    echo "Your release is mostly ready, but review the warnings above."
    echo "These issues won't prevent distribution but should be addressed."
    exit 0
else
    echo "❌ $ERRORS error(s) and $WARNINGS warning(s) found."
    echo ""
    echo "CRITICAL ISSUES FOUND!"
    echo "Fix the errors above before distributing the release."
    echo ""
    echo "Common fixes:"
    echo "  - Enable ProGuard in voicenotes/build.gradle"
    echo "  - Create and configure keystore.properties"
    echo "  - Update version fallback in voicenotes/build.gradle"
    echo "  - Add credentials to gradle.properties"
    echo "  - Fix failing tests"
    echo ""
    exit 1
fi
