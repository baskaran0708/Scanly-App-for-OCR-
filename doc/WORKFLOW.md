# LMI Scanly — User Workflows

## 1. Scan a Document

```
HomeScreen  →  tap "Scan" / FAB camera button
      │
      ▼
CameraScreen
  ├─ Live preview via CameraX PreviewView
  ├─ Shutter button  →  captures Bitmap via ImageCapture use-case
  └─ Gallery icon    →  launches system image/PDF picker
      │
      ▼  bitmap stored in BitmapHolder.capturedBitmap
      │
CropScreen
  ├─ CROP tab    →  drag corner handles to adjust crop rectangle
  ├─ ENHANCE tab →  brightness slider (real-time ColorFilter preview)
  ├─ FILTER tab  →  select preset (Normal / Greyscale / High Contrast / etc.)
  ├─ ROTATE tab  →  rotate 90° CW or CCW
  └─ Confirm     →  applyBrightness + cropBitmap (Dispatchers.Default)
      │
      ▼  processed Bitmap in BitmapHolder.processedBitmap
      │
OcrResultScreen
  ├─ ML Kit processes bitmap on Dispatchers.Default
  ├─ View modes: Image / Text / Split
  ├─ Tap text block to copy or extract
  ├─ Edit text inline
  ├─ Export row: PDF · Image · TXT · DOC  →  saved to Downloads/LMI Scanly/
  └─  "Save to Documents & Gallery"
         ├─ saves JPEG to app storage (getExternalFilesDir("scans"))
         ├─ copies to Pictures/LMI Scanly/ (gallery)
         └─ creates Room database record
              │
              ▼
        DocumentDetailScreen  (navigate, BitmapHolder cleared)
```

---

## 2. Import PDF or Image

```
HomeScreen  →  "Import PDF" quick action (or gallery icon in CameraScreen)
      │
      ▼  system file picker (ActivityResultContracts.GetContent)
      │
HomeViewModel.processImportedFile(uri)
  ├─ PDF  →  PdfProcessor.pdfToBitmaps → first page Bitmap
  └─ Image →  ContentResolver decodeStream → Bitmap
      │
      ▼  same CropScreen → OcrResultScreen flow as above
```

---

## 3. Export a Saved Document

```
DocumentDetailScreen  →  tap "Export" button (bottom bar)
      │
      ▼
ModalBottomSheet — choose format:
  ├─ PDF Document (.pdf)
  │    PdfProcessor.createPdfFromText  →  saveFileToDownloads  →  Toast
  ├─ JPEG Image (.jpg)
  │    Bitmap.compress(JPEG)           →  saveFileToDownloads  →  Toast
  ├─ PNG Image (.png)
  │    Bitmap.compress(PNG)            →  saveFileToDownloads  →  Toast
  ├─ Plain Text (.txt)
  │    PdfProcessor.saveTextFile       →  saveFileToDownloads  →  Toast
  ├─ Word Document (.rtf)
  │    buildRtf(text, title)           →  saveFileToDownloads  →  Toast
  └─ Share Text
       Android share sheet (text/plain)
```

Saved files appear at: **Internal Storage → Downloads → LMI Scanly → filename**

---

## 4. Change Theme

```
HomeScreen  →  Settings gear icon  →  SettingsScreen
      │
      ▼  Appearance section → tap Light / Dark / Follow system
      │
SettingsViewModel.setThemeMode(mode)
      │
UserPreferencesRepository.setTheme(mode.name)   ← DataStore write
      │
MainViewModel.isDarkTheme flow emits new value
      │
MainActivity recomposes ScanlyTheme(darkTheme = …)
      │
      ▼  entire app theme switches immediately, persisted across restarts
```

---

## 5. Search Documents

```
HomeScreen bottom nav  →  tab 2 "Search"
      │
      ▼
SearchContent composable
  ├─ OutlinedTextField  →  viewModel.onSearchQueryChange(query)
  ├─ 300 ms debounce via Flow.debounce
  └─ GetDocumentsUseCase.search(query)  →  Room LIKE query on title + text
      │
      ▼  results list rendered, tap to open DocumentDetailScreen
```

---

## 6. Star / Organise Documents

```
DocumentCard / DocumentListItem  →  star icon button
  └─ HomeViewModel.onToggleStar(id, currentlyStarred)
       →  DocumentRepository.setStarred(id, !current)  (Room UPDATE)

Filter chips: All · Starred · Recent
  └─ HomeViewModel.onFilterChange(filter)
       →  client-side filter on the current document list
```
