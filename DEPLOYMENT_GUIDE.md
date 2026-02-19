# Deployment Guide - Direct APK Distribution

This guide focuses on the **recommended deployment method** for POS systems: Direct APK Distribution.

## Why Direct APK Distribution?

✅ **No App Store Required** - Deploy immediately without approval  
✅ **Full Control** - You control when and how updates are distributed  
✅ **Cost Effective** - No developer fees or subscription costs  
✅ **Fast Deployment** - Deploy in minutes, not days/weeks  
✅ **Perfect for Enterprise** - Ideal for internal/company use  
✅ **Immediate Updates** - Push updates instantly when needed  

## Prerequisites

- ✅ Signed release APK (see [DEPLOYMENT.md](DEPLOYMENT.md) for signing setup)
- ✅ Web server or cloud storage for hosting APK
- ✅ Download link to share with users

## Step-by-Step Deployment

### Step 1: Build Release APK

```bash
# Generate signed release APK
./gradlew assembleRelease
```

The APK will be at: `app/build/outputs/apk/release/app-release.apk`

### Step 2: Host APK File

Choose one of these hosting options:

#### Option A: Your Own Web Server
1. Upload APK to your web server
2. Ensure proper MIME type: `application/vnd.android.package-archive`
3. Provide direct download link: `https://yourdomain.com/jayma-pos.apk`

#### Option B: Cloud Storage (Recommended for Quick Setup)

**Google Drive:**
1. Upload APK to Google Drive
2. Right-click → Share → Get link
3. Change link to allow "Anyone with the link"
4. Copy direct download link (use `uc?export=download` format)

**Dropbox:**
1. Upload APK to Dropbox
2. Right-click → Copy link
3. Change `?dl=0` to `?dl=1` in URL for direct download

**OneDrive:**
1. Upload APK to OneDrive
2. Right-click → Share → Get link
3. Use direct download link

#### Option C: GitHub Releases (Free & Reliable)
1. Create a new release on GitHub
2. Upload APK as release asset
3. Users download from release page
4. Automatic versioning and changelog

### Step 3: Share Download Link

Provide users with:
- Direct download link
- Installation instructions (see below)
- Version information

### Step 4: User Installation Instructions

**For Android Users:**

1. **Enable Unknown Sources:**
   - Go to Settings → Security
   - Enable "Install from Unknown Sources" or "Install Unknown Apps"
   - Select your browser/file manager

2. **Download APK:**
   - Open download link in browser
   - Tap "Download" or "Install"
   - Wait for download to complete

3. **Install APK:**
   - Open Downloads folder
   - Tap on `jayma-pos.apk`
   - Tap "Install"
   - Tap "Open" when installation completes

**For SUNMI Devices:**
- SUNMI devices typically allow installation from unknown sources by default
- Download and install directly

## Update Process

### For Users:
1. Download new APK version
2. Install over existing app (data is preserved)
3. App updates automatically

### For Administrators:
1. Build new APK with incremented version:
   ```kotlin
   versionCode = 2  // Increment by 1
   versionName = "1.0.1"  // Update version name
   ```
2. Upload new APK (replace old or use versioned URL)
3. Notify users of update
4. Optional: Implement in-app update checker

## In-App Update Checker (Optional)

You can add automatic update checking:

```kotlin
// Check for updates on app launch
fun checkForUpdates() {
    // Call your API to get latest version
    // Compare with BuildConfig.VERSION_CODE
    // Show update dialog if new version available
    // Download and install new APK
}
```

## Security Considerations

1. **HTTPS Only** - Always serve APK over HTTPS
2. **Verify APK** - Users can verify APK signature before installing
3. **Version Control** - Keep track of distributed versions
4. **Checksums** - Provide MD5/SHA256 checksums for verification

## Troubleshooting

### "Install Blocked" Error
- User needs to enable "Install from Unknown Sources"
- Check Android version (settings location varies)

### "App Not Installed" Error
- APK might be corrupted - re-download
- Check if device architecture matches (ARM, x86)
- Ensure sufficient storage space

### Update Issues
- Uninstall old version first (if versionCode conflict)
- Clear app cache before installing update

## Recommended Setup for POS Systems

**Best Practice:**
1. Host APK on your own server (if available)
2. Use versioned URLs: `jayma-pos-v1.0.0.apk`, `jayma-pos-v1.0.1.apk`
3. Create simple download page with:
   - Current version info
   - Download button
   - Installation instructions
   - Changelog
4. Implement update notification system
5. Keep old versions for rollback if needed

## Alternative: Enterprise MDM (For Large Deployments)

If managing 10+ devices, consider MDM solution:
- **Microsoft Intune** - Best for Microsoft environments
- **Samsung Knox Manage** - Best for Samsung/SUNMI devices
- **SOTI MobiControl** - Enterprise-grade MDM

MDM allows:
- Remote installation
- Automatic updates
- Device policy enforcement
- Centralized management

## FAQ

**Q: Do I need Firebase?**  
A: No, Firebase is optional. Only needed if you want analytics or crash reporting.

**Q: Can users update automatically?**  
A: Not by default. You can implement in-app update checker (requires development).

**Q: Is this secure?**  
A: Yes, as long as you serve APK over HTTPS and users verify the source.

**Q: Can I use this for commercial distribution?**  
A: Yes, but check local regulations. Some regions require app store distribution for commercial apps.

**Q: How do I handle updates?**  
A: Build new APK, upload to same location, notify users. They download and install over existing app.
