# LMI Scanly — Project Structure

```
ocrapp/
├── app/
│   ├── build.gradle.kts                      # App-level Gradle config
│   └── src/main/
│       ├── AndroidManifest.xml               # Permissions, FileProvider
│       ├── res/
│       │   ├── values/
│       │   │   ├── strings.xml
│       │   │   └── themes.xml                # Base window theme (edge-to-edge)
│       │   └── xml/
│       │       └── file_paths.xml            # FileProvider paths for exports
│       └── java/com/app/ocrscanner/
│
│           ── Application ────────────────────────────────────────────────────
│           ├── ScanlyApplication.kt          # @HiltAndroidApp entry point
│           ├── MainActivity.kt               # Single Activity; theme switcher
│
│           ── DI ──────────────────────────────────────────────────────────────
│           └── di/
│               └── AppModule.kt              # Hilt: Room DB + DAO bindings
│
│           ── Data Layer ──────────────────────────────────────────────────────
│           └── data/
│               ├── local/
│               │   ├── AppDatabase.kt        # Room @Database
│               │   ├── DocumentDao.kt        # CRUD + full-text search queries
│               │   └── DocumentEntity.kt     # Room @Entity (documents table)
│               ├── preferences/
│               │   └── UserPreferencesRepository.kt  # DataStore — all settings
│               └── repository/
│                   └── DocumentRepository.kt # Façade over DocumentDao
│
│           ── Domain Layer ─────────────────────────────────────────────────────
│           └── domain/
│               └── usecases/
│                   ├── DeleteDocumentUseCase.kt
│                   ├── GetDocumentsUseCase.kt
│                   ├── ProcessOcrUseCase.kt
│                   └── SaveDocumentUseCase.kt
│
│           ── OCR & PDF ────────────────────────────────────────────────────────
│           ├── ocr/
│           │   ├── OcrProcessor.kt           # ML Kit Text Recognition v2 wrapper
│           │   ├── OcrResult.kt              # Result data class
│           │   └── OcrTextBlock.kt           # Per-block result with bounding box
│           └── pdf/
│               └── PdfProcessor.kt           # PDF create (text→PDF) + render (PDF→Bitmap)
│
│           ── UI Layer ─────────────────────────────────────────────────────────
│           └── ui/
│               ├── MainViewModel.kt          # Activity-scoped: resolves dark/light theme
│               ├── components/
│               │   └── ScanlyComponents.kt   # ScanlyFab, ScanlyChip, ConfidenceBadge
│               ├── navigation/
│               │   └── ScanlyNavigation.kt   # NavHost + Screen sealed class + BitmapHolder
│               ├── theme/
│               │   ├── Color.kt              # Design tokens (Primary, Accent, surfaces…)
│               │   ├── Theme.kt              # ScanlyTheme composable (light/dark schemes)
│               │   └── Type.kt               # ScanlyTypography
│               └── screens/
│                   ├── camera/
│                   │   └── CameraScreen.kt   # CameraX live preview, capture, gallery import
│                   ├── crop/
│                   │   ├── CropScreen.kt     # Interactive crop overlay, enhance, filter, rotate
│                   │   └── CropViewModel.kt  # Crop state, bitmap processing (filter/rotate/crop)
│                   ├── document/
│                   │   ├── DocumentDetailScreen.kt  # Saved doc viewer, export sheet, rename
│                   │   └── DocumentDetailViewModel.kt
│                   ├── home/
│                   │   ├── HomeScreen.kt     # Dashboard, search, docs list, quick actions
│                   │   └── HomeViewModel.kt  # Document list, search debounce, PDF import
│                   ├── ocr/
│                   │   ├── OcrResultScreen.kt  # Text blocks, edit, export, save
│                   │   └── OcrViewModel.kt
│                   └── settings/
│                       ├── SettingsScreen.kt   # Theme, quality, language, export defaults
│                       └── SettingsViewModel.kt
│
│           ── Utilities ────────────────────────────────────────────────────────
│           └── util/
│               ├── DownloadHelper.kt         # saveFileToDownloads() + showSavedToast()
│               └── RtfBuilder.kt             # buildRtf(text, title): String
│
├── doc/
│   ├── ARCHITECTURE.md       ← you are here
│   ├── PROJECT_OVERVIEW.md
│   ├── PROJECT_STRUCTURE.md
│   └── WORKFLOW.md
├── build.gradle.kts          # Root Gradle config
├── gradle/
│   └── libs.versions.toml    # Version catalog
└── README.md
```

---

## Key File Relationships

```
MainActivity
  └─ MainViewModel  ──reads──  UserPreferencesRepository
                                      │
                          SettingsViewModel  ──reads/writes──  UserPreferencesRepository

ScanlyNavigation
  ├─ HomeScreen        ── HomeViewModel ── GetDocumentsUseCase ── DocumentRepository ── DocumentDao
  ├─ CameraScreen      (CameraX, no ViewModel)
  ├─ CropScreen        ── CropViewModel  (bitmap transforms, all on Dispatchers.Default)
  ├─ OcrResultScreen   ── OcrViewModel  ── ProcessOcrUseCase ── OcrProcessor
  │                                     ── SaveDocumentUseCase ── DocumentRepository
  │                                     ── PdfProcessor
  └─ DocumentDetailScreen ── DocumentDetailViewModel ── DocumentRepository + PdfProcessor
```

---

## Shared Utilities

| Utility | Used by |
|---|---|
| `saveFileToDownloads()` | `OcrViewModel`, `DocumentDetailViewModel` |
| `showSavedToast()` | `OcrViewModel`, `DocumentDetailViewModel` |
| `buildRtf()` | `OcrViewModel.exportDoc`, `DocumentDetailViewModel.exportDoc` |
