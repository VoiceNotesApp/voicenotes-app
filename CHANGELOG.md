# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Removed
- On-Device Test Suite (TestSuite.kt)
- Instrumentation tests (SettingsActivityInstrumentationTest.kt)
- Debug Logging toggle UI from Debug Log screen
- Firebase Test Lab integration from CI/CD workflows

### Added
- MIT License
- Comprehensive ProGuard/R8 configuration for release builds
- Unit test suite with 5 test categories
- Lint configuration for code quality
- Security documentation and best practices
- Release verification script
- CHANGELOG.md for version history tracking

### Changed
- Enabled ProGuard/R8 minification for release builds
- Version fallback changed from 0.0.0-unknown to 1.0.0-unknown
- Updated documentation with production build guidelines

### Fixed
- Production build configuration
- Release APK optimization

## [0.0.3] - 2026-01-25

### Added
- Database test isolation with in-memory test database
- Comprehensive on-device test suite (82 tests)
- Recording operations testing
- Error handling test coverage
- Debug log viewer with test execution

### Changed
- Improved database migration handling
- Enhanced error reporting in test suite

### Fixed
- Database test contamination issues
- Test reliability improvements

## [0.0.2] - 2026-01-20

### Added
- Recording Manager activity for post-recording management
- Batch processing service for transcription
- Multiple export formats (Audio, GPX, CSV)
- Google Cloud Speech-to-Text integration
- Status tracking with color-coded UI

### Changed
- Separated recording and management into two launcher icons
- Improved UI with Material Design cards

### Fixed
- Audio playback reliability
- GPS coordinate accuracy

## [0.0.1] - 2026-01-15

### Added
- Initial release
- Automatic GPS-tagged voice recording
- Hands-free operation
- Text-to-speech location announcement
- Bluetooth headset support
- Configurable recording duration (1-99 seconds)
- Room database for recording persistence
- Overlay bubble during recording

### Features
- Android 8.0+ support (API 26+)
- OGG/Opus format on Android 10+ (API 29+)
- AMR-WB format on Android 8-9 (API 26-28)
- Google Play Services location integration

[Unreleased]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.3...HEAD
[0.0.3]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/c0dev0id/autorecord-app/releases/tag/v0.0.1
