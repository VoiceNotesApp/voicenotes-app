# Implementation Notes: FALLBACK Status for Empty Transcriptions

## Overview
This document describes the implementation of FALLBACK status handling for empty transcriptions in the AutoRecord app.

## Problem Statement
Previously, when Speech-to-Text returned an empty transcription (no speech detected), the recording was marked as COMPLETED with an empty v2sResult. This was misleading because COMPLETED should indicate a successful transcription with content, not an empty result.

## Solution
Treat empty transcriptions as a first-class FALLBACK status with appropriate UI feedback.

## Changes Made

### 1. BatchProcessingService.kt (Line 108)
**Before:**
```kotlin
v2sStatus = V2SStatus.COMPLETED,
```

**After:**
```kotlin
v2sStatus = if (transcribedText.isBlank()) V2SStatus.FALLBACK else V2SStatus.COMPLETED,
```

**Rationale:** Empty/blank transcriptions now result in FALLBACK status instead of COMPLETED. The v2sFallback flag is still set to true for consistency.

### 2. RecordingManagerActivity.kt (Lines 628-643)
**Before:**
```kotlin
if (recording.v2sResult.isNullOrBlank() ||
    recording.v2sStatus == V2SStatus.NOT_STARTED ||
    recording.v2sStatus == V2SStatus.DISABLED) {
    transcriptionEditText.setText("")
} else {
    transcriptionEditText.setText(recording.v2sResult)
}
```

**After:**
```kotlin
// Show helpful placeholder for FALLBACK status
if (recording.v2sResult.isNullOrBlank() ||
    recording.v2sStatus == V2SStatus.NOT_STARTED ||
    recording.v2sStatus == V2SStatus.DISABLED ||
    recording.v2sStatus == V2SStatus.FALLBACK) {
    transcriptionEditText.setText("")
    if (recording.v2sStatus == V2SStatus.FALLBACK) {
        transcriptionEditText.hint = "(empty transcription - no speech detected)"
    } else {
        transcriptionEditText.hint = "transcribed text goes here... field can be changed!"
    }
} else {
    transcriptionEditText.setText(recording.v2sResult)
    transcriptionEditText.hint = "transcribed text goes here... field can be changed!"
}
```

**Rationale:** 
- Added FALLBACK status to the empty text condition for clarity
- Show a helpful placeholder message "(empty transcription - no speech detected)" for FALLBACK status
- The FALLBACK check is necessary to differentiate the placeholder text between NOT_STARTED/DISABLED and FALLBACK recordings

### 3. TranscriptionService.kt (Lines 174-178)
**No changes needed** - Already correctly joins all Speech-to-Text result chunks:

```kotlin
val transcribedText = response.resultsList
    .joinToString(" ") { result ->
        result.alternativesList.firstOrNull()?.transcript ?: ""
    }
    .trim()
```

This ensures long transcriptions are not truncated by joining all result chunks with spaces.

## Status Flow

### Successful Transcription with Content
```
NOT_STARTED → PROCESSING → COMPLETED
v2sResult: "Hello world"
v2sFallback: false
```

### Empty Transcription (No Speech Detected)
```
NOT_STARTED → PROCESSING → FALLBACK
v2sResult: ""
v2sFallback: true
UI shows: "(empty transcription - no speech detected)"
```

### Transcription Error
```
NOT_STARTED → PROCESSING → ERROR
v2sResult: null
errorMsg: "Error message"
```

## UI Behavior

### Transcription EditText Placeholder
- **NOT_STARTED**: "transcribed text goes here... field can be changed!"
- **DISABLED**: "transcribed text goes here... field can be changed!"
- **FALLBACK**: "(empty transcription - no speech detected)"
- **COMPLETED**: No placeholder (shows transcribed text)

### Transcode Button
The existing UI already handles FALLBACK status correctly:
- **NOT_STARTED**: "Transcode" button with NOT_STARTED icon
- **PROCESSING**: "Processing" button (disabled)
- **COMPLETED**: "Retranscribe" button with COMPLETED icon
- **FALLBACK**: "Retry" button with ERROR icon
- **ERROR**: "Retry" button with ERROR icon

## Database Storage

### Recording Object
- Empty transcriptions store empty string in `v2sResult`
- Status is set to `V2SStatus.FALLBACK`
- Flag `v2sFallback` is set to `true`

### GPX/CSV Files
- Use fallback text with coordinates: `"lat,lng (no text)"`
- This is intentional - provides useful information in exported files

## Testing

Created comprehensive test suite in `TranscriptionFallbackTest.kt` with 9 test cases:
1. Empty transcription results in FALLBACK status
2. Blank transcription (spaces/tabs/newlines) results in FALLBACK status
3. Non-empty transcription results in COMPLETED status
4. FALLBACK recording validation
5. Status transition from PROCESSING to FALLBACK
6. Status transition from PROCESSING to COMPLETED
7. Edge case: single space treated as blank
8. Multiple result chunks joining
9. Empty result chunks result in FALLBACK

All tests include extensive debug output for easy troubleshooting.

## Backward Compatibility

These changes are backward compatible:
- No database schema changes
- Existing recordings with COMPLETED status and empty v2sResult will display correctly
- No impact on recordings with actual transcribed content
- UI gracefully handles all status values

## Security

No security vulnerabilities introduced:
- CodeQL analysis shows no issues
- No sensitive data exposed
- No new permissions required
- No new dependencies added
