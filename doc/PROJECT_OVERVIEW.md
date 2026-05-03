# LMI Scanly — Project Overview

## What is LMI Scanly?

LMI Scanly is an Android OCR document scanner built for **Live Medica**. It allows users to:

- Capture documents with the device camera or import from the gallery / PDF files
- Crop and enhance images interactively
- Extract text using **ML Kit Text Recognition v2** (on-device, offline)
- Review, edit, and star the extracted text
- Save documents to an in-app library (Room database)
- Export documents as **PDF, TXT, RTF/DOC, or JPEG** directly to `Downloads/LMI Scanly/`
- Switch between **Light / Dark / System** themes (persisted across app restarts)

## Target Users

Medical professionals at Live Medica who need to digitise and archive physical documents (lab reports, prescriptions, imaging reports, forms).

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.x |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room (SQLite) |
| Settings | Jetpack DataStore Preferences |
| Camera | CameraX |
| OCR | ML Kit Text Recognition v2 |
| PDF | Android PdfDocument / PdfRenderer |
| Image processing | Android Bitmap / ColorMatrix |
| Async | Kotlin Coroutines + Flow |
| Navigation | Jetpack Navigation Compose |
| Permissions | Accompanist Permissions |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 36 |

## Key Design Decisions

### On-device OCR
ML Kit runs fully offline — no internet permission required. Text recognition quality matches the camera image quality; the Enhance tab lets users adjust brightness before confirming.

### Single-Activity, Compose-first
There is one `Activity` (`MainActivity`). All screens are Compose composables composed inside a single `NavHost`. This eliminates the Fragment back-stack complexity and allows seamless animations.

### DataStore over SharedPreferences
All user settings (theme, image quality, OCR language, export defaults) are persisted with Jetpack DataStore. This guarantees atomic writes and coroutine-friendly reads without `StrictMode` violations.

### MediaStore Downloads API
Exports use `MediaStore.Downloads` on API 29+ and `Environment.DIRECTORY_DOWNLOADS` on older devices — no `WRITE_EXTERNAL_STORAGE` permission needed on modern Android, and files appear in the Files app under `Downloads/LMI Scanly/`.

## App Screens

| Screen | Route | Purpose |
|---|---|---|
| Home | `home` | Dashboard, recent scans, search, quick actions |
| Camera | `camera` | Live camera preview with CameraX; gallery import |
| Crop | `crop` | Interactive crop handles, brightness, filters, rotation |
| OCR Result | `ocr_result` | Text blocks, editable text, export, save to library |
| Document Detail | `document/{id}` | Saved document view, re-export, rename, delete |
| Settings | `settings` | Theme, quality, language, export defaults |

## Document Kinds

Documents are tagged with a `kind` field used for colour coding in the UI:

| Kind | Description | Colour |
|---|---|---|
| `lab` | Lab reports | Deep Blue |
| `rx` | Prescriptions | Teal |
| `imaging` | Imaging / X-ray | Dark Navy |
| `form` | Forms | Grey |
| `doc` | General documents | Slate |
