# Screenshot Documentation

This document describes the screenshots needed for the README and other documentation.

## Required Screenshots

### 1. Main Activity - Initial State
**Filename**: `screenshot-main-initial.png`

**Description**: The main screen when first loaded after setup is complete.

**Should Show**:
- App title/header
- Status text showing "Ready" or initial state
- Settings/Configuration button
- Progress bar (hidden initially)
- Clean, minimal UI

**Device**: Any Android device running Android 8.0+

**Orientation**: Portrait preferred, but landscape acceptable

---

### 2. First Run Tutorial Dialog
**Filename**: `screenshot-tutorial-dialog.png`

**Description**: The explanation dialog that appears on first run after setup.

**Should Show**:
- Dialog title: "How This App Works"
- Full tutorial text with emojis
- List of 6 steps explaining the process
- "Start Recording" button
- Cannot be dismissed by tapping outside

**How to Capture**:
1. Clear app data to reset
2. Complete setup (save directory + trigger app)
3. Launch app again
4. Screenshot the tutorial dialog

---

### 3. Settings Activity
**Filename**: `screenshot-settings.png`

**Description**: The configuration screen showing all available settings.

**Should Show**:
- Recording location display
- "Choose Recording Location" button
- Trigger app display
- "Select Trigger App" button
- "Grant Required Permissions" button
- Current values for directory and trigger app
- Back button in toolbar

**Device State**: Should show some values already configured

---

### 4. App Selection Dialog
**Filename**: `screenshot-app-chooser.png`

**Description**: The dialog for selecting which app to launch after recording.

**Should Show**:
- Dialog title: "Select Trigger App"
- Scrollable list of installed apps
- App names sorted alphabetically
- Cancel button
- Common apps like Google Maps, Spotify, Waze visible

**How to Capture**:
1. Open Settings
2. Tap "Select Trigger App"
3. Screenshot the dialog

---

### 5. Recording in Progress
**Filename**: `screenshot-recording.png`

**Description**: The main screen while recording is in progress.

**Should Show**:
- Status text: "Recording for 10 seconds..."
- Progress bar visible (if applicable)
- Any location information
- App remains in foreground

**How to Capture**:
- Launch app
- Quickly take screenshot within 10-second recording window
- May require multiple attempts to time correctly

---

### 6. Location Acquisition
**Filename**: `screenshot-acquiring-location.png`

**Description**: The screen showing GPS location being acquired.

**Should Show**:
- Status text: "Acquiring location..."
- Progress bar visible
- App waiting for GPS lock

**How to Capture**:
- Start in area with poor GPS signal
- Launch app immediately when inside
- Screenshot during GPS acquisition phase

---

### 7. Permission Request
**Filename**: `screenshot-permissions.png`

**Description**: Android system permission request dialog.

**Should Show**:
- System permission dialog
- Requesting microphone, location, or Bluetooth permission
- Allow/Deny buttons
- App name visible

**How to Capture**:
1. Clear app data
2. Launch app
3. Screenshot when permission dialog appears

---

### 8. Setup Dialog
**Filename**: `screenshot-setup-required.png`

**Description**: The dialog that appears on very first launch when setup is needed.

**Should Show**:
- Dialog title: "Setup Required"
- Message explaining setup is needed
- "Open Settings" button
- Cannot be dismissed

**How to Capture**:
1. Clear app data completely
2. Launch app
3. Screenshot immediately

---

### 9. GPX File Example
**Filename**: `screenshot-gpx-example.png`

**Description**: A GPX file opened in a text editor or mapping application.

**Should Show**:
- XML structure of GPX file
- Waypoint entries with:
  - GPS coordinates
  - Timestamps
  - Waypoint names (transcribed text or filename)
  - Waypoint descriptions

**How to Capture**:
- Make several recordings
- Open `acquired_locations.gpx` in text editor
- Screenshot showing multiple waypoints

---

### 10. Recordings in File Manager
**Filename**: `screenshot-file-manager.png`

**Description**: The recording directory showing saved MP3 files.

**Should Show**:
- Files named with GPS coordinates and timestamps
- .mp3 file extensions
- File sizes (should be 1-2 MB each)
- Timestamps showing when created
- `acquired_locations.gpx` file also visible

