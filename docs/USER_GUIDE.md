# User Guide

## Installation

1. Download APK from [Releases](https://github.com/c0dev0id/autorecord-app/releases)
2. Install on Android 8.0+ device
3. Open VN Manager
4. Grant permissions when prompted

## Launcher Icons

- **Voice Notes**: Record voice notes
- **VN Manager**: Settings and recording management

## Recording

1. Tap the Voice Notes icon
2. Overlay bubble appears showing status
3. Location is acquired and announced via TTS
4. Audio recording starts automatically
5. Recording stops after configured duration
6. File saved with GPS coordinates
7. App quits automatically

## Recording Manager

Access via VN Manager > Open Recording Manager.

### Features

- Play recordings
- Transcribe (requires Google Cloud credentials)
- View GPS coordinates
- Open location in maps
- Delete recordings
- Export recordings

### Export Formats

Single recording:
- Audio (OGG/AMR)
- GPX (waypoint)
- CSV (spreadsheet)
- All (combined)

All recordings:
- Audio (ZIP)
- GPX (all waypoints)
- CSV (all data)
- All (ZIP)

Files save to Downloads folder.

## Settings

Access via VN Manager.

### Recording Duration

Set recording length (1-99 seconds). Default: 10 seconds.

### Language Preferences

- App Language: UI display language
- Primary Speech Language: Main transcription language
- Secondary Speech Language: Optional secondary language

### Permissions

Required:
- Microphone: Audio recording
- Location: GPS coordinates
- Bluetooth: Headset support
- Overlay: Status bubble

### Debug Logging

Enable to log API calls for troubleshooting. View logs via Show Debug Log.

## Bluetooth Audio

The app automatically uses connected Bluetooth audio devices. No configuration required.

## Transcription

Requires Google Cloud Speech-to-Text credentials (developer setup).

Status indicators:
- Gray: Not transcribed
- Orange: Processing
- Green: Completed
- Red: Error

If not configured, transcription is disabled. Recording and playback still work.

## Audio Formats

- Android 10+: OGG/Opus
- Android 8-9: AMR-WB

## File Storage

Recordings stored in internal app storage. Use Recording Manager to export files.

## Troubleshooting

### Location not acquired
- Enable GPS in device settings
- Move to area with clear sky view
- App uses last known location as fallback

### Recording fails
- Check microphone permission
- Ensure no other app is using microphone

### Bluetooth not working
- Check Bluetooth permission
- Ensure device is paired and connected

### Transcription fails
- Check internet connection
- Verify Google Cloud credentials configured
- View Debug Log for details

## Data and Privacy

Data stored locally on device:
- Audio recordings
- GPS coordinates
- Transcription results

Transcription sends audio to Google Cloud only when you tap Transcribe.

To delete data:
- Delete individual recordings in Recording Manager
- Clear app data in Android Settings
- Uninstall the app
