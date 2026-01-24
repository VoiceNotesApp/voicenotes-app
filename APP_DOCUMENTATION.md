# Autorecord App - Comprehensive Technical Documentation

**Android Voice Recording Application with GPS, Cloud Transcription & OpenStreetMap Integration**

Version: 1.0  
Last Updated: 2024

---

## Table of Contents

1. [Screens Overview](#1-screens-overview)
2. [Screen Details](#2-screen-details)
3. [Core Functions by Component](#3-core-functions-by-component)
4. [Background Recording Flow Timeline](#4-background-recording-flow-timeline)
5. [Error Handling Sequences](#5-error-handling-sequences)
6. [Summary Statistics](#6-summary-statistics)

---

## 1. Screens Overview

| Screen Name | File | Purpose | Entry Points |
|------------|------|---------|--------------|
| **MainActivity** | `MainActivity.kt` | Entry point, permission handling, initialization | Launcher icon, background launch |
| **SettingsActivity** | `SettingsActivity.kt` | Configuration UI, permissions, OAuth, manual processing | From MainActivity, from launcher |
| **RecordingManagerActivity** | `RecordingManagerActivity.kt` | View/manage/export recordings | From SettingsActivity |
| **DebugLogActivity** | `DebugLogActivity.kt` | Debug logs, test runner | From SettingsActivity |
| **OverlayService** | `OverlayService.kt` | Recording service with overlay UI | From MainActivity |

### Supporting Services & Components

| Component | File | Purpose |
|-----------|------|---------|
| **BatchProcessingService** | `BatchProcessingService.kt` | Async transcription & OSM note creation |
| **TranscriptionService** | `TranscriptionService.kt` | Google Cloud Speech-to-Text API integration |
| **OsmNotesService** | `OsmNotesService.kt` | OpenStreetMap Notes API integration |
| **OsmOAuthManager** | `OsmOAuthManager.kt` | OAuth 2.0 authentication for OSM |
| **NetworkUtils** | `NetworkUtils.kt` | Connectivity checks |
| **RecordingDatabase** | `database/RecordingDatabase.kt` | Room database singleton |
| **Recording** | `database/Recording.kt` | Entity with status enums |
| **RecordingDao** | `database/RecordingDao.kt` | Database operations |

---

## 2. Screen Details

### 2.1 MainActivity.kt

**Purpose:** Entry point for the app, handles permissions, initialization, and background launch.

**Key Components:**
- **Permissions:** `RECORD_AUDIO`, `ACCESS_FINE_LOCATION`, `BLUETOOTH_CONNECT`, `DRAW_OVERLAY`
- **Request Codes:** `PERMISSIONS_REQUEST_CODE = 100`, `OVERLAY_PERMISSION_REQUEST_CODE = 101`
- **Initialization Timeout:** 10 seconds (triggers error dialog if exceeded)

**Flow Decision Points:**

```kotlin
onCreate() {
    if (!shouldShowUI && !fromSettings && !isFirstRun() && checkPermissions() && canDrawOverlays()) {
        // Background launch - no UI
        startBackgroundRecording()
        return
    }
    // Otherwise, show UI for setup
    setContentView(R.layout.activity_main)
}
```

**Key Methods:**

| Method | Purpose | Timeout/Duration |
|--------|---------|------------------|
| `onCreate()` | Initialize, check launch mode | 10s initialization timeout |
| `isFirstRun()` | Check if permissions & directory configured | - |
| `checkOverlayPermission()` | Verify overlay permission, run migration | - |
| `startRecordingProcess()` | Start OverlayService if permissions OK | - |
| `startBackgroundRecording()` | Launch service without UI (no flicker) | - |
| `extendRecording()` | Extend active recording duration | - |
| `onNewIntent()` | Handle recording extension requests | - |

**SharedPreferences Keys (AppPrefs):**
- `saveDirectory` (String) - Recording save path (default: `/Music/VoiceNotes`)
- `isCurrentlyRecording` (Boolean) - Active recording flag
- `recordingStartTime` (Long) - Recording start timestamp
- `initialRecordingDuration` (Int) - Initial duration in seconds
- `recordingDuration` (Int) - Configured duration (1-99 seconds, default: 10)

**Error Conditions:**
1. Initialization timeout after 10s → Show restart dialog
2. Missing permissions → Request permissions dialog
3. No overlay permission → Redirect to system settings
4. First run (no directory) → Redirect to SettingsActivity

---

### 2.2 SettingsActivity.kt

**Purpose:** Configuration UI for recording duration, permissions, online processing, OSM integration.

**Key Components:**
- Recording duration configuration (1-99 seconds)
- Permission status display (5 permissions)
- Online processing toggle
- OSM account binding/removal
- Manual batch processing trigger
- Service configuration status (Google Cloud, OSM)

**SharedPreferences Keys (AppPrefs):**
- `recordingDuration` (Int) - Duration in seconds (1-99, default: 10)
- `saveDirectory` (String) - Fixed path: `/Music/VoiceNotes`
- `tryOnlineProcessingDuringRide` (Boolean) - Enable transcription during ride (default: true)
- `addOsmNote` (Boolean) - Enable OSM note creation (default: false)

**SharedPreferences (OsmAuth):**
- `osm_access_token` (String) - OAuth access token
- `osm_refresh_token` (String) - OAuth refresh token
- `osm_username` (String) - OSM display name

**BuildConfig Constants:**
- `OSM_CLIENT_ID` - OAuth client ID from gradle.properties
- `GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON_BASE64` - Base64 service account credentials

**Key Methods:**

| Method | Purpose | Async/UI |
|--------|---------|----------|
| `loadCurrentSettings()` | Load all settings from SharedPreferences | UI |
| `saveDuration()` | Validate & save recording duration | UI |
| `updatePermissionStatusList()` | Refresh 5 permission statuses | UI |
| `updateServiceConfigurationStatus()` | Check Google Cloud & OSM configs | UI |
| `requestAllPermissions()` | Request runtime + overlay + storage | UI |
| `runManualProcessing()` | Start BatchProcessingService | Async |
| `updateOsmAccountUI()` | Show OAuth account status | UI |
| `removeOsmAccount()` | Clear OAuth tokens | UI |

**Broadcast Receivers:**
- `BATCH_PROGRESS` - Processing progress updates (filename, status, current, total)
- `BATCH_COMPLETE` - Batch processing complete signal

**Processing Status Display:**
- Colors: Green (complete), Red (error/timeout), Blue (in-progress)
- States: "transcribing", "creating GPX", "creating OSM note", "complete", "error", "timeout"

---

### 2.3 RecordingManagerActivity.kt

**Purpose:** View, play, process, delete, and export recordings from Room database.

**Key Components:**
- RecyclerView with Recording entities
- MediaPlayer for audio playback
- Export formats: Audio (ZIP), GPX, CSV, All (ZIP)
- Individual & bulk export support

**Database Operations:**
- Uses `LiveData` for auto-updating list
- CRUD operations via RecordingDao
- Supports single recording processing
- Batch export (all recordings)

**Key Methods:**

| Method | Purpose | Export Format |
|--------|---------|---------------|
| `loadRecordings()` | Load all recordings (LiveData) | - |
| `playRecording()` | Play audio with MediaPlayer | - |
| `processRecording()` | Trigger BatchProcessingService | - |
| `deleteRecording()` | Delete file + DB entry | - |
| `exportAudio()` | Export audio files (single/ZIP) | `.ogg/.amr` or `.zip` |
| `exportGPX()` | Export waypoints as GPX | `.gpx` |
| `exportCSV()` | Export data as CSV | `.csv` |
| `exportAll()` | Export audio + GPX + CSV | `.zip` |

**Export Formats:**

**GPX Structure:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="VoiceNotes">
  <wpt lat="LAT" lon="LON">
    <time>TIMESTAMP</time>
    <name>FILENAME</name>
    <desc>TRANSCRIPTION</desc>
  </wpt>
</gpx>
```

**CSV Structure:**
```csv
Latitude,Longitude,Timestamp,Filename,Transcription,V2S Status,OSM Status
LAT,LON,DATETIME,FILE,TEXT,STATUS,STATUS
```

**Status Icons:**

| V2SStatus | Icon | Description |
|-----------|------|-------------|
| `NOT_STARTED` | `ic_menu_help` | Not transcribed |
| `PROCESSING` | `ic_popup_sync` | Transcribing |
| `COMPLETED` | `checkbox_on_background` | Transcribed |
| `FALLBACK` | `ic_dialog_alert` | Partial transcription |
| `ERROR` | `ic_delete` | Transcription error |
| `DISABLED` | `ic_menu_close_clear_cancel` | Transcription disabled |

| OsmStatus | Icon | Description |
|-----------|------|-------------|
| `NOT_STARTED` | `ic_menu_help` | OSM note not created |
| `PROCESSING` | `ic_popup_sync` | Creating OSM note |
| `COMPLETED` | `checkbox_on_background` | OSM note created |
| `ERROR` | `ic_delete` | OSM note error |
| `DISABLED` | `ic_menu_close_clear_cancel` | OSM disabled |


---

### 2.4 DebugLogActivity.kt

**Purpose:** View debug logs, run test suite, share/copy/clear logs.

**Key Components:**
- Logging enable/disable toggle
- Test runner button
- Log display (ScrollView)
- Share/Copy/Clear actions

**SharedPreferences Keys (AppPrefs):**
- `debug_logging_enabled` (Boolean) - Enable debug logging (default: false)

**Key Methods:**

| Method | Purpose |
|--------|---------|
| `updateLogDisplay()` | Refresh log content from DebugLogger |
| `runTests()` | Execute TestSuite.runAllTests() |
| `buttonCopyLog` | Copy log to clipboard |
| `buttonShareLog` | Share log via Intent |
| `buttonClearLog` | Clear all logs |

**Log Actions:**
- Copy to clipboard (ClipboardManager)
- Share via Intent (`ACTION_SEND`)
- Clear all entries

---

### 2.5 OverlayService.kt (LifecycleService)

**Purpose:** Core recording service with overlay UI, GPS, MediaRecorder, TTS, and online processing.

**Key Components:**
- Floating overlay bubble (WindowManager)
- Text-to-Speech announcements
- GPS location acquisition
- MediaRecorder (OGG_OPUS/AMR_WB)
- Online transcription (optional)
- OSM note creation (optional)
- Bluetooth SCO audio routing

**Timeouts:**

| Component | Timeout | Action on Timeout |
|-----------|---------|-------------------|
| TTS Initialization | 10s | Proceed without TTS |
| GPS Location | 30s | Fall back to last known location |
| Bluetooth SCO | 5s | Continue recording anyway |
| Transcription | 60s | Fail with timeout error |

**SharedPreferences Keys (AppPrefs):**
- `recordingDuration` (Int) - Recording duration in seconds
- `isCurrentlyRecording` (Boolean) - Active recording flag
- `recordingStartTime` (Long) - Start timestamp (milliseconds)
- `initialRecordingDuration` (Int) - Initial duration for extensions
- `tryOnlineProcessingDuringRide` (Boolean) - Enable online processing
- `addOsmNote` (Boolean) - Enable OSM note creation

**Audio Formats:**

| API Level | Format | Encoder | Sample Rate | Bitrate | File Extension |
|-----------|--------|---------|-------------|---------|----------------|
| 29+ (Q+) | OGG | OPUS | 48000 Hz | 32 kbps | `.ogg` |
| 26-28 | AMR_WB | AMR_WB | 16000 Hz | Fixed | `.amr` |

**Filename Format:**
```
{latitude},{longitude}_{timestamp}.{ext}
Example: 37.774929,-122.419418_20241215_143022.ogg
```

**Storage Location:**
- Internal app storage: `filesDir/recordings/`
- External legacy: `/Music/VoiceNotes/` (for GPX/CSV)

**Key Lifecycle Methods:**

| Method | Phase | Duration Estimate |
|--------|-------|-------------------|
| `onCreate()` | Service creation | T+0ms |
| `onStartCommand()` | Check for extension request | T+50ms |
| `onInit()` | TTS initialization callback | T+0-10000ms |
| `startRecordingProcess()` | Begin location acquisition | T+100ms |
| `acquireLocation()` | GPS location request | T+100ms - T+30100ms |
| `onLocationAcquired()` | Location obtained | T+500ms - T+30000ms |
| `speakText()` | TTS announcements (2x) | T+0-3000ms |
| `startRecording()` | MediaRecorder start | T+3500ms |
| `startCountdown()` | 1s interval countdown | T+3500ms - T+Xs |
| `stopRecording()` | Save & stop recording | T+X+1s |
| `saveRecordingToDatabase()` | Room DB insert | T+X+1.1s |
| `finishRecordingProcess()` | Check online processing | T+X+2s |
| `startPostProcessing()` | Transcription (if online) | T+X+2s - T+X+62s |
| `stopSelfAndFinish()` | Cleanup & finish | T+X+3s |

**Overlay States:**
1. "Acquiring location"
2. "Location acquired: LAT, LON"
3. "Recording: Xs" (countdown)
4. "Recording stopped"
5. "File saved: FILENAME"
6. "Online: Transcribing: TEXT"
7. "Online: Creating OSM Note"
8. "Online: OSM Note created."

**Error States:**
1. "Location permission not granted"
2. "Location unavailable - please ensure GPS is enabled"
3. "Location failed"
4. "Recording failed: Invalid state"
5. "Recording failed: Microphone in use"
6. "Online: Transcribing: failed :-("
7. "Online: OSM Note creation failed :("

---

## 3. Core Functions by Component

### 3.1 Database Layer (Room)

**RecordingDatabase.kt**

- **Pattern:** Singleton with double-check locking
- **Database Name:** `recording_database`
- **Version:** 1
- **Migration Strategy:** `fallbackToDestructiveMigration()`

```kotlin
companion object {
    @Volatile
    private var INSTANCE: RecordingDatabase? = null
    
    fun getDatabase(context: Context): RecordingDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(/*...*/).build()
            INSTANCE = instance
            instance
        }
    }
}
```

**Recording.kt Entity**

```kotlin
@Entity(tableName = "recordings")
data class Recording(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filename: String,           // Original filename
    val filepath: String,           // Full path to audio file
    val timestamp: Long,            // Recording timestamp (milliseconds)
    val latitude: Double,           // GPS latitude
    val longitude: Double,          // GPS longitude
    
    val v2sStatus: V2SStatus = V2SStatus.NOT_STARTED,
    val v2sResult: String? = null,  // Transcribed text
    val v2sFallback: Boolean = false,
    
    val osmStatus: OsmStatus = OsmStatus.NOT_STARTED,
    val osmResult: String? = null,  // OSM note URL
    val osmNoteId: Long? = null,    // OSM note ID
    
    val errorMsg: String? = null,   // Last error message
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

**Status Enums:**

```kotlin
enum class V2SStatus {
    NOT_STARTED,    // Not yet processed
    PROCESSING,     // Currently being transcribed
    COMPLETED,      // Successfully transcribed
    FALLBACK,       // Used fallback/partial result
    ERROR,          // Transcription failed
    DISABLED        // Processing disabled
}

enum class OsmStatus {
    NOT_STARTED,    // Not yet created
    PROCESSING,     // Currently creating note
    COMPLETED,      // Successfully created
    ERROR,          // Creation failed
    DISABLED        // OSM integration disabled
}
```

**RecordingDao.kt Operations**

| Method | Return Type | Purpose |
|--------|-------------|---------|
| `getAllRecordings()` | `Flow<List<Recording>>` | Reactive list |
| `getAllRecordingsList()` | `List<Recording>` | One-time list |
| `getAllRecordingsLiveData()` | `LiveData<List<Recording>>` | LiveData list |
| `getRecordingById(id)` | `Recording?` | Single by ID |
| `getRecordingByFilepath(path)` | `Recording?` | Single by path |
| `getRecordingsByV2SStatus(status)` | `List<Recording>` | Filter by V2S |
| `getRecordingsByOsmStatus(status)` | `List<Recording>` | Filter by OSM |
| `insertRecording(recording)` | `Long` | Insert, return ID |
| `insertRecordings(recordings)` | - | Bulk insert |
| `updateRecording(recording)` | - | Update existing |
| `deleteRecording(recording)` | - | Delete single |
| `deleteRecordingById(id)` | - | Delete by ID |
| `getRecordingCount()` | `Int` | Total count |
| `getRecordingCountByV2SStatus(status)` | `Int` | Count by status |
| `deleteAllRecordings()` | - | Clear all |


---

### 3.2 API Services

#### 3.2.1 TranscriptionService.kt (Google Cloud Speech-to-Text)

**Configuration:**
- **BuildConfig:** `GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON_BASE64` (Base64-encoded JSON)
- **API Endpoint:** `https://speech.googleapis.com/v1/speech:recognize`
- **Timeout:** 60 seconds
- **Encoding:** OGG_OPUS (48kHz) or FLAC (44.1kHz for M4A)
- **Language:** en-US
- **Model:** default
- **Features:** Automatic punctuation enabled

**Key Methods:**

| Method | Parameters | Return | Timeout |
|--------|-----------|--------|---------|
| `isConfigured()` | - | `Boolean` | - |
| `transcribeAudioFile(filePath)` | `String` | `Result<String>` | 60s |

**Validation:**
- Base64 decode service account JSON
- Check for `type`, `project_id`, `private_key` fields
- Verify audio file exists
- Read file as ByteString

**Response Handling:**
- Extract first alternative transcript
- Return empty string if no results
- Log API request/response via DebugLogger

**Error Handling:**
- Timeout → "Transcription timeout - network too slow or file too large"
- Invalid credentials → "Google Cloud credentials not configured"
- File not found → "Audio file not found: PATH"
- Other exceptions → Pass through with error message

---

#### 3.2.2 OsmNotesService.kt (OpenStreetMap Notes API)

**Configuration:**
- **API Endpoint:** `https://api.openstreetmap.org/api/0.6/notes.json`
- **Method:** POST
- **Authentication:** Bearer token (OAuth 2.0)
- **Timeouts:** Connect: 10s, Read: 30s, Write: 30s

**Request Format:**
```
POST https://api.openstreetmap.org/api/0.6/notes.json?lat={LAT}&lon={LON}&text={TEXT}
Headers:
  Authorization: Bearer {ACCESS_TOKEN}
  Content-Type: application/json
```

**Key Methods:**

| Method | Parameters | Return | Validation |
|--------|-----------|--------|------------|
| `createNote(lat, lon, text, token)` | `Double, Double, String, String` | `Result<Unit>` | Lat: [-90, 90], Lon: [-180, 180], Text: not blank |

**Validation:**
- Latitude: -90.0 to 90.0
- Longitude: -180.0 to 180.0
- Text: not blank
- URL-encode text parameter

**Error Handling:**
1. `IllegalArgumentException` - Invalid input validation
2. `IOException` - Network error
3. HTTP error codes - "Failed to create note: CODE MESSAGE"
4. Unexpected exceptions - Generic error

---

#### 3.2.3 OsmOAuthManager.kt (OAuth 2.0)

**Configuration:**
- **BuildConfig:** `OSM_CLIENT_ID` (from gradle.properties)
- **Auth Endpoint:** `https://www.openstreetmap.org/oauth2/authorize`
- **Token Endpoint:** `https://www.openstreetmap.org/oauth2/token`
- **User Details:** `https://api.openstreetmap.org/api/0.6/user/details.json`
- **Redirect URI:** `app.voicenotes.motorcycle://oauth`
- **Scope:** `read_prefs write_notes`
- **PKCE:** Enabled (code verifier)

**Storage (SharedPreferences: OsmAuth):**
- `osm_access_token` - OAuth access token
- `osm_refresh_token` - OAuth refresh token
- `osm_username` - OSM display name

**OAuth Flow:**
1. `startOAuthFlow(launcher)` - Launch authorization URL
2. User authorizes in browser
3. Redirect to app with auth code
4. `handleOAuthResponse(intent, onSuccess, onFailure)` - Exchange code for token
5. `fetchUsername(accessToken, callback)` - Get user display name
6. `saveTokensToKeystore(access, refresh)` - Store tokens
7. `saveUsername(username)` - Store username

**Key Methods:**

| Method | Purpose | Return |
|--------|---------|--------|
| `startOAuthFlow(launcher)` | Launch OAuth browser flow | - |
| `handleOAuthResponse(intent, success, failure)` | Handle callback | - |
| `fetchUsername(token, callback)` | Get OSM username | - |
| `saveTokensToKeystore(access, refresh)` | Store tokens | - |
| `getAccessToken()` | Retrieve access token | `String?` |
| `getUsername()` | Retrieve username | `String?` |
| `removeTokens()` | Clear all tokens | - |
| `isAuthenticated()` | Check if token exists | `Boolean` |

**Username Extraction:**
```json
{
  "user": {
    "display_name": "USERNAME"
  }
}
```

**Fallback:** If username fetch fails, use "OSM_User"

---

### 3.3 Background Services

#### 3.3.1 BatchProcessingService.kt

**Purpose:** Asynchronous processing of recordings (transcription, GPX, CSV, OSM notes).

**Processing Modes:**
1. **Single Recording:** Process specific recording by ID
2. **Batch Mode:** Process all `V2SStatus.NOT_STARTED` recordings

**Timeouts:**
- **Per-file timeout:** 120 seconds (2 minutes)
- Triggers `TimeoutCancellationException` → Update status to ERROR with "Processing timeout"

**Processing Steps:**

| Step | Description | Status Update | Broadcast |
|------|-------------|---------------|-----------|
| 1. Update status | Set `v2sStatus = PROCESSING` | DB | - |
| 2. Transcribe | Call `TranscriptionService.transcribeAudioFile()` | - | "transcribing" |
| 3. Handle result | Save transcription to DB | DB (v2sResult) | - |
| 4. Create GPX | Add waypoint to `voicenote_waypoint_collection.gpx` | - | "creating GPX" |
| 5. Create CSV | Add entry to `voicenote_waypoint_collection.csv` | - | - |
| 6. OSM note (if enabled) | Call `OsmNotesService.createNote()` | DB (osmStatus) | "creating OSM note" |
| 7. Complete | Set status to COMPLETED | DB | "complete" |

**Broadcast Intents:**
- `com.voicenotes.motorcycle.BATCH_PROGRESS`
  - `filename` (String)
  - `status` (String): "transcribing", "creating GPX", "creating OSM note", "complete", "error", "timeout"
  - `current` (Int)
  - `total` (Int)
- `com.voicenotes.motorcycle.BATCH_COMPLETE`

**Error Handling:**
- Transcription failure → Update `v2sStatus = ERROR`, set `errorMsg`
- OSM failure → Update `osmStatus = ERROR`, set `errorMsg`
- Timeout → Update `v2sStatus = ERROR`, `errorMsg = "Processing timeout"`
- Continue to next file after errors

**GPX/CSV Management:**
- **Deduplication:** Check for existing waypoint at same coordinates (6 decimal precision)
- **Replace:** Update existing waypoint with new text
- **Add:** Append new waypoint if coordinates don't exist
- **File Location:** External storage `/Music/VoiceNotes/`

---

## 4. Background Recording Flow Timeline

### 4.1 Normal Flow (All Services Available)

| Time | Phase | Component | Action | Display/Announcement |
|------|-------|-----------|--------|---------------------|
| T+0ms | App Launch | MainActivity | User taps launcher icon | - |
| T+50ms | Permission Check | MainActivity | Check: permissions, overlay, first run | - |
| T+100ms | Decision | MainActivity | All OK → startBackgroundRecording() | - |
| T+150ms | Service Start | OverlayService | onCreate() called | - |
| T+200ms | Overlay Creation | OverlayService | Create floating bubble | "Acquiring location" |
| T+250ms | TTS Init | OverlayService | Initialize TextToSpeech | - |
| T+300ms | GPS Request | OverlayService | FusedLocationClient.getCurrentLocation() | - |
| T+500ms - T+5000ms | GPS Acquisition | OverlayService | Wait for GPS fix | "Acquiring location" |
| T+5000ms | Location Acquired | OverlayService | Location received | "Location acquired: LAT, LON" |
| T+5100ms | TTS Announce 1 | OverlayService | Speak "Location acquired" | TTS: "Location acquired" |
| T+6500ms | TTS Announce 2 | OverlayService | Speak "Recording started" | TTS: "Recording started" |
| T+8000ms | Recording Start | OverlayService | MediaRecorder.start() | - |
| T+8100ms | Countdown Start | OverlayService | Start 1s interval countdown | "Recording: 10s" |
| T+9100ms | Countdown | OverlayService | Update countdown | "Recording: 9s" |
| T+10100ms | Countdown | OverlayService | Update countdown | "Recording: 8s" |
| ... | ... | ... | ... | ... |
| T+18000ms | Recording Stop | OverlayService | MediaRecorder.stop() | "Recording stopped" |
| T+18100ms | Save to DB | OverlayService | Insert Recording entity | - |
| T+18200ms | TTS Announce | OverlayService | Speak "Recording stopped" | TTS: "Recording stopped" |
| T+19500ms | File Saved | OverlayService | Display filename | "File saved: FILENAME" |
| T+19600ms | Network Check | OverlayService | Check NetworkUtils.isOnline() | - |
| T+19700ms | Online Processing | OverlayService | Start transcription | "Online: Transcribing:" |
| T+20000ms | API Call | TranscriptionService | Google Cloud Speech API | - |
| T+23000ms | Transcription Done | TranscriptionService | Return transcribed text | "Online: Transcribing: TEXT" |
| T+24000ms | GPX Creation | OverlayService | Create/update GPX waypoint | - |
| T+24500ms | CSV Creation | OverlayService | Create/update CSV entry | - |
| T+25000ms | OSM Check | OverlayService | Check addOsmNote preference | - |
| T+25100ms | OSM Note | OsmNotesService | POST to OSM API (if enabled) | "Online: Creating OSM Note" |
| T+27000ms | OSM Done | OsmNotesService | Note created | "Online: OSM Note created." |
| T+28000ms | Cleanup | OverlayService | stopSelfAndFinish() | - |
| T+28100ms | Remove Overlay | OverlayService | Remove bubble, stop Bluetooth SCO | - |
| T+28200ms | Broadcast | OverlayService | Send FINISH_ACTIVITY to MainActivity | - |
| T+28300ms | Service Stop | OverlayService | stopSelf(), onDestroy() | - |
| T+28400ms | MainActivity Finish | MainActivity | Receive broadcast, finish() | - |

**Total Duration:** ~28 seconds (with 10s recording, online processing enabled)

---

### 4.2 Fast Flow (Offline, No Processing)

| Time | Phase | Component | Action | Display |
|------|-------|-----------|--------|---------|
| T+0ms | App Launch | MainActivity | startBackgroundRecording() | - |
| T+100ms | Service Start | OverlayService | onCreate() | "Acquiring location" |
| T+500ms | GPS Acquired | OverlayService | Location received | "Location acquired" |
| T+1500ms | TTS Complete | OverlayService | Both announcements done | - |
| T+1600ms | Recording Start | OverlayService | MediaRecorder.start() | "Recording: 10s" |
| T+11600ms | Recording Stop | OverlayService | MediaRecorder.stop() | "Recording stopped" |
| T+11700ms | Save to DB | OverlayService | Insert Recording | - |
| T+12700ms | TTS Complete | OverlayService | Announce complete | "File saved: FILENAME" |
| T+12800ms | Network Check | OverlayService | Offline detected | - |
| T+14800ms | Cleanup | OverlayService | stopSelfAndFinish() | - |
| T+15000ms | Service Stop | OverlayService | onDestroy() | - |

**Total Duration:** ~15 seconds (with 10s recording, offline mode)

---

### 4.3 Recording Extension Flow

| Time | Phase | Component | Action | Display |
|------|-------|-----------|--------|---------|
| T+0ms | User Reopens | MainActivity | onNewIntent() called | - |
| T+50ms | Check State | MainActivity | Read isCurrentlyRecording = true | - |
| T+100ms | Extension | MainActivity | extendRecording() called | - |
| T+150ms | Service Intent | MainActivity | Send additionalDuration to OverlayService | - |
| T+200ms | Service Receives | OverlayService | onStartCommand() with additionalDuration | - |
| T+250ms | Extend Duration | OverlayService | extendRecordingDuration(seconds) | - |
| T+300ms | Cancel Countdown | OverlayService | Remove existing countdown callback | - |
| T+350ms | Add Time | OverlayService | remainingSeconds += additionalDuration | - |
| T+400ms | Restart Countdown | OverlayService | startCountdown() | "Recording extended! Xs remaining" |
| T+450ms | Background | MainActivity | moveTaskToBack(true) | - |

**Result:** Recording continues with extended duration, no service restart


---

## 5. Error Handling Sequences

### Error Path 1: Initialization Timeout

**Condition:** MainActivity initialization exceeds 10 seconds

**Sequence:**
1. `onCreate()` sets 10s timeout handler
2. Timeout fires → `initializationTimeout` Runnable executes
3. Display error dialog: "Initialization failed. Please try again."
4. User clicks "Restart" → Launch SettingsActivity
5. MainActivity finishes

**Recovery:** User can configure settings and try again

---

### Error Path 2: Missing Permissions

**Condition:** First run or permissions revoked

**Sequence:**
1. `isFirstRun()` returns true (missing permissions)
2. Show setup dialog: "Setup Required" → "Open Settings"
3. User clicks "Open Settings" → Launch SettingsActivity
4. User grants permissions in SettingsActivity
5. Return to MainActivity → checkOverlayPermission()
6. If all OK → startRecordingProcess()

**Recovery:** Automatic after permissions granted

---

### Error Path 3: No Overlay Permission

**Condition:** `Settings.canDrawOverlays()` returns false

**Sequence:**
1. checkOverlayPermission() detects missing overlay
2. Show dialog: "Overlay permission required"
3. User clicks "OK" → Launch system settings
4. User grants overlay permission
5. onActivityResult() called → startRecordingProcess()

**Recovery:** Automatic after permission granted

---

### Error Path 4: TTS Initialization Timeout

**Condition:** TextToSpeech fails to initialize within 10 seconds

**Sequence:**
1. OverlayService.onCreate() initializes TTS
2. Set 10s timeout handler
3. Timeout fires before onInit() callback
4. Log warning: "TTS initialization timeout"
5. Set `isTtsInitialized = false`
6. Proceed to startRecordingProcess() without TTS
7. Recording continues, no voice announcements

**Recovery:** Recording works, but silent (no TTS)

---

### Error Path 5: GPS Timeout (30s)

**Condition:** GPS location not acquired within 30 seconds

**Sequence:**
1. acquireLocation() requests current location
2. Set 30s timeout handler
3. Timeout fires → Cancel location request
4. Log: "GPS location acquisition timeout"
5. Call tryLastKnownLocation()
6. If last known location available:
   - Use last location
   - Display: "Using last known location"
   - Continue to onLocationAcquired()
7. If no last known location:
   - Display: "Location unavailable - please ensure GPS is enabled"
   - Wait 3s → stopSelfAndFinish()

**Recovery:** Use last known location (may be stale) or abort

---

### Error Path 6: GPS Acquisition Failed

**Condition:** FusedLocationClient returns null location

**Sequence:**
1. acquireLocation() receives null from getCurrentLocation()
2. Log: "GPS location acquisition failed"
3. Call tryLastKnownLocation()
4. (Same as Error Path 5 steps 6-7)

**Recovery:** Same as GPS timeout

---

### Error Path 7: MediaRecorder IllegalStateException

**Condition:** MediaRecorder in invalid state (setup or start failed)

**Sequence:**
1. startRecording() configures MediaRecorder
2. mediaRecorder.start() throws IllegalStateException
3. Catch exception, log error
4. Display: "Recording failed: Invalid state"
5. Wait 3s → stopSelfAndFinish()

**Recovery:** Service stops, user must restart

---

### Error Path 8: MediaRecorder RuntimeException (Microphone in Use)

**Condition:** Microphone already in use by another app

**Sequence:**
1. startRecording() calls mediaRecorder.start()
2. Throws RuntimeException with "start failed" message
3. Catch exception, parse message
4. Display: "Recording failed: Microphone in use"
5. Log error with DebugLogger
6. Wait 3s → stopSelfAndFinish()

**Recovery:** User must close other app using microphone

---

### Error Path 9: Database Save Failure

**Condition:** Room database insert fails

**Sequence:**
1. saveRecordingToDatabase() called after recording stops
2. Coroutine launch on IO dispatcher
3. db.recordingDao().insertRecording() throws exception
4. Catch exception, log error
5. Log: "Failed to save recording to database: MESSAGE"
6. Recording file still exists on disk
7. Service continues to online processing or cleanup

**Recovery:** File preserved, can be manually added to DB later

---

### Error Path 10: Transcription Timeout (60s)

**Condition:** Google Cloud API takes > 60 seconds

**Sequence:**
1. startPostProcessing() calls transcribeAudioFile()
2. withTimeout(60000) wraps API call
3. Timeout fires → TimeoutCancellationException thrown
4. Catch in TranscriptionService
5. Return Result.failure("Transcription timeout - network too slow or file too large")
6. handleTranscriptionFailure() called
7. Display: "Online: Transcribing: failed :-("
8. Log error, wait 1s → stopSelfAndFinish()

**Recovery:** Recording saved with V2SStatus.ERROR, can retry manually

---

### Error Path 11: Transcription Service Not Configured

**Condition:** Google Cloud credentials missing or invalid

**Sequence:**
1. transcribeAudioFileInternal() decodes Base64 credentials
2. decodeServiceAccountJson() returns null (empty/invalid)
3. Log: "Google Cloud credentials not configured"
4. Return Result.failure("Transcription is disabled")
5. handleTranscriptionFailure() displays error
6. Service continues to cleanup (no retry)

**Recovery:** User must configure credentials in gradle.properties and rebuild

---

### Error Path 12: OSM Note Creation Failed

**Condition:** OSM API returns error (network, auth, validation)

**Sequence:**
1. createOsmNote() calls osmService.createNote()
2. OsmNotesService.createNote() fails with exception:
   - IllegalArgumentException (invalid coordinates/text)
   - IOException (network error)
   - HTTP error (4xx/5xx)
3. Return Result.failure(Exception)
4. onFailure block executes
5. Display: "Online: OSM Note creation failed :("
6. Update database: `osmStatus = ERROR`, set errorMsg
7. Service continues to cleanup

**Recovery:** Recording saved with OsmStatus.ERROR, can retry manually in RecordingManagerActivity

---

### Error Path 13: Batch Processing Timeout (Per-File)

**Condition:** Single file processing exceeds 120 seconds

**Sequence:**
1. BatchProcessingService.processAllFiles() calls processRecording()
2. withTimeout(120000) wraps processing
3. Timeout fires → TimeoutCancellationException
4. Catch exception in processAllFiles()
5. Update recording: `v2sStatus = ERROR`, `errorMsg = "Processing timeout"`
6. Broadcast: status = "timeout"
7. Continue to next file in batch

**Recovery:** Failed recording marked as ERROR, batch continues with remaining files

---

## 6. Summary Statistics

### 6.1 Component Counts

| Category | Count | Details |
|----------|-------|---------|
| **Activities** | 4 | MainActivity, SettingsActivity, RecordingManagerActivity, DebugLogActivity |
| **Services** | 3 | OverlayService (LifecycleService), BatchProcessingService, (TranscriptionService is helper class) |
| **API Services** | 3 | TranscriptionService, OsmNotesService, OsmOAuthManager |
| **Database Components** | 3 | RecordingDatabase, Recording entity, RecordingDao |
| **Utility Classes** | 2 | NetworkUtils, DebugLogger |
| **Enums** | 2 | V2SStatus (6 states), OsmStatus (5 states) |
| **Broadcast Receivers** | 2 | FinishActivityReceiver, BatchProcessingService broadcasts |

---

### 6.2 Timeout Configuration

| Component | Operation | Timeout | Fallback/Action |
|-----------|-----------|---------|-----------------|
| MainActivity | Initialization | 10s | Show restart dialog |
| OverlayService | TTS Init | 10s | Proceed without TTS |
| OverlayService | GPS Acquisition | 30s | Use last known location |
| OverlayService | Bluetooth SCO | 5s | Continue recording |
| TranscriptionService | API Call | 60s | Fail with timeout error |
| BatchProcessingService | Per-File Processing | 120s | Mark as ERROR, continue |
| OsmNotesService | API Call | Connect: 10s, Read: 30s, Write: 30s | Fail with network error |
| OsmOAuthManager | API Call | Connect: 10s, Read: 30s | Fall back to "OSM_User" |

**Total Possible Timeout:** 10 + 10 + 30 + 5 + 60 + 120 = 235 seconds (worst case)

---

### 6.3 SharedPreferences Keys

**AppPrefs:**
- `saveDirectory` (String) - Default: `/Music/VoiceNotes`
- `recordingDuration` (Int) - Range: 1-99, Default: 10
- `isCurrentlyRecording` (Boolean) - Recording active flag
- `recordingStartTime` (Long) - Start timestamp (ms)
- `initialRecordingDuration` (Int) - For extensions
- `tryOnlineProcessingDuringRide` (Boolean) - Default: true
- `addOsmNote` (Boolean) - Default: false
- `debug_logging_enabled` (Boolean) - Default: false

**OsmAuth:**
- `osm_access_token` (String)
- `osm_refresh_token` (String)
- `osm_username` (String)

**Total:** 11 keys across 2 SharedPreferences files

---

### 6.4 API Endpoints

| Service | Endpoint | Method | Auth | Timeout |
|---------|----------|--------|------|---------|
| Google Cloud Speech | `https://speech.googleapis.com/v1/speech:recognize` | POST | Service Account JSON | 60s |
| OSM OAuth Authorize | `https://www.openstreetmap.org/oauth2/authorize` | GET | Client ID | - |
| OSM OAuth Token | `https://www.openstreetmap.org/oauth2/token` | POST | Client ID + Secret | - |
| OSM User Details | `https://api.openstreetmap.org/api/0.6/user/details.json` | GET | Bearer Token | 30s |
| OSM Notes Create | `https://api.openstreetmap.org/api/0.6/notes.json` | POST | Bearer Token | 30s |

**Total:** 5 endpoints across 2 external services

---

### 6.5 Database Schema

**Table:** `recordings`

| Column | Type | Constraints | Default |
|--------|------|-------------|---------|
| `id` | Long | PRIMARY KEY, AUTO_INCREMENT | 0 |
| `filename` | String | NOT NULL | - |
| `filepath` | String | NOT NULL | - |
| `timestamp` | Long | NOT NULL | - |
| `latitude` | Double | NOT NULL | - |
| `longitude` | Double | NOT NULL | - |
| `v2sStatus` | V2SStatus (enum) | NOT NULL | NOT_STARTED |
| `v2sResult` | String? | NULLABLE | null |
| `v2sFallback` | Boolean | NOT NULL | false |
| `osmStatus` | OsmStatus (enum) | NOT NULL | NOT_STARTED |
| `osmResult` | String? | NULLABLE | null |
| `osmNoteId` | Long? | NULLABLE | null |
| `errorMsg` | String? | NULLABLE | null |
| `createdAt` | Long | NOT NULL | currentTimeMillis() |
| `updatedAt` | Long | NOT NULL | currentTimeMillis() |

**Total Columns:** 15

---

### 6.6 File Formats

| Format | File Extension | MIME Type | Use Case |
|--------|---------------|-----------|----------|
| OGG Opus | `.ogg` | `audio/ogg` | API 29+ recordings |
| AMR-WB | `.amr` | `audio/amr-wb` | API 26-28 recordings |
| GPX 1.1 | `.gpx` | `application/gpx+xml` | Waypoint export |
| CSV | `.csv` | `text/csv` | Data export |
| ZIP | `.zip` | `application/zip` | Batch export |

**Audio Encoding Details:**
- **OGG Opus:** 48kHz, 32kbps, optimal for speech
- **AMR-WB:** 16kHz, fixed bitrate, legacy compatibility

---

### 6.7 Permissions Required

| Permission | Type | Purpose | API Level |
|-----------|------|---------|-----------|
| `RECORD_AUDIO` | Dangerous | Audio recording | All |
| `ACCESS_FINE_LOCATION` | Dangerous | GPS coordinates | All |
| `BLUETOOTH_CONNECT` | Dangerous | Bluetooth audio routing | 31+ |
| `SYSTEM_ALERT_WINDOW` | Special | Overlay bubble | All |
| `MANAGE_EXTERNAL_STORAGE` | Special | File access (optional) | 30+ |

**Total:** 5 permissions (3 dangerous, 2 special)

---

### 6.8 Recording Duration

**Configuration:**
- **Min:** 1 second
- **Max:** 99 seconds
- **Default:** 10 seconds
- **Extension:** Additive (can extend multiple times)

**Example:**
- Initial: 10s
- Extension 1: +10s = 20s remaining
- Extension 2: +10s = 30s remaining
- Total: 30s recording

---

### 6.9 Storage Locations

| Data Type | Location | Purpose |
|-----------|----------|---------|
| Audio Files | `filesDir/recordings/` | Internal app storage, private |
| GPX File | `/Music/VoiceNotes/voicenote_waypoint_collection.gpx` | External, user-accessible |
| CSV File | `/Music/VoiceNotes/voicenote_waypoint_collection.csv` | External, user-accessible |
| Database | `databases/recording_database` | Internal, managed by Room |
| Debug Logs | Internal (via DebugLogger) | In-memory or file (context-dependent) |

**Total:** 5 storage locations

---

## Technical Notes

### Audio Format Selection Logic

```kotlin
val fileExtension = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "ogg" else "amr"

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    setOutputFormat(MediaRecorder.OutputFormat.OGG)
    setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
    setAudioEncodingBitRate(32000)  // 32kbps
    setAudioSamplingRate(48000)     // 48kHz
} else {
    setOutputFormat(MediaRecorder.OutputFormat.AMR_WB)
    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
    // Fixed 16kHz sample rate
}
```

### Bluetooth Audio Source Selection

```kotlin
if (audioManager.isBluetoothScoAvailableOffCall) {
    audioManager.startBluetoothSco()
    handler.postDelayed(bluetoothScoTimeoutRunnable, 5000)
    MediaRecorder.AudioSource.VOICE_RECOGNITION
} else {
    MediaRecorder.AudioSource.VOICE_RECOGNITION
}
```

### GPS Fallback Chain

1. Current location (high accuracy, 30s timeout)
2. Last known location (may be stale)
3. Abort recording (display error)

### Transcription Format Detection

```kotlin
val isOggOpus = filePath.endsWith(".ogg", ignoreCase = true)

val encoding = if (isOggOpus) {
    RecognitionConfig.AudioEncoding.OGG_OPUS
} else {
    RecognitionConfig.AudioEncoding.FLAC  // Works for M4A/AAC
}
val sampleRate = if (isOggOpus) 48000 else 44100
```

---

## Error Recovery Matrix

| Error Type | Detection Method | User Action Required | Auto Recovery |
|-----------|------------------|---------------------|---------------|
| Init Timeout | Handler timeout | Restart app | No |
| Missing Permissions | Permission check | Grant permissions | Yes (after grant) |
| No Overlay | canDrawOverlays() | Grant in settings | Yes (after grant) |
| TTS Timeout | Handler timeout | None | Yes (silent mode) |
| GPS Timeout | Location timeout | Enable GPS / Move outdoors | Partial (last known) |
| GPS Failed | Null location | Enable GPS / Move outdoors | Partial (last known) |
| Mic in Use | RuntimeException | Close other app | No |
| MediaRecorder Error | IllegalStateException | Restart app | No |
| DB Save Failed | Exception | None (file preserved) | Manual |
| Transcription Timeout | withTimeout | None | Manual retry |
| Transcription Not Configured | Missing credentials | Configure & rebuild | No |
| OSM Note Failed | API error | Check network / auth | Manual retry |
| Batch Timeout | Per-file timeout | None | Continue to next |

---

## Version History

- **v1.0** - Initial comprehensive documentation
  - 4 Activities, 3 Services, 3 API integrations
  - Room database with 2 status enums
  - 13 error handling paths documented
  - 7+ phase background recording timeline
  - Complete API endpoint documentation

---

**End of Documentation**