**How to Capture**:
- Navigate to save directory in file manager
- Screenshot showing multiple recording files

---

## Optional Screenshots

### Bluetooth Device Connected
**Filename**: `screenshot-bluetooth-connected.png`

**Description**: Android Bluetooth settings showing a connected microphone/headset.

**Should Show**:
- Bluetooth device list
- Connected device with microphone capability
- Device connected and active

---

### Google Earth with Waypoints
**Filename**: `screenshot-google-earth.png`

**Description**: GPX file imported into Google Earth showing waypoints on map.

**Should Show**:
- Map view with multiple waypoint markers
- Waypoint names visible
- Route or path if applicable

---

### Recording Complete
**Filename**: `screenshot-complete.png`

**Description**: The screen after recording completes and is being transcribed.

**Should Show**:
- Status text: "Recording saved, transcribing..." or "Transcription complete"
- Any success indicators

---

## Screenshot Guidelines

### General Requirements

1. **Device Requirements**:
   - Android 8.0 (API 26) or higher
   - Standard Android UI (not heavily customized)
   - Reasonable screen resolution (1080x1920 or better)

2. **Quality**:
   - PNG format (lossless)
   - Clear, sharp images
   - Good lighting conditions
   - Readable text

3. **Privacy**:
   - No personal information visible
   - No sensitive location data
   - Use generic test data
   - Blur any identifying information

4. **Consistency**:
   - Use same device for all screenshots if possible
   - Same Android version
   - Consistent UI theme
   - Similar time of day for status bar

### Image Dimensions

- Portrait: 1080x1920 (or device native)
- Landscape: 1920x1080 (or device native)
- Do not scale down - provide original resolution
- Will be scaled for README display

### File Naming Convention

- Use descriptive names with hyphens
- Prefix with `screenshot-`
- Lowercase
- PNG format
- Example: `screenshot-main-initial.png`

## Directory Structure

```
autorecord-app/
├── screenshots/
│   ├── screenshot-main-initial.png
│   ├── screenshot-tutorial-dialog.png
│   ├── screenshot-settings.png
│   ├── screenshot-app-chooser.png
│   ├── screenshot-recording.png
│   ├── screenshot-acquiring-location.png
│   ├── screenshot-permissions.png
│   ├── screenshot-setup-required.png
│   ├── screenshot-gpx-example.png
│   └── screenshot-file-manager.png
└── README.md
```

## Adding Screenshots to README

Once screenshots are captured:

1. Create `screenshots/` directory in repository root
2. Add all screenshot files
3. Update README.md to reference actual files:

```markdown
### Main Screen
![Main Screen](screenshots/screenshot-main-initial.png)

### First Run Tutorial
![Tutorial Dialog](screenshots/screenshot-tutorial-dialog.png)

### Settings Screen
![Settings](screenshots/screenshot-settings.png)

### App Selection
![App Chooser](screenshots/screenshot-app-chooser.png)
```

## Screenshot Tools

### On Android Device

1. **Built-in Screenshot**:
   - Press Power + Volume Down simultaneously
   - Screenshots saved to Pictures/Screenshots

2. **ADB Screenshot**:
   ```bash
   adb shell screencap -p /sdcard/screenshot.png
   adb pull /sdcard/screenshot.png
   ```

3. **Android Studio**:
   - Device emulator has screenshot button
   - Camera icon in emulator toolbar

### Editing Tools

- Remove status bar if needed: Use GIMP or Photoshop
- Crop to relevant area
- Add annotations if helpful (arrows, highlights)
- Compress if file size is large

## Notes for Developers

- Screenshots should be updated when UI changes significantly
- Keep original full-resolution versions
- Compressed versions for web can be created
- Consider adding video demonstration as well
- Animated GIFs can show the flow better than static images

## Testing Screenshot Capture

Before finalizing screenshots:

1. View on different screen sizes
2. Check readability when scaled down
3. Verify no sensitive information visible
4. Confirm images are properly compressed
5. Test loading speed in README

## Localization

If supporting multiple languages in future:

- Create subdirectories: `screenshots/en/`, `screenshots/de/`, etc.
- Capture same screenshots in each language
- Update README per locale
