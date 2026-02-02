#!/bin/bash

# Release build script for Voice Notes Android App
# This script builds the production-ready release APK with ProGuard enabled

set -e  # Exit on error

echo "========================================"
echo "Voice Notes - Release Build Script"
echo "========================================"
echo ""

# Check if Android SDK is set
if [ -z "$ANDROID_HOME" ] && [ ! -f "local.properties" ]; then
    echo "ERROR: Android SDK not found!"
    echo ""
    echo "Please set ANDROID_HOME environment variable or create local.properties file"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed!"
    echo "Please install Java JDK 17 or higher"
    exit 1
fi

echo "Java version:"
java -version
echo ""

# Check if keystore is configured
if [ ! -f "keystore.properties" ]; then
    echo "WARNING: keystore.properties not found!"
    echo "Release APK will be built but NOT signed."
    echo "To sign the APK, create keystore.properties with your signing configuration."
    echo "See docs/SECURITY.md for details."
    echo ""
    read -p "Continue with unsigned build? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Check if credentials are configured
if [ ! -f "gradle.properties" ]; then
    echo "WARNING: gradle.properties not found!"
    echo "App will build but Google Cloud features will not work."
    echo "See docs/SECURITY.md for credential setup."
    echo ""
fi

# Make gradlew executable
chmod +x gradlew

echo "Running lint checks..."
./gradlew lintRelease || {
    echo ""
    echo "WARNING: Lint checks failed!"
    echo "Review the lint report: voicenotes/build/reports/lint-results-release.html"
    echo ""
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
}

echo ""
echo "Running unit tests..."
./gradlew test || {
    echo ""
    echo "WARNING: Some tests failed!"
    echo ""
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
}

echo ""
echo "Building release APK with ProGuard enabled..."
echo ""

# Clean and build release
./gradlew clean assembleRelease

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "BUILD SUCCESSFUL!"
    echo "========================================"
    echo ""
    
    # Check if APK was signed
    if [ -f "keystore.properties" ]; then
        echo "✓ Release APK is signed and ready for distribution"
        echo ""
        echo "APK location: voicenotes/build/outputs/apk/release/voicenotes-release.apk"
    else
        echo "⚠ Release APK is unsigned"
        echo ""
        echo "APK location: voicenotes/build/outputs/apk/release/voicenotes-release-unsigned.apk"
        echo ""
        echo "To sign the APK, configure keystore.properties and rebuild"
        echo "See docs/SECURITY.md for signing instructions"
    fi
    
    echo ""
    echo "ProGuard mapping file: voicenotes/build/outputs/mapping/release/mapping.txt"
    echo "⚠ IMPORTANT: Save the mapping file for crash report deobfuscation!"
    echo ""
    echo "To install on a connected device:"
    echo "  adb install voicenotes/build/outputs/apk/release/voicenotes-release.apk"
    echo ""
    
    # Show APK info
    if command -v aapt &> /dev/null; then
        echo "APK Information:"
        if [ -f "voicenotes/build/outputs/apk/release/voicenotes-release.apk" ]; then
            aapt dump badging voicenotes/build/outputs/apk/release/voicenotes-release.apk | grep -E "package:|versionCode|versionName"
        fi
        echo ""
    fi
    
else
    echo ""
    echo "========================================"
    echo "BUILD FAILED!"
    echo "========================================"
    echo ""
    echo "Please check the error messages above"
    echo "Common issues:"
    echo "  - ProGuard configuration errors (check proguard-rules.pro)"
    echo "  - Missing dependencies"
    echo "  - Code errors that only appear with ProGuard enabled"
    echo ""
    exit 1
fi
