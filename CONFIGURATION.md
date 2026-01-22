# Configuration Guide for Motorcycle Voice Notes

This guide explains how to configure optional features in the Motorcycle Voice Notes app.

## Overview

The app has two optional online services that require configuration:

1. **Google Cloud Speech-to-Text** - For automatic transcription of voice recordings
2. **OpenStreetMap (OSM) OAuth** - For creating OSM notes with your voice recordings

**Important:** These features are **optional**. The app works without them - it will still record audio and capture GPS coordinates. However, transcription and OSM note creation will be disabled.

## For Users of Pre-built APKs

If you download a pre-built APK from GitHub Actions or Releases, these services may not work out of the box:

### Why Services May Not Work

- **Google Cloud Transcription**: Requires valid service account credentials to be embedded at build time
- **OSM OAuth**: Requires a valid OAuth Client ID that matches a registered application

### What You'll See

Without proper configuration:

- **In Settings**: Status indicators will show ⚠️ warnings:
  - `⚠ Google Cloud Speech-to-Text: Not configured (transcription disabled)`
  - `⚠ OSM OAuth Client ID: Not configured (using placeholder)`

- **During Recording**: Transcription will be skipped silently (audio and GPS still saved)

- **Manual Processing**: Button will work but transcription attempts will show friendly errors

- **OSM OAuth**: Clicking "Bind OSM Account" will show a detailed error message explaining the issue

### Your Options

1. **Use the app without online features** - Record audio with GPS coordinates (works perfectly)
2. **Build the app yourself** with your own credentials (see below)
3. **Wait for the maintainer** to update the credentials in the published builds

## For Developers/Contributors

To enable optional features during development:

### 1. Google Cloud Speech-to-Text Setup

#### Create a Google Cloud Service Account

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project (or select an existing one)
3. Enable the **Speech-to-Text API**:
   - Go to "APIs & Services" > "Library"
   - Search for "Cloud Speech-to-Text API"
   - Click "Enable"

4. Create a service account:
   - Go to "IAM & Admin" > "Service Accounts"
   - Click "Create Service Account"
   - Name: `motorcycle-voice-notes` (or your choice)
   - Click "Create and Continue"
   - Grant role: **Cloud Speech-to-Text API User**
   - Click "Continue" then "Done"

5. Create a JSON key:
   - Click on the service account you just created
   - Go to "Keys" tab
   - Click "Add Key" > "Create new key"
   - Choose **JSON** format
   - Click "Create" - the file downloads automatically

#### Configure gradle.properties

1. Copy the template file:
   ```bash
   cp gradle.properties.template gradle.properties
   ```

2. Edit `gradle.properties`:
   ```properties
   GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON={"type":"service_account","project_id":"your-project-id","private_key_id":"...","private_key":"-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n","client_email":"...","client_id":"...","auth_uri":"https://accounts.google.com/o/oauth2/auth","token_uri":"https://oauth2.googleapis.com/token","auth_provider_x509_cert_url":"https://www.googleapis.com/oauth2/v1/certs","client_x509_cert_url":"..."}
   ```

   **Important notes:**
   - The entire JSON must be on **one line**
   - Remove all newlines from the JSON (especially in the private_key field)
   - No need to escape quotes - gradle.properties handles it
   - Keep this file private! It's in `.gitignore` already

3. Verify the configuration:
   ```bash
   ./gradlew assembleDebug
   ```

### 2. OpenStreetMap OAuth Setup

#### Register an OAuth Application

