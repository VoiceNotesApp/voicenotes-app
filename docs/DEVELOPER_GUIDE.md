# Developer Guide

## Prerequisites

- Android Studio Arctic Fox (2020.3.1) or later
- JDK 17
- Android SDK API 26 minimum, API 35 target

## Setup

```bash
git clone https://github.com/c0dev0id/autorecord-app.git
cd autorecord-app
```

Open in Android Studio and sync Gradle.

## Project Structure

```
app/src/main/java/com/voicenotes/main/
├── database/
│   ├── Recording.kt          # Entity
│   ├── RecordingDao.kt       # Data access
│   ├── RecordingDatabase.kt  # Database singleton
│   ├── Converters.kt         # Type converters
│   └── RecordingMigration.kt # Migrations
├── MainActivity.kt           # App entry, starts OverlayService
├── OverlayService.kt         # Recording service
├── RecordingManagerActivity.kt # Recording list/management
├── BatchProcessingService.kt # Background transcription
├── TranscriptionService.kt   # Google Cloud STT
├── SettingsActivity.kt       # Preferences
├── DebugLogActivity.kt       # Log viewer
├── DebugLogger.kt            # Logging utility
├── NetworkUtils.kt           # Network helpers
└── VersionUtils.kt           # Version info
```

## Building

### Debug Build

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

```bash
./gradlew assembleRelease
# or
./build-release.sh
```

Requires signing configuration in `keystore.properties`:

```properties
storeFile=path/to/keystore.jks
storePassword=password
keyAlias=alias
keyPassword=password
```

Release builds use ProGuard/R8 for optimization and obfuscation.

## Google Cloud Setup

Optional for transcription functionality.

1. Create Google Cloud project
2. Enable Speech-to-Text API
3. Create service account with "Speech-to-Text User" role
4. Generate JSON key
5. Encode: `base64 -w 0 service-account-key.json > credentials.base64`
6. Add to `gradle.properties`:

```properties
GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON_BASE64=<base64 content>
```

## Database

Room database with `Recording` entity:

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| filename | String | Display filename |
| filepath | String | Full path |
| timestamp | Long | Recording time |
| latitude | Double | GPS latitude |
| longitude | Double | GPS longitude |
| v2sStatus | V2SStatus | Transcription status |
| v2sResult | String? | Transcription text |
| v2sFallback | Boolean | Fallback result flag |
| errorMsg | String? | Error message |
| createdAt | Long | Creation time |
| updatedAt | Long | Update time |

V2SStatus values: NOT_STARTED, PROCESSING, COMPLETED, FALLBACK, ERROR, DISABLED

## Audio Formats

- Android 10+ (API 29+): OGG/Opus
- Android 8-9 (API 26-28): AMR-WB

## Testing

```bash
./gradlew test
./gradlew lintDebug
```

Unit tests in `app/src/test/java/com/voicenotes/motorcycle/`.

## Versioning

Automatic git-based versioning:

- `versionName`: From `git describe --tags`
- `versionCode`: From `git rev-list --count HEAD`

Create releases with tags:

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

## Release Checklist

1. Run tests: `./gradlew test`
2. Run lint: `./gradlew lintRelease`
3. Update CHANGELOG.md
4. Commit and tag
5. Build: `./gradlew assembleRelease`
6. Test on device
7. Save ProGuard mapping file

## Permissions

Runtime (dangerous):
- RECORD_AUDIO
- ACCESS_FINE_LOCATION
- BLUETOOTH_CONNECT (API 31+)

Install-time (normal):
- INTERNET
- ACCESS_NETWORK_STATE
- FOREGROUND_SERVICE
- SYSTEM_ALERT_WINDOW

## Code Style

- Language: Kotlin
- Formatting: Android Studio defaults
- Classes: PascalCase
- Functions/variables: camelCase
- Constants: UPPER_SNAKE_CASE

## Troubleshooting

### Build fails
- Clear Gradle cache: `rm -rf ~/.gradle/caches`
- Clean: `./gradlew clean`

### Missing BuildConfig fields
- Check `gradle.properties` for required keys

### ProGuard issues
- Check mapping file for removed classes
- Add `-keep` rules as needed
