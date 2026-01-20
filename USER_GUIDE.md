# Motorcycle Voice Notes - User Guide

## Overview

Motorcycle Voice Notes is designed for riders who need to quickly record voice memos while on the road. The app automatically captures your GPS location, records audio, and then seamlessly returns you to your navigation or music app.

## First Time Setup

When you launch the app for the first time, you'll need to configure two essential settings:

### 1. Recording Storage Location

Choose where your voice recordings will be saved:

- Tap **"Set Recording Location"**
- The app will use the default location: `/storage/emulated/0/Music/VoiceNotes`
- This folder will be created automatically if it doesn't exist
- Recordings are saved as MP3 files with GPS coordinates and timestamps in the filename

**File naming format:** `latitude_longitude_timestamp.mp3`

**Example:** `34.052235_-118.243683_20260120_143022.mp3`

### 2. App to Launch After Recording

Select which app opens automatically after recording:

- Tap **"Select App to Launch"**
- Choose from your installed apps (e.g., Google Maps, Waze, Spotify, etc.)
- This app will open automatically after each recording completes

### 3. Grant Permissions

The app requires the following permissions to function:

- **Microphone** - To record your voice notes
- **Location** - To tag recordings with GPS coordinates
- **Notifications** (Android 13+) - To provide status updates

Tap **"Grant Required Permissions"** and allow all requested permissions.

## How It Works

### First Run After Setup

On your first actual recording session:

1. Launch the app
2. The app acquires your GPS location
3. Voice announcement: "Location acquired, recording for 10 seconds"
4. Records for exactly 10 seconds
5. Voice announcement: "Recording complete"
6. Saves the file with GPS coordinates in filename
7. Creates or updates `acquired_locations.gpx` with waypoint
8. Launches your selected app

### Second Run and Beyond

From the second launch onwards, for a faster experience:

1. Launch the app
2. Your trigger app launches **immediately**
3. Recording continues in the background
4. The recording process completes automatically
5. You can continue using your trigger app without interruption

## Recording Files

### Audio Format

- **Format:** MP3 (MPEG-4 AAC)
- **Bitrate:** 128 kbps
- **Sample Rate:** 44.1 kHz
- **Duration:** 10 seconds fixed

### GPX Location File

The app creates and maintains a file called `acquired_locations.gpx` in your recording folder. This file contains:

- GPS waypoints for each recording
- Waypoint name: `VoiceNote: filename.mp3`
- Timestamp of when the recording was made
- Can be imported into mapping applications like Google Earth, Garmin, etc.

## Screen Orientation

The app supports both portrait and landscape orientations, making it easy to use in different mounting configurations on your motorcycle.

## Configuration Changes

You can return to the configuration screen at any time by:

1. Tapping the **"Configuration"** button on the main screen
2. Make changes to:
   - Recording storage location
   - Trigger app selection
   - Permissions

## Tips for Motorcyclists

### Mounting Your Phone

- Use a quality motorcycle phone mount
- Position for easy reach but minimal distraction
- Ensure good GPS signal reception

### Recording Best Practices

- Speak clearly toward the phone
- Keep wind noise in mind - the microphone will pick up wind
- Consider a foam windscreen over your phone if needed
- 10 seconds is enough for a quick note - keep it concise

### Using Voice Commands

If your phone supports voice activation:

- Set up "OK Google" or "Hey Siri" to launch the app
- This allows truly hands-free operation while riding

### Safety First

- **Never manipulate your phone while riding**
- Pull over safely before adjusting settings
- Configure everything before you start riding
- The app is designed for minimal interaction

## Troubleshooting

### Location Not Acquired

- Ensure GPS/Location services are enabled on your device
- Check that location permissions are granted
- Try moving to an area with better sky visibility
- Wait a moment for GPS to acquire satellites

### Recording Not Saved

- Verify the storage location exists and is writable
- Check available storage space
- If the folder doesn't exist, the app will prompt you to reconfigure

### App Doesn't Launch Trigger App

- Ensure the trigger app is still installed
- Check that the trigger app package hasn't changed
- Reconfigure the trigger app in settings

### No Sound During Recording

- Check microphone permissions
- Ensure no other app is using the microphone
- Test your device microphone in another app

## Privacy & Data

- All recordings are stored locally on your device
- No data is sent to external servers
- GPS coordinates are only embedded in filenames and GPX file
- You have complete control over your recordings

## File Management

### Accessing Your Recordings

Recordings are stored in your configured directory, typically:
`/storage/emulated/0/Music/VoiceNotes/`

You can access them via:

- File manager apps
- Computer when connected via USB
- Cloud backup apps (Google Drive, Dropbox, etc.)

### Backing Up

Consider setting up automatic cloud backup:

1. Install your preferred cloud storage app
2. Configure it to backup the VoiceNotes folder
3. Your recordings will be automatically backed up

### GPX File Usage

The `acquired_locations.gpx` file can be:

- Opened in Google Earth to see all recording locations
- Imported into GPS devices
- Used in route planning software
- Shared with others for route documentation

## Advanced Usage

### Creating a Quick Launch Shortcut

For fastest access:

1. Long-press the app icon on your home screen
2. Drag it to your home screen or dock
3. Consider placing it in an easily accessible location

### Integration with Other Apps

The app works well with:

- **Navigation apps:** Google Maps, Waze, OsmAnd
- **Music apps:** Spotify, YouTube Music, Podcast apps
- **Communication apps:** Return to a call or messaging app

## Support

If you encounter issues or have suggestions:

- Check the [README](README.md) for general information
- Visit the [GitHub repository](https://github.com/c0dev0id/autorecord-app) for updates
- Report issues in the GitHub Issues section

## Version Information

This guide is for Motorcycle Voice Notes v1.0.0 and later.

---

**Remember:** The primary goal is safe riding. This app is designed to minimize distraction and maximize efficiency. Always prioritize road safety over technology.
