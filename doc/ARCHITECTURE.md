# LMI Scanly — Architecture

## Overview

LMI Scanly follows **Clean Architecture** with a strict three-layer separation. Data flows upward through domain use-cases; UI state flows downward through StateFlow. Dependency injection is handled by **Hilt**.

```
┌─────────────────────────────────────────────────────────┐
│  UI Layer  (Jetpack Compose + ViewModels)                │
│  screens/ · navigation/ · theme/ · components/          │
├─────────────────────────────────────────────────────────┤
│  Domain Layer  (pure Kotlin, no Android imports)        │
│  usecases/                                              │
├─────────────────────────────────────────────────────────┤
│  Data Layer  (Room · DataStore · File I/O · ML Kit)     │
│  local/ · repository/ · preferences/                    │
└─────────────────────────────────────────────────────────┘
```

---

## Layer Details

### UI Layer

| File | Responsibility |
|---|---|
| `MainActivity.kt` | Single Activity; owns `MainViewModel`; drives `ScanlyTheme` dark/light state |
| `ui/MainViewModel.kt` | Maps `UserPreferences.theme` → `isDarkTheme: StateFlow<Boolean?>` |
| `ui/navigation/ScanlyNavigation.kt` | NavHost; `BitmapHolder` passes bitmaps between screens |
| `ui/screens/*/` | One package per screen: Screen composable + ViewModel |
| `ui/components/` | Reusable Compose components (`ScanlyFab`, `ScanlyChip`, `ConfidenceBadge`) |
| `ui/theme/` | `Color.kt` · `Theme.kt` · `Typography.kt` |

### Domain Layer

| Use Case | Input | Output |
|---|---|---|
| `ProcessOcrUseCase` | `Bitmap` | `OcrResult` (text, blocks, confidence) |
| `SaveDocumentUseCase` | metadata fields | `Long` (new document id) |
| `GetDocumentsUseCase` | — / search query | `Flow<List<DocumentEntity>>` |
| `DeleteDocumentUseCase` | id + filePath | Unit |

### Data Layer

| Component | Technology | Purpose |
|---|---|---|
| `AppDatabase` | Room | Local SQLite for documents |
| `DocumentDao` | Room DAO | CRUD + full-text search queries |
| `DocumentRepository` | — | Single data-access façade over DAO |
| `UserPreferencesRepository` | Jetpack DataStore | Persist all user settings (theme, quality, language…) |
| `OcrProcessor` | ML Kit Text Recognition v2 | Bitmap → `OcrResult` |
| `PdfProcessor` | Android PdfDocument / PdfRenderer | Create PDF from text; render PDF pages to Bitmap |

---

## Settings Persistence Pipeline

```
User taps theme option in SettingsScreen
      │
      ▼
SettingsViewModel.setThemeMode(mode)
      │  viewModelScope.launch
      ▼
UserPreferencesRepository.setTheme(mode.name)
      │  DataStore.edit { it[KEY_THEME] = "DARK" }
      ▼
UserPreferences flow emits new snapshot
      │
      ├──▶ SettingsViewModel.uiState  (updates UI checkmark)
      │
      └──▶ MainViewModel.isDarkTheme  (Boolean? — Activity re-composes)
                  │
                  ▼
          ScanlyTheme(darkTheme = true)  ← theme switches immediately
```

---

## Navigation & Bitmap Passing

Navigation uses the Jetpack Navigation Compose `NavHost`. Bitmaps cannot be serialised into navigation arguments; they are passed through `BitmapHolder`, a simple singleton:

```
CameraScreen  ──capture──▶  BitmapHolder.capturedBitmap
                                    │
                                    ▼
                            CropScreen  ──confirm──▶  BitmapHolder.processedBitmap
                                                              │
                                                              ▼
                                                     OcrResultScreen
```

After saving, navigation pops to `DocumentDetail` and **both** holder fields are cleared to release bitmap memory.

---

## Dependency Injection

Hilt `@Singleton` bindings provided in `di/AppModule.kt`:

- `AppDatabase` → `DocumentDao`
- All `@Inject constructor` classes are auto-bound by Hilt

`@HiltViewModel` is used for all ViewModels; `hiltViewModel()` is used in composables.

---

## Concurrency

| Operation | Dispatcher |
|---|---|
| OCR inference | `Dispatchers.Default` |
| Bitmap crop / filter | `Dispatchers.Default` |
| File I/O (save, export) | `Dispatchers.IO` |
| DataStore reads/writes | Internal (IO-backed) |
| UI updates | `Dispatchers.Main` (viewModelScope default) |

All expensive bitmap work in `CropScreen` runs via `withContext(Dispatchers.Default)` inside a `rememberCoroutineScope` to avoid freezing the main thread.

---

## Export Pipeline

```
User taps PDF / TXT / Image / DOC in OcrResultScreen or DocumentDetailScreen
      │
      ▼
ViewModel.exportXxx(context, title)
      │  viewModelScope.launch (Main)
      │
      ├─ create temp file in  getExternalFilesDir("exports")
      │    (Dispatchers.IO)
      │
      ├─ saveFileToDownloads(context, file, name, mimeType)
      │    API 29+  → MediaStore.Downloads + IS_PENDING pattern
      │    API 28-  → Environment.DIRECTORY_DOWNLOADS + MediaScannerConnection
      │
      └─ showSavedToast(context, "Downloads/LMI Scanly/…")
```
