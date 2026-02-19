# JAYMA - Android POS Application

Android Point of Sale (POS) application for SUNMI/POS devices with offline-first capabilities.

## 📋 Overview

This is a multi-tenant, offline-first POS system that integrates with the existing API at [https://gmexperteng.com](https://gmexperteng.com), ensuring POS operations continue seamlessly without internet connectivity.

## 🎯 Features

- **Offline-First Architecture**: All operations work offline, syncs when online
- **Multi-Warehouse Support**: Manage multiple warehouses
- **Product Management**: Browse, search, and filter products
- **Sales Processing**: Create sales with multiple payment methods
- **Draft Management**: Save and resume incomplete sales
- **Receipt Printing**: SUNMI printer integration
- **Barcode Scanning**: Support for hardware and camera scanners
- **Background Sync**: Automatic synchronization when online

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM + Repository Pattern
- **Local Database**: Room
- **Networking**: Retrofit
- **Dependency Injection**: Hilt
- **Background Tasks**: WorkManager
- **Coroutines**: For asynchronous operations

## 📡 API

**Base URL:** `https://gmexperteng.com`  
**Documentation:** [https://gmexperteng.com/](https://gmexperteng.com/)

The API is public (no authentication required).

## 📱 Target Platform

- Android (API 21+)
- SUNMI POS Devices
- Other Android-based POS hardware

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK (API 21+)
- Gradle 8.2+

### Setup

1. Clone the repository:
```bash
git clone https://github.com/dawdawally/jayma.git
cd jayma
```

2. Open the project in Android Studio

3. Create `local.properties` file (copy from `local.properties.example`) and set your Android SDK path:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

4. Sync Gradle and build the project

### Development Status

✅ **Phase 1:** API Integration & Understanding - Complete  
✅ **Phase 2:** Android Project Setup - Complete  
✅ **Phase 3:** POS Initialization - Complete  
✅ **Phase 4:** Product Management - Complete  
✅ **Phase 5:** POS Cart & Checkout - Complete  
✅ **Phase 6:** Receipt Printing - Complete  
✅ **Phase 7:** Background Sync - Complete  
✅ **Phase 8:** Barcode Scanning - Complete  
✅ **Phase 9:** Testing & QA - Complete  
✅ **Phase 10:** Security & Optimization - Complete  
✅ **Phase 11:** Additional Features - Complete  
✅ **Phase 12:** Deployment - Ready  

**Status:** 🎉 **Production Ready!**

See [DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md) for the complete development roadmap.

### Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/jayma/pos/
│   │   │   ├── data/
│   │   │   │   ├── local/          # Room database entities & DAOs
│   │   │   │   ├── remote/         # API services & models
│   │   │   │   └── repository/     # Repository implementations
│   │   │   ├── di/                 # Hilt dependency injection modules
│   │   │   └── ui/                 # Activities, Fragments, ViewModels
│   │   └── res/                    # Resources (layouts, strings, etc.)
│   └── test/                       # Unit tests
└── build.gradle.kts                # App-level build configuration
```

## 📦 Deployment

The app is ready for deployment. See deployment guides:
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - Step-by-step deployment instructions (Recommended)
- **[DEPLOYMENT.md](DEPLOYMENT.md)** - Complete deployment reference

**Quick Deployment:**
1. Build signed APK: `./gradlew assembleRelease`
2. Host APK on web server or cloud storage
3. Share download link with users
4. Users install directly (no app store needed)

**Note:** Firebase is **NOT required** for deployment. It's optional for analytics only.

## 📄 License

[Add your license here]

## 👥 Contributors

[Add contributors here]
