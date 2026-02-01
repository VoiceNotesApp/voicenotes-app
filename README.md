# Voice Notes

Android app for recording GPS-tagged voice notes.

## Features

- Automatic recording with GPS coordinates
- Bluetooth audio support
- Text-to-speech location announcement
- Recording Manager for playback and export
- Optional Google Cloud transcription
- Export formats: Audio, GPX, CSV

## Requirements

- Android 8.0+ (API 26)
- Permissions: Microphone, Location, Bluetooth, Overlay

## Installation

Download APK from [Releases](https://github.com/c0dev0id/autorecord-app/releases).

## Usage

Two launcher icons:
- **Voice Notes**: Record (auto-quits when done)
- **VN Manager**: Settings and recording management

## Building

```bash
./gradlew assembleDebug
```

See [Developer Guide](docs/DEVELOPER_GUIDE.md) for release builds.

## Documentation

- [User Guide](docs/USER_GUIDE.md)
- [Developer Guide](docs/DEVELOPER_GUIDE.md)
- [Architecture](docs/ARCHITECTURE.md)

## License

MIT License - see [LICENSE](LICENSE).
