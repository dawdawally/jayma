# SUNMI Printer SDK

The SUNMI Printer SDK is now integrated via Maven repository.

## Setup Instructions

The SDK is already configured in `app/build.gradle.kts`:
```kotlin
implementation("com.sunmi:printerx:1.0.17")
```

## Documentation

- Official Documentation: https://developer.sunmi.com/docs/en-US/cdixeghjk491/xdzceghjk502
- SDK Integration Guide: https://developer.sunmi.com/docs/en-US/cdixeghjk491/xdzceghjk502

## Usage

The SDK is automatically downloaded from Maven Central when you sync Gradle.
No manual JAR file installation is required.

## Implementation

See `app/src/main/java/com/jayma/pos/util/printer/PrinterService.kt` for the printer service implementation.
Replace the TODO comments with actual SUNMI SDK calls based on the documentation.
