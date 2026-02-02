[<img src="assets/banners/banner-07-features.svg">](https://github.com/c0dev0id/autorecord-app/releases/latest)

# Voice Notes App (designed for Motorcycle Riders)

Android app for recording GPS-tagged voice notes.

This application is designed specifically for motorcycle riders who need to capture location-based voice notes while on the road.

## Quick Recording Workflow

Assign the Voice Note app to a quick-launch button on your navigation system, device, or remote control. When activated, the app will:

- Capture current GPS coordinates
- Record audio for a configurable duration
- Save the recording and exit automatically

## Recording Management

The Recording Manager provides the following capabilities:

- Playback of recorded notes
- Transcription of audio to text (requires internet connection)
- Export recordings in multiple formats: audio files, CSV, or GPX

## Special Features

**Extended Recording**: When the app is triggered via a quick-launch button, pressing the button again during an active recording will extend the recording duration.

**Batch Export**: Use the download-all function on the top right in the Recording Manager to generate a single GPX or CSV file containing all recorded waypoints with their associated transcribed text.

## Features

- Automatic recording with GPS coordinates
- Bluetooth audio support
- Text-to-speech location announcement
- Recording Manager for playback and export
- Google Cloud transcription (requires internet)
- Export formats: Audio, GPX, CSV

## Requirements

- Android 8.0+ (API 26)
- Permissions: Microphone, Location, Bluetooth, Overlay

## Installation

Download APKs from [Releases](https://github.com/c0dev0id/autorecord-app/releases).

## Usage

Two launcher icons:
- **Voice Notes**: Record (auto-quits when done)
- **VN Manager**: Settings and recording management

## Documentation

- [User Guide](docs/USER_GUIDE.md)
- [Developer Guide](docs/DEVELOPER_GUIDE.md)
- [Architecture](docs/ARCHITECTURE.md)

## License

CC BY-NC 4.0 License - see [LICENSE](LICENSE).
