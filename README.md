# LMI Scanly

> **AI-powered OCR document scanner for Android** — built for Live Medica

LMI Scanly lets you capture, crop, extract text from, and archive physical documents directly on your Android device. OCR runs fully **on-device** (no internet required) using ML Kit Text Recognition v2.

---

## Features

- **Camera scanning** — live CameraX preview with one-tap capture
- **PDF / image import** — import any PDF (first page) or photo from the gallery
- **Interactive crop** — drag corner handles to crop precisely; adjust brightness with a slider; apply presets (greyscale, high-contrast, etc.); rotate 90°
- **On-device OCR** — ML Kit v2 extracts text with per-block bounding boxes and confidence scores
- **Editable text** — review and correct extracted text before saving
- **Document library** — Room database with full-text search, star/favourite, and kind-based colour coding
- **Export anywhere** — one-tap save to `Downloads/LMI Scanly/` as PDF, JPEG, PNG, TXT, or RTF; share as plain text
- **Theme switching** — Light / Dark / Follow system, persisted across restarts via DataStore

---

## Screenshots

> *(Add screenshots here before pushing to GitHub)*

---

## Tech Stack

| | |
|---|---|
| **Language** | Kotlin 2.x |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Clean Architecture (Data / Domain / UI) |
| **DI** | Hilt |
| **Database** | Room (SQLite) |
| **Settings** | Jetpack DataStore Preferences |
| **Camera** | CameraX |
| **OCR** | ML Kit Text Recognition v2 (on-device) |
| **Min SDK** | API 26 — Android 8.0 Oreo |
| **Target SDK** | API 36 |

---

## Project Structure

See [`doc/PROJECT_STRUCTURE.md`](doc/PROJECT_STRUCTURE.md) for the full file tree.

```
app/src/main/java/com/app/ocrscanner/
├── data/           # Room DB, DataStore, Repository
├── domain/         # Use cases (pure Kotlin)
├── ocr/            # ML Kit wrapper
├── pdf/            # PDF create + render
├── ui/             # Compose screens, ViewModels, theme, nav
└── util/           # DownloadHelper, RtfBuilder
```

---

## Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2) or newer
- JDK 17
- Android device or emulator running API 26+

### Build & Run

```bash
git clone https://github.com/YOUR_ORG/lmi-scanly.git
cd lmi-scanly
./gradlew assembleDebug
```

Install on device:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Permissions

The app requests the following at runtime:

| Permission | When | Why |
|---|---|---|
| `CAMERA` | First scan | Live camera preview |
| `READ_MEDIA_IMAGES` (API 33+) | Gallery import | Access photos |
| `READ_EXTERNAL_STORAGE` (API ≤ 32) | Gallery import | Access photos |
| `WRITE_EXTERNAL_STORAGE` (API ≤ 28) | First export | Write to Downloads |

No internet permission is required.

---

## Documentation

| Doc | Description |
|---|---|
| [`doc/ARCHITECTURE.md`](doc/ARCHITECTURE.md) | Layer diagram, settings pipeline, navigation flow, concurrency |
| [`doc/PROJECT_OVERVIEW.md`](doc/PROJECT_OVERVIEW.md) | What the app does, tech choices, screen map |
| [`doc/PROJECT_STRUCTURE.md`](doc/PROJECT_STRUCTURE.md) | Full file tree with descriptions |
| [`doc/WORKFLOW.md`](doc/WORKFLOW.md) | Step-by-step user workflow diagrams |

---

## Release Build

```bash
./gradlew assembleRelease
```

ProGuard / R8 minification and resource shrinking are enabled in `release` build type. Add your signing config to `app/build.gradle.kts` before distributing.

---

## Known Limitations

- Multi-page scanning captures pages separately; there is no multi-page PDF merge yet
- OCR language selection is displayed in settings but ML Kit defaults to Latin-script detection; additional language model packs require separate download
- Cloud backup setting is a UI placeholder; no cloud integration is implemented

---

## License

Proprietary — Live Medica Internal Use Only

---

*Built with ❤ by the Live Medica engineering team*
