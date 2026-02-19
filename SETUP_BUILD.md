# Build Setup Instructions

## Quick Setup (Using Android Studio)

Since you have Android Studio installed, the easiest way to build is:

### Step 1: Open Project in Android Studio
1. Open Android Studio
2. Select **File > Open**
3. Navigate to your project folder: `C:\Users\dell\Desktop\jayma`
4. Click **OK**

### Step 2: Let Android Studio Sync
- Android Studio will automatically:
  - Download Gradle wrapper
  - Sync dependencies
  - Create `local.properties` with SDK path
  - Download required SDK components

### Step 3: Build Debug APK
1. Wait for sync to complete (check bottom status bar)
2. Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**
3. Wait for build to complete
4. Click **locate** in the notification to find your APK
5. APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Step 4: Build Release APK (After Debug Success)
1. Go to **Build > Generate Signed Bundle / APK**
2. Select **APK**
3. **First time?** Create a new keystore:
   - Click **Create new...**
   - Choose location: `jayma-pos-release.jks` (save in project root)
   - Set passwords (remember these!)
   - Fill in certificate details
   - Click **OK**
4. Select **release** build variant
5. Click **Finish**
6. APK location: `app/build/outputs/apk/release/app-release.apk`

## Alternative: Command Line Build

If you prefer command line after Android Studio syncs:

### After Android Studio Syncs Once:
1. Android Studio will create `gradle/wrapper/gradle-wrapper.jar`
2. Then you can use:
   ```bash
   gradlew.bat assembleDebug
   gradlew.bat assembleRelease
   ```

## Troubleshooting

### "SDK not found"
- Android Studio will create `local.properties` automatically
- If missing, create it with:
  ```
  sdk.dir=C\:\\Users\\dell\\AppData\\Local\\Android\\Sdk
  ```
  (Adjust path if your SDK is elsewhere)

### "Gradle sync failed"
- Check internet connection
- Go to **File > Invalidate Caches / Restart**
- Try **File > Sync Project with Gradle Files**

### Build Errors
- Make sure all dependencies are downloaded
- Check that Java JDK 17+ is installed
- Verify Android SDK is properly configured

## Testing the APK

### Install on Emulator:
1. Start Android Emulator from Android Studio
2. Drag APK to emulator window, or:
   ```bash
   adb install app-debug.apk
   ```

### Install on Physical Device:
1. Enable **Developer Options** on device
2. Enable **USB Debugging**
3. Connect device via USB
4. Run: `adb install app-debug.apk`

## What to Test (Without SUNMI Device)

✅ **Can Test:**
- App launch and navigation
- Product browsing and search
- Cart operations
- Checkout flow
- Barcode scanning (camera)
- Sales reports
- Background sync
- Database operations

❌ **Cannot Test:**
- Actual receipt printing (will fail gracefully)
- Hardware barcode scanner (camera scanner works)

The app will compile and run fine - printer operations just won't execute without SUNMI hardware.
