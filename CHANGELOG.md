# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.0.12] - 2026-01-31

### Added
- VN Manager launcher module for quick access to Recording Manager
- Headless app launch mode that skips UI when all permissions are granted

### Changed
- Migrated Settings, Debug, and Recording Manager screens to Material Components
- Replaced animated spinner with static processing icon for better performance
- Improved permission handling with proper guards for Bluetooth and overlay permissions

### Fixed
- Recording extension and TTS timeout handling for missing permissions
- Race condition in overlay service cleanup

## [0.0.11] - 2026-01-30

### Added
- Status color coding in Recording Manager (green for success, red for error)
- Material Design animations and button ripple effects

### Fixed
- CSV and XML special characters now properly escaped in exports
- Status icon color filter artifacts when recycling views

## [0.0.10] - 2026-01-29

### Changed
- Fail-fast version detection for better error reporting
- Improved Settings navigation flow

### Fixed
- Download button visibility issues in Recording Manager
- Processing animation and button drawable recycling issues

## [0.0.9] - 2026-01-29

_This release was superseded by v0.0.10 and contained the same changes._

## [0.0.8] - 2026-01-28

### Added
- Visual pulse animation for PROCESSING status indicator
- Fallback placeholder display for empty transcriptions

### Changed
- Consolidated developer documentation into single DEVELOPER_GUIDE.md

### Fixed
- Recording Manager transcription truncation
- Playback controls in Recording Manager

## [0.0.7] - 2026-01-27

### Added
- Unrestricted battery optimization for background reliability
- Foreground service support for reliable background recording

### Changed
- Separated MainActivity and Settings into independent task stacks

## [0.0.6] - 2026-01-27

### Fixed
- Build configuration for release APK

## [0.0.5] - 2026-01-27

### Fixed
- ProGuard rules to suppress logging warnings in release builds

## [0.0.4] - 2026-01-26

### Added
- "Open Maps" button to view recording location in external maps app
- NumberPicker for recording duration selection (replaces text input)
- Manual transcription editing in Recording Manager
- Automatic version from git tags
- CI/CD pipeline with caching and parallel execution
- Unit test suite and coverage reporting

### Changed
- Switched to VOICE_COMMUNICATION audio source for automatic gain control
- Redesigned Recording Manager UI with inline transcription editing
- Removed OSM checkbox and simplified settings UI
- Removed storage location text and OAuth status from Settings
- Upgraded Gradle to 8.4, AGP to 8.3.2, and Kotlin to 1.9.23

### Removed
- OpenStreetMap integration (replaced with "Open Maps" button)
- First-run setup dialog
- Legacy file-based batch processing
- SD card storage selection (now uses internal storage only)

### Fixed
- Recording extension bug that reset countdown
- Implicit broadcast to non-exported receiver
- Database test isolation issues

## [0.0.3] - 2026-01-25

### Added
- Launcher icon for direct access to Settings
- Background launch feature - skips MainActivity UI when all permissions granted
- Native OGG/Opus encoding for voice recordings
- Comprehensive test suite with database isolation

### Changed
- Improved thread safety documentation

### Fixed
- MediaRecorder API compatibility issues
- Storage manager API level checks

## [0.0.2] - 2026-01-20

### Added
- Recording Manager activity for post-recording management
- Google Cloud Speech-to-Text transcription
- CSV export for voice note waypoints
- GPX export with transcribed text
- Real-time status list during processing
- OSM username fetch via API
- On-device test suite with debug logging

### Fixed
- OverlayService premature termination during async post-processing
- Settings UI and version display issues
- Speech-to-Text encoding configuration for M4A audio

## [0.0.1] - 2026-01-15

### Added
- Initial release of Motorcycle Voice Notes app
- Automatic GPS-tagged voice recording
- Hands-free operation with configurable duration (1-99 seconds)
- Text-to-speech location announcement
- Bluetooth headset support
- Recording overlay bubble UI
- Room database for recording persistence
- Post-processing pipeline with GPX deduplication
- Recording extension on app relaunch

### Changed
- Audio recording starts after TTS completes (prevents capturing TTS audio)

### Technical
- Android 8.0+ support (API 26+)
- OGG/Opus format on Android 10+ (API 29+)
- AMR-WB format on Android 8-9 (API 26-28)
- Google Play Services location integration
- AppAuth OAuth 2.0 with PKCE support

[Unreleased]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.12...HEAD
[0.0.12]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.11...v0.0.12
[0.0.11]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.10...v0.0.11
[0.0.10]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.9...v0.0.10
[0.0.9]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.8...v0.0.9
[0.0.8]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.7...v0.0.8
[0.0.7]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.6...v0.0.7
[0.0.6]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.5...v0.0.6
[0.0.5]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.4...v0.0.5
[0.0.4]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/c0dev0id/autorecord-app/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/c0dev0id/autorecord-app/releases/tag/v0.0.1
