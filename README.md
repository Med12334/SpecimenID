# SpecimenID 🔍

[![Android CI](https://github.com/Med12334/SpecimenID/actions/workflows/build.yml/badge.svg)](https://github.com/Med12334/SpecimenID/actions/workflows/build.yml)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.22-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4?logo=android)](https://developer.android.com/jetpack/compose)

An advanced, production-ready Android application designed to detect dietary hazards and allergens in real-time. SpecimenID leverages on-device Machine Learning, Augmented Reality (AR) overlays, and cloud data integration to filter complex ingredient lists against user-defined dietary restrictions.

*(Note: Add a GIF or screenshot of the AR bounding boxes in action here)*

## 🚀 Key Features

* **Real-Time AR Allergen Detection:** Utilizes ML Kit Optical Character Recognition (OCR) to extract spatial coordinates (`Rect`) from physical labels, rendering dynamic red targeting boxes over hazards using a Jetpack Compose `Canvas` overlay.
* **Multilingual Hazard Engine:** An offline SQLite/Room database populated with a multilingual dictionary (English, French, Arabic). Evaluates live text streams via reactive Kotlin `Flow` pipelines (`combine`, `debounce`) to match allergens instantly.
* **Cloud Barcode Integration:** Seamlessly switches to an ML Kit Barcode scanning pipeline, utilizing Retrofit to asynchronously query the global Open Food Facts API and pipe downloaded ingredients back through the local threat engine.
* **Hardware-Optimized Camera Pipeline:** Implements Android CameraX with non-blocking backpressure strategies (`STRATEGY_KEEP_ONLY_LATEST`) to ensure fluid framerates and memory safety during heavy ML analysis.

## 🏗 Architecture & Tech Stack

Engineered with scalability and separation of concerns in mind, the codebase strictly adheres to **Multi-Module Clean Architecture** principles.

* **Architecture:** Multi-Module (`:app`, `:core:domain`, `:core:database`, `:core:vision`, `:feature:scanner`), Clean Architecture, MVI (Model-View-Intent).
* **UI:** 100% Jetpack Compose, dynamic layout scaling, reactive state observation (`StateFlow`).
* **Concurrency:** Kotlin Coroutines & Asynchronous Flows.
* **Dependency Injection:** Dagger Hilt.
* **Machine Learning:** Google ML Kit (Text Recognition v2, Barcode Scanning).
* **Local Storage:** Room Database with custom asset seeding and fallback destructive migrations.
* **Network:** Retrofit2, Gson converter.
* **CI/CD:** Automated GitHub Actions workflows for cloud APK compilation and dependency validation.

## ⚙️ Building the Project

This project uses Gradle version catalogs and requires Android Studio Iguana (or newer) to build locally.

```bash
git clone [https://github.com/Med12334/SpecimenID.git](https://github.com/Med12334/SpecimenID.git)
cd SpecimenID
./gradlew assembleDebug
