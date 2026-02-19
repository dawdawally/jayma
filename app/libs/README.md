# SUNMI Printer SDK

Place the SUNMI Printer SDK JAR file here:
- `sunmiprinterlibrary.jar`

You can download it from:
- SUNMI Developer Portal: https://developer.sunmi.com/
- Or contact SUNMI support for the SDK

## Setup Instructions

1. Download the SUNMI Printer SDK JAR file
2. Place it in this `libs` folder
3. The build.gradle.kts is already configured to use it
4. Sync Gradle

## Note

If you have access to SUNMI's Maven repository, you can update `build.gradle.kts` to use:
```kotlin
implementation("com.sunmi:printerlibrary:1.0.0")
```
