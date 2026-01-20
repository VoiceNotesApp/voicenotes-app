#!/bin/bash

# Build script for Motorcycle Voice Notes Android App
# This script builds the debug APK

echo "=================================="
echo "Motorcycle Voice Notes - Build Script"
echo "=================================="
echo ""

# Check if Android SDK is set
if [ -z "$ANDROID_HOME" ] && [ ! -f "local.properties" ]; then
    echo "ERROR: Android SDK not found!"
    echo ""
    echo "Please set ANDROID_HOME environment variable or create local.properties file"
    echo "See BUILD_INSTRUCTIONS.md for details"
    echo ""
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed!"
    echo "Please install Java JDK 8 or higher"
    exit 1
fi

echo "Java version:"
java -version
echo ""

# Make gradlew executable
chmod +x gradlew

echo "Starting build..."
echo ""

# Clean and build
./gradlew clean assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "=================================="
    echo "BUILD SUCCESSFUL!"
    echo "=================================="
    echo ""
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "To install on a connected device:"
    echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
    echo ""
else
    echo ""
    echo "=================================="
    echo "BUILD FAILED!"
    echo "=================================="
    echo ""
    echo "Please check the error messages above and see BUILD_INSTRUCTIONS.md"
    echo ""
    exit 1
fi
