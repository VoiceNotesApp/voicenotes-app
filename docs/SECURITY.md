# Security Policy

## Reporting Security Issues

If you discover a security vulnerability in this project, please report it privately:

1. **DO NOT** create a public GitHub issue
2. Email the maintainers directly (see repository owner contact)
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

We will respond within 48 hours and work to address the issue promptly.

---

## Credential Management Best Practices

### Google Cloud Service Account Credentials

**CRITICAL:** Never commit API credentials to version control!

#### Proper Configuration

1. **Create `gradle.properties` file** (this file is in `.gitignore`):
   ```properties
   GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON={"type":"service_account","project_id":"your-project",...}
   ```

2. **Verify `.gitignore` includes**:
   ```
   gradle.properties
   keystore.properties
   *.jks
   *.keystore
   ```

3. **Use template files for reference** (already included):
   - `gradle.properties.template` - Example structure without real credentials
   - `keystore.properties.template` - Example keystore configuration

#### Encoding Credentials

The app requires credentials in Base64-encoded JSON format:

```bash
# Encode your service account JSON file
base64 -w 0 service-account-key.json > credentials.base64

# Add the output to gradle.properties
echo "GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON_BASE64=$(cat credentials.base64)" >> gradle.properties
```

**Important:** The raw JSON can also be used directly in `gradle.properties` as shown in the template.

#### Environment Variables (CI/CD)

For GitHub Actions or other CI/CD:

1. Add credentials as **repository secrets**:
   - `GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON_BASE64`
   - `KEYSTORE_FILE` (base64-encoded keystore)
   - `KEYSTORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`

2. Use secrets in workflows without exposing them:
   ```yaml
   - name: Decode credentials
     run: |
       echo "${{ secrets.GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON_BASE64 }}" | base64 -d > credentials.json
   ```

---

## APK Security Considerations

### Release Builds

Release APKs use ProGuard/R8 for:
- **Code obfuscation**: Makes reverse engineering harder
- **Minification**: Reduces APK size
- **Resource shrinking**: Removes unused resources
- **Optimization**: Improves runtime performance

#### ProGuard Configuration

The project includes comprehensive ProGuard rules in `app/proguard-rules.pro`:
- Keeps Room database entities and DAOs
- Preserves Kotlin coroutines
- Protects Google Cloud API classes
- Maintains OkHttp functionality

**Review ProGuard mapping file** after each release build:
- Location: `app/build/outputs/mapping/release/mapping.txt`
- Needed for crash report deobfuscation
- **Store securely** for each release version

### Code Signing

Release APKs must be signed with a private keystore:

1. **Generate a keystore** (one-time setup):
   ```bash
   keytool -genkey -v -keystore release.keystore \
     -alias voicenotes -keyalg RSA -keysize 2048 \
     -validity 10000
   ```

2. **Create `keystore.properties`** (in `.gitignore`):
   ```properties
   storeFile=release.keystore
   storePassword=your_keystore_password
   keyAlias=voicenotes
   keyPassword=your_key_password
   ```

3. **Protect your keystore**:
   - Never commit to version control
   - Store backup in secure location
   - Use strong passwords
   - Consider using Android App Signing by Google Play

**Loss of keystore = unable to update app on Play Store**

---

## API Key Protection

### BuildConfig Fields

Credentials are embedded in BuildConfig during build:

```kotlin
// In app/build.gradle
buildConfigField "String", "GOOGLE_CLOUD_SERVICE_ACCOUNT_JSON_BASE64", 
    "\"${encoded_credentials}\""
```

This means credentials are:
- ✅ Not in source code
- ✅ Not in version control
- ⚠️ Embedded in APK (obfuscated by ProGuard)
- ⚠️ Can be extracted with effort

### Additional Protection Measures

1. **Restrict API key usage** in Google Cloud Console:
   - Set application restrictions (Android app package name + SHA-1)
   - Set API restrictions (only Speech-to-Text API)
   - Monitor usage and set quotas

2. **Rotate credentials periodically**:
   - Generate new service account keys quarterly
   - Revoke old keys after rotation
   - Update builds with new keys

3. **Consider backend proxy** for production apps:
   - Move API calls to your own backend
   - Backend calls Google Cloud APIs
   - APK only contains your backend URL
   - More secure but adds infrastructure complexity

---

## Runtime Security

### Permissions

The app requests only necessary permissions:
- `RECORD_AUDIO` - Required for voice recording
- `ACCESS_FINE_LOCATION` - Required for GPS tagging
- `SYSTEM_ALERT_WINDOW` - Required for overlay bubble
- `BLUETOOTH_CONNECT` - Optional for headset support
- `POST_NOTIFICATIONS` - Optional for background processing

**All permissions are requested at runtime** (Android 6.0+)

### Data Storage

- **Audio files**: Stored in app private storage (`filesDir/recordings/`)
- **Database**: Encrypted at rest by Android (API 29+)
- **Preferences**: Stored in default shared preferences
- **No external storage**: Prevents data leakage

### Network Security

- **HTTPS only**: All API calls use secure connections
- **Certificate pinning**: Not implemented (consider for production)
- **Network security config**: Default Android configuration

---

## Dependency Security

### Vulnerability Scanning

Before releases, scan dependencies:

```bash
# Check for known vulnerabilities
./gradlew dependencyCheckAnalyze

# Review dependencies
./gradlew dependencies
```

### Keep Dependencies Updated

Regularly update dependencies to patch security issues:
- Android Gradle Plugin
- Kotlin
- AndroidX libraries
- Google Cloud libraries
- OkHttp

**Test thoroughly after updates** to ensure compatibility.

---

## Pre-Release Security Checklist

Before releasing a new version:

- [ ] All API credentials removed from source code
- [ ] `.gitignore` properly configured
- [ ] ProGuard enabled and tested (`minifyEnabled true`)
- [ ] Release APK signed with production keystore
- [ ] Keystore backed up securely
- [ ] ProGuard mapping file saved for this version
- [ ] Dependencies scanned for vulnerabilities
- [ ] All dependencies up to date
- [ ] API keys restricted in Google Cloud Console
- [ ] Test APK on clean device (not development device)
- [ ] Verify no debug logs in release build
- [ ] Check for hardcoded secrets or test data
- [ ] Review permissions requested
- [ ] Test all features work with ProGuard enabled

---

## Secure Development Practices

### Code Review

- Review all PRs for security issues
- Check for hardcoded credentials
- Verify input validation
- Look for SQL injection risks (though Room mitigates this)

### Testing

- Test with real-world malicious inputs
- Test permission denial scenarios
- Test with restricted API keys
- Test on multiple Android versions

### Logging

- Use `DebugLogger` for debugging
- Disable debug logging in release builds
- Never log sensitive information (credentials, user data)
- Use ProGuard to remove debug code

---

## Incident Response

If credentials are compromised:

1. **Immediately revoke** the compromised credentials in Google Cloud Console
2. **Generate new credentials** and update all systems
3. **Review access logs** to assess impact
4. **Notify users** if their data may be affected
5. **Document the incident** and lessons learned
6. **Update security practices** to prevent recurrence

---

## Resources

- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [Google Cloud Security](https://cloud.google.com/security/best-practices)
- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)
- [ProGuard Manual](https://www.guardsquare.com/manual/home)

---

**Last Updated**: 2026-01-26  
**Version**: 1.0
