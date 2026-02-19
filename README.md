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

See [DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md) for the complete development roadmap.

## 📄 License

[Add your license here]

## 👥 Contributors

[Add contributors here]