1. Go to [OSM OAuth Applications](https://www.openstreetmap.org/oauth2/applications)
2. Click "Register new application"
3. Fill in the form:
   - **Name**: `Motorcycle Voice Notes` (or your choice)
   - **Redirect URIs**: `app.voicenotes.motorcycle://oauth`
   - **Confidential application**: Leave **unchecked** (this is a mobile app)
   - **Permissions**: 
     - ✓ Read user preferences (`read_prefs`)
     - ✓ Create notes (`write_notes`)
4. Click "Register"
5. Copy your **Client ID** from the application details page

#### Configure gradle.properties

1. Edit `gradle.properties`:
   ```properties
   OSM_CLIENT_ID=your_actual_client_id_here_abc123xyz
   ```

2. Rebuild the app:
   ```bash
   ./gradlew assembleDebug
   ```

3. In the app settings:
   - Click "Bind OSM Account"
   - Authenticate with your OSM credentials
   - Grant permissions
   - Enable "Add OSM Note" checkbox

### 3. Complete gradle.properties Example

Your `gradle.properties` should look like:

```properties
# Google Cloud Speech-to-Text service account (one line)
GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON={"type":"service_account","project_id":"my-project",...}

# OSM OAuth Client ID
OSM_CLIENT_ID=abc123xyz456
```

**Security:** Never commit `gradle.properties` to version control! It contains secrets.

## For Repository Maintainers

To enable features in GitHub Actions builds:

### Set GitHub Secrets

1. Go to your repository on GitHub
2. Click **Settings** > **Secrets and variables** > **Actions**
3. Click **New repository secret**

#### Add Google Cloud Credentials

- **Name**: `GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON`
- **Value**: Entire content of your service account JSON file (can be multi-line in GitHub Secrets)
- Click "Add secret"

#### Add OSM Client ID

- **Name**: `OSM_KEY`
- **Value**: Your OSM OAuth Client ID
- Click "Add secret"

### Verify Workflows

The GitHub Actions workflows (`.github/workflows/*.yml`) should already be configured to:

```yaml
env:
  GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON: ${{ secrets.GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON }}
  OSM_CLIENT_ID: ${{ secrets.OSM_KEY }}
```

These environment variables are automatically passed to the build process.

### Important Notes

- **Secrets are private** - they won't appear in logs or be accessible to pull request contributors
- **Match your registrations** - The Client ID must match your registered OSM application
- **Keep secrets updated** - If you rotate credentials, update the secrets
- **Test builds** - Download a build artifact and verify the features work

## Verification

After configuration, verify features are working:

### Check Settings Screen

Open the app settings and look for:

- ✓ **Google Cloud Speech-to-Text: Configured** (green text)
- ✓ **OSM OAuth Client ID: Configured** (green text)

### Test Transcription

1. Enable "Try Online processing during ride"
2. Launch the app to make a recording
3. Speak clearly during the recording
4. Check the debug log for transcription results
5. Check that a GPX waypoint was created with your transcribed text

### Test OSM Integration

1. Click "Bind OSM Account"
2. Authenticate in the browser
3. Enable "Add OSM Note" checkbox
4. Make a recording
5. Check the debug log for OSM note creation
6. Visit [OpenStreetMap](https://www.openstreetmap.org) and check for your note

## Troubleshooting

### Google Cloud Errors

**"Google Cloud credentials not configured"**
- Check that `GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON` is set in gradle.properties
- Verify the JSON is valid (use a JSON validator)
- Ensure the JSON is on one line

**"Invalid service account JSON format"**
- Check that the JSON contains required fields: `type`, `project_id`, `private_key`
- Verify you didn't truncate the JSON when copying

**"Authentication failed"**
- Ensure the Speech-to-Text API is enabled in your Google Cloud project
- Verify the service account has the "Cloud Speech-to-Text API User" role
- Check that the service account key hasn't been deleted or revoked

### OSM Errors

**"OSM Integration Not Configured"**
- Check that `OSM_CLIENT_ID` is set in gradle.properties
- Verify it's not still set to `your_osm_client_id`
- Rebuild the app after changing the client ID

**"unknown client" error in browser**
- The Client ID in the app doesn't match any registered OSM application
- Verify the Client ID matches exactly (copy-paste from OSM application page)
- Check that the redirect URI in your OSM app is `app.voicenotes.motorcycle://oauth`

**OAuth flow succeeds but note creation fails**
- Verify your OSM application has `write_notes` permission
- Check that you're authenticated (see account status in settings)
- Check the debug log for detailed error messages

## Additional Resources

- [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) - Complete build setup guide
- [README.md](README.md) - App overview and usage
- [Google Cloud Speech-to-Text API](https://cloud.google.com/speech-to-text/docs)
- [OpenStreetMap OAuth 2.0](https://wiki.openstreetmap.org/wiki/OAuth)

## Support

For issues and questions:
- Check the [Issues](https://github.com/c0dev0id/autorecord-app/issues) page
- Review the debug log in the app (Settings > View Debug Log)
- Consult the [OSM Wiki](https://wiki.openstreetmap.org/) for OSM-related questions
