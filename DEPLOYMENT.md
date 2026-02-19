# Deployment Guide

This guide covers the deployment process for the Jayma POS Android application.

## Pre-Deployment Checklist

### 1. Build Configuration
- [x] ProGuard/R8 enabled for release builds
- [x] Code obfuscation configured
- [x] Resource shrinking enabled
- [x] Network security configured (HTTPS only in production)
- [x] Secure logging implemented

### 2. Version Management
- Current Version: `1.0.0` (versionCode: 1)
- Update `versionCode` and `versionName` in `app/build.gradle.kts` for each release

### 3. Signing Configuration

#### Generate Keystore
```bash
keytool -genkey -v -keystore jayma-pos-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias jayma-pos
```

#### Configure Signing in build.gradle.kts
Create `keystore.properties` file (add to `.gitignore`):
```properties
storeFile=../jayma-pos-release.jks
storePassword=your_store_password
keyAlias=jayma-pos
keyPassword=your_key_password
```

Then update `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))
            
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... existing config
        }
    }
}
```

## Building Release APK

### Generate Signed APK
1. In Android Studio: `Build > Generate Signed Bundle / APK`
2. Select `APK`
3. Choose your keystore
4. Select `release` build variant
5. Click `Finish`

### Build via Command Line
```bash
./gradlew assembleRelease
```

The APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

## Testing Release Build

1. **Install on Test Device**
   ```bash
   adb install app/build/outputs/apk/release/app-release.apk
   ```

2. **Test Scenarios**
   - [ ] App launches successfully
   - [ ] POS setup completes
   - [ ] Products sync correctly
   - [ ] Sales can be created offline
   - [ ] Receipt printing works
   - [ ] Barcode scanning works
   - [ ] Background sync functions
   - [ ] No crashes or ANRs

## Distribution Methods

### ⭐ Recommended: Direct APK Distribution (Best for POS Systems)

**Why this is recommended:**
- ✅ No app store approval needed
- ✅ Full control over distribution
- ✅ Quick deployment
- ✅ Perfect for enterprise/internal use
- ✅ No ongoing fees
- ✅ Can update immediately

**Steps:**
1. Build signed release APK (see above)
2. Upload APK to:
   - Your own web server
   - Cloud storage (Google Drive, Dropbox, OneDrive)
   - File hosting service
   - Internal company server
3. Share download link with users
4. Users download and install:
   - Enable "Install from Unknown Sources" in Android settings
   - Open downloaded APK file
   - Tap "Install"

**Update Process:**
- Build new APK with incremented version
- Upload to same location (replace old file or use versioned URLs)
- Notify users to download new version
- Users install over existing app (data preserved)

### Option 2: Enterprise MDM Distribution

**Best for:** Companies with multiple devices managed centrally

**Benefits:**
- ✅ Centralized device management
- ✅ Automatic updates
- ✅ Device policy enforcement
- ✅ Remote installation

**Popular MDM Solutions:**
- Microsoft Intune
- VMware Workspace ONE
- MobileIron
- SOTI MobiControl
- Samsung Knox Manage

### Option 3: Private App Store

**Best for:** Organizations with internal app store

**Options:**
- Samsung Galaxy Store (Enterprise)
- Huawei AppGallery (Enterprise)
- Custom internal app store
- F-Droid (open source alternative)

### Option 4: OTA (Over-The-Air) Updates

**Best for:** Automatic updates without user intervention

**Implementation:**
- Create update API endpoint
- Check for updates on app launch
- Download and install APK programmatically
- Requires `REQUEST_INSTALL_PACKAGES` permission

**Note:** This requires additional development work for update mechanism.

## Post-Deployment

### Monitoring
- Monitor crash reports (Firebase Crashlytics recommended)
- Track app analytics
- Monitor API performance
- Check sync success rates

### Updates
1. Increment `versionCode` in `build.gradle.kts`
2. Update `versionName`
3. Build new signed APK
4. Distribute update
5. Notify users of new version

## Version History

### Version 1.0.0 (Initial Release)
- Core POS functionality
- Offline-first architecture
- Product management
- Sales processing
- Receipt printing
- Barcode scanning
- Background sync

## Support

For issues or questions:
- GitHub Issues: [https://github.com/dawdawally/jayma/issues](https://github.com/dawdawally/jayma/issues)
- Documentation: See [README.md](README.md) and [DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md)
