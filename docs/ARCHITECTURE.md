# Architecture

## Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Voice Notes App                       │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌────────────┐              ┌────────────┐             │
│  │ Voice Notes│              │ VN Manager │             │
│  │   (Main)   │              │ (Settings) │             │
│  └─────┬──────┘              └─────┬──────┘             │
│        │                           │                     │
│        ▼                           ▼                     │
│  ┌────────────┐              ┌────────────┐             │
│  │  Overlay   │              │ Recording  │             │
│  │  Service   │              │ Manager    │             │
│  └─────┬──────┘              └─────┬──────┘             │
│        │                           │                     │
│        │                           ▼                     │
│        │                     ┌────────────┐             │
│        │                     │   Batch    │             │
│        │                     │ Processing │             │
│        │                     │  Service   │             │
│        │                     └─────┬──────┘             │
│        │                           │                     │
│        ▼                           ▼                     │
│  ┌──────────────────────────────────────────┐          │
│  │         Room Database (SQLite)            │          │
│  └──────────────────────────────────────────┘          │
│                       │                                  │
│                       ▼                                  │
│  ┌──────────────────────────────────────────┐          │
│  │    Internal Storage (Audio Files)         │          │
│  └──────────────────────────────────────────┘          │
│                                                          │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
              ┌────────────────┐
              │  Google Cloud  │
              │ Speech-to-Text │
              └────────────────┘
```

## Components

### MainActivity

Entry point. Validates permissions and starts OverlayService.

### OverlayService

Foreground service handling recording:
1. Display overlay bubble
2. Initialize TTS
3. Acquire GPS location (30s timeout)
4. Announce location via TTS
5. Record audio (MediaRecorder)
6. Save to database
7. Stop service

Threading:
- Main thread: UI operations
- Background thread: Database (coroutines)
- MediaRecorder: Audio recording

### RecordingManagerActivity

Recording list with:
- Playback (MediaPlayer)
- Transcription trigger
- Export (Audio, GPX, CSV)
- Delete

Uses LiveData for reactive updates.

### BatchProcessingService

Background service for transcription. Updates database with results.

### TranscriptionService

Google Cloud Speech-to-Text integration:
- Base64 encode audio
- POST to API
- Parse response
- Return transcription

### SettingsActivity

PreferenceFragmentCompat-based settings:
- Recording duration
- Language preferences
- Permission management
- Debug logging

### Database

Room database with Recording entity.

DAO operations:
- getAllRecordings (Flow)
- getAllRecordingsList
- getAllRecordingsLiveData
- getRecordingById
- insertRecording
- insertRecordings
- updateRecording
- deleteRecording
- getRecordingCount

Test database: In-memory, isolated from production.

## Data Flow

### Recording

```
User tap → MainActivity → OverlayService
                              │
                              ├─ Show overlay
                              ├─ Get GPS location
                              ├─ Announce via TTS
                              ├─ Record audio
                              ├─ Save file
                              ├─ Insert to database
                              └─ Stop service
```

### Transcription

```
User tap Transcribe → RecordingManagerActivity
                              │
                              ├─ Set status: PROCESSING
                              └─ Start BatchProcessingService
                                        │
                                        └─ TranscriptionService
                                                   │
                                                   ├─ Read audio file
                                                   ├─ Call Google API
                                                   └─ Update database
                                                            │
                                                            ▼
                                               LiveData updates UI
```

## File Storage

```
/data/data/com.voicenotes.main/
├── files/
│   ├── recordings/       # Audio files
│   └── debug_log.txt     # Debug log
├── databases/            # SQLite database
└── shared_prefs/         # Settings
```

Filename format: `LAT,LON_YYYYMMDD_HHMMSS.ext`

Example: `40.712800,-74.005900_20240115_123045.ogg`

## Export Formats

GPX:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="VoiceNotes" xmlns="http://www.topografix.com/GPX/1/1">
  <wpt lat="40.7128" lon="-74.0060">
    <time>2024-01-15T12:30:45Z</time>
    <name>VoiceNote: 2024-01-15 12:30:45</name>
    <desc>Transcription text</desc>
  </wpt>
</gpx>
```

CSV:
```
Latitude,Longitude,Date,Time,Text
40.7128,-74.0060,2024-01-15,12:30,"Transcription text"
```

## Security

- Credentials in gradle.properties (not in source)
- ProGuard obfuscation in release builds
- Internal storage (app-private)
- HTTPS for API calls

## Permissions

Runtime:
- RECORD_AUDIO
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- BLUETOOTH_CONNECT
- POST_NOTIFICATIONS

Install-time:
- BLUETOOTH (API <= 30)
- MODIFY_AUDIO_SETTINGS
- INTERNET
- ACCESS_NETWORK_STATE
- REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
- FOREGROUND_SERVICE
- FOREGROUND_SERVICE_LOCATION
- FOREGROUND_SERVICE_MICROPHONE
- SYSTEM_ALERT_WINDOW
