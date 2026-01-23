# Settings Launcher Icon

## Overview

This app now includes two launcher icons on the Android home screen:

1. **Motorcycle Voice Notes** - The main app icon that launches MainActivity for recording
2. **Voice Notes Settings** - A settings icon that launches SettingsActivity directly

## Implementation

### Changes Made

#### 1. String Resource (`app/src/main/res/values/strings.xml`)
Added a new string resource for the settings launcher label:
```xml
<string name="settings_launcher_name">Voice Notes Settings</string>
```

#### 2. Activity Alias (`app/src/main/AndroidManifest.xml`)
Added an `<activity-alias>` element that creates a second launcher entry:
```xml
<activity-alias
    android:name=".SettingsLauncher"
    android:targetActivity=".SettingsActivity"
    android:exported="true"
    android:label="@string/settings_launcher_name"
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity-alias>
```

### How It Works

- **Activity Alias**: An `activity-alias` is an Android component that creates an additional entry point to an existing Activity without duplicating code
- **Intent Filter**: The MAIN/LAUNCHER intent filter tells Android to show this as a launchable app icon
- **Target Activity**: Points to `.SettingsActivity`, so tapping this icon opens settings directly
- **Label**: Uses the custom string resource to display "Voice Notes Settings" in the launcher
- **Icon**: Currently uses the same icon as the main app (maintainers can customize this later)

## Testing

### On a Physical Device or Emulator

1. Build and install the app:
   ```bash
   ./gradlew installDebug
   ```

2. Check your home screen launcher (or app drawer):
   - You should see **two icons** for this app:
     - "Motorcycle Voice Notes" (main app)
     - "Voice Notes Settings" (settings)

3. Test both icons:
   - Tap "Motorcycle Voice Notes" → Should start the recording flow (MainActivity)
   - Tap "Voice Notes Settings" → Should open the settings screen (SettingsActivity) directly

### Verification Without Building

You can verify the configuration is correct by checking:

```bash
# Check that the activity-alias exists in the manifest
grep -A 10 "activity-alias" app/src/main/AndroidManifest.xml

# Verify the string resource exists
grep "settings_launcher_name" app/src/main/res/values/strings.xml
```

## Customization

### To Use a Different Icon for Settings

The problem statement suggests using a gear overlay or different color for the settings icon. To implement this:

1. Create a new icon file (e.g., `ic_launcher_settings.png`) in the mipmap folders:
   - `app/src/main/res/mipmap-mdpi/ic_launcher_settings.png` (48x48 dp)
   - `app/src/main/res/mipmap-hdpi/ic_launcher_settings.png` (72x72 dp)
   - `app/src/main/res/mipmap-xhdpi/ic_launcher_settings.png` (96x96 dp)
   - `app/src/main/res/mipmap-xxhdpi/ic_launcher_settings.png` (144x144 dp)
   - `app/src/main/res/mipmap-xxxhdpi/ic_launcher_settings.png` (192x192 dp)

2. Update the activity-alias in `AndroidManifest.xml`:
   ```xml
   <activity-alias
       ...
       android:icon="@mipmap/ic_launcher_settings"
       android:roundIcon="@mipmap/ic_launcher_settings_round">
   ```

3. Recommended icon style:
   - Use the same base icon as the main app
   - Add a gear/cog overlay in the corner
   - Or use a different background color (e.g., darker shade)
   - Ensure it's visually distinguishable but clearly related to the main app

## Benefits

- **Quick Access**: Users can jump directly to settings without going through the main app
- **User Convenience**: Faster access to configuration options
- **No Code Duplication**: Uses activity-alias, so no extra Activity code needed
- **Minimal Changes**: Only requires manifest and string resource changes

## Launcher Behavior

When users long-press the main app icon, some Android launchers will also show app shortcuts (defined in `app/src/main/res/xml/shortcuts.xml`). The settings launcher icon is a separate, permanent icon that always appears in the launcher.

## References

- [Android Activity Alias Documentation](https://developer.android.com/guide/topics/manifest/activity-alias-element)
- [Multiple Launcher Icons Guide](https://developer.android.com/training/multiscreen/screendensities)
