# Build Instructions

## Option 1: Build from Android Studio (Recommended - Easiest)

### Build Debug APK:
1. Open the project in Android Studio
2. Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**
3. Wait for build to complete
4. APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK:
1. Go to **Build > Generate Signed Bundle / APK**
2. Select **APK**
3. If you have a keystore, select it. Otherwise, create a new one:
   - Click "Create new..."
   - Fill in keystore details
   - Save keystore file securely
4. Select **release** build variant
5. Click **Finish**
6. APK location: `app/build/outputs/apk/release/app-release.apk`

## Option 2: Build from Command Line

### Prerequisites:
- Android SDK installed
- Java JDK 17+ installed
- Gradle wrapper files (gradlew.bat and gradle-wrapper.jar)

### If Gradle wrapper is missing:

**Download gradle-wrapper.jar:**
1. Download from: https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar
2. Save to: `gradle/wrapper/gradle-wrapper.jar`

**Or use Android Studio to generate wrapper:**
1. Open project in Android Studio
2. Go to **File > Settings > Build, Execution, Deployment > Build Tools > Gradle**
3. Select "Use Gradle from: 'gradle-wrapper.properties' file"
4. Sync project - this will generate wrapper files

### Build Commands:

**Debug APK:**
```bash
gradlew.bat assembleDebug
```

**Release APK:**
```bash
gradlew.bat assembleRelease
```

**Clean build:**
```bash
gradlew.bat clean assembleDebug
```

## Troubleshooting

### "Gradle wrapper not found"
- Use Android Studio to build (Option 1)
- Or download gradle-wrapper.jar manually (see above)

### "SDK not found"
- Ensure `local.properties` exists with correct SDK path:
  ```
  sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
  ```

### Build Errors
- Sync project in Android Studio first
- Clean build: `gradlew.bat clean`
- Invalidate caches: **File > Invalidate Caches / Restart**

## Testing Without SUNMI Device

✅ **You can test everything except:**
- Actual receipt printing (printer calls will fail gracefully)
- Hardware barcode scanner (camera scanner will work)

✅ **You can test:**
- All UI functionality
- Product browsing and search
- Cart operations
- Checkout flow
- Barcode scanning (using camera)
- Background sync
- Database operations
- Sales reports

The app will compile and run fine without SUNMI hardware - only printer operations won't execute.
