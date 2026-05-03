package com.app.ocrscanner.ui.screens.ocr

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ocrscanner.domain.usecases.ProcessOcrUseCase
import com.app.ocrscanner.domain.usecases.SaveDocumentUseCase
import com.app.ocrscanner.ocr.OcrTextBlock
import com.app.ocrscanner.pdf.PdfProcessor
import com.app.ocrscanner.util.buildRtf
import com.app.ocrscanner.util.saveFileToDownloads
import com.app.ocrscanner.util.showSavedToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class OcrUiState(
    val isProcessing: Boolean = false,
    val fullText: String = "",
    val blocks: List<OcrTextBlock> = emptyList(),
    val confidence: Float = 0f,
    val selectedBlockId: Int = -1,
    val viewMode: OcrViewMode = OcrViewMode.IMAGE,
    val editedText: String = "",
    val error: String? = null,
    val isSaving: Boolean = false,
    val savedToGallery: Boolean = false,
)

enum class OcrViewMode(val label: String) {
    IMAGE("Image"),
    TEXT("Text"),
    SPLIT("Split"),
}

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val processOcrUseCase: ProcessOcrUseCase,
    private val saveDocumentUseCase: SaveDocumentUseCase,
    private val pdfProcessor: PdfProcessor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    fun processOcr(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            runCatching {
                withContext(Dispatchers.Default) { processOcrUseCase(bitmap) }
            }.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    fullText = result.fullText,
                    editedText = result.fullText,
                    blocks = result.blocks,
                    confidence = result.confidence,
                    error = result.error,
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "OCR processing failed",
                )
            }
        }
    }

    fun selectBlock(index: Int) {
        _uiState.value = _uiState.value.copy(selectedBlockId = index)
    }

    fun setViewMode(mode: OcrViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    fun updateText(text: String) {
        _uiState.value = _uiState.value.copy(editedText = text)
    }

    fun clearGallerySavedFlag() {
        _uiState.value = _uiState.value.copy(savedToGallery = false)
    }

    fun saveDocument(context: Context, bitmap: Bitmap, title: String, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            runCatching {
                val file = saveBitmapToFile(context, bitmap)
                // Gallery save is best-effort — we also trigger it after navigation via the flag
                saveToGallery(context, bitmap)
                saveDocumentUseCase(
                    title = title,
                    filePath = file.absolutePath,
                    extractedText = _uiState.value.editedText,
                    pageCount = 1,
                    fileSizeBytes = file.length(),
                    kind = "doc",
                )
            }.onSuccess { docId ->
                _uiState.value = _uiState.value.copy(isSaving = false)
                // Navigate first, then set the gallery flag so the LaunchedEffect
                // in OcrResultScreen doesn't fire while the screen is being destroyed.
                onSaved(docId)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Save failed. Please try again.",
                )
            }
        }
    }

    fun exportPdf(context: Context, title: String) {
        viewModelScope.launch {
            val file = runCatching {
                pdfProcessor.createPdfFromText(context, _uiState.value.editedText, title)
            }.getOrElse { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
                return@launch
            }
            val path = saveFileToDownloads(context, file, file.name, "application/pdf")
            showSavedToast(context, path)
        }
    }

    fun exportTxt(context: Context, title: String) {
        viewModelScope.launch {
            val file = runCatching {
                pdfProcessor.saveTextFile(context, _uiState.value.editedText, title)
            }.getOrElse { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
                return@launch
            }
            val path = saveFileToDownloads(context, file, file.name, "text/plain")
            showSavedToast(context, path)
        }
    }

    fun exportImage(context: Context, bitmap: Bitmap, title: String) {
        viewModelScope.launch {
            val file = runCatching {
                withContext(Dispatchers.IO) {
                    val dir = context.getExternalFilesDir("exports") ?: context.filesDir
                    val safe = title.take(20).replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                    val f = File(dir, "${safe}_scan.jpg")
                    FileOutputStream(f).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
                    f
                }
            }.getOrElse { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
                return@launch
            }
            val path = saveFileToDownloads(context, file, file.name, "image/jpeg")
            showSavedToast(context, path)
        }
    }

    fun exportDoc(context: Context, title: String) {
        viewModelScope.launch {
            val file = runCatching {
                withContext(Dispatchers.IO) {
                    val rtf = buildRtf(_uiState.value.editedText, title)
                    val dir = context.getExternalFilesDir("exports") ?: context.filesDir
                    val safe = title.take(20).replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                    val f = File(dir, "$safe.rtf")
                    f.writeText(rtf, Charsets.UTF_8)
                    f
                }
            }.getOrElse { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
                return@launch
            }
            val path = saveFileToDownloads(context, file, file.name, "application/rtf")
            showSavedToast(context, path)
        }
    }

    private suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap): File =
        withContext(Dispatchers.IO) {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
            val dir = context.getExternalFilesDir("scans") ?: context.filesDir
            val file = File(dir, "SCAN_${timeStamp}.jpg")
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            }
            file
        }

    private suspend fun saveToGallery(context: Context, bitmap: Bitmap) =
        withContext(Dispatchers.IO) {
            try {
                val filename = "Scanly_${System.currentTimeMillis()}.jpg"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val cv = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/LMI Scanly")
                    }
                    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
                    uri?.let { u ->
                        context.contentResolver.openOutputStream(u)?.use { stream ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val dir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "LMI Scanly",
                    )
                    dir.mkdirs()
                    val file = File(dir, filename)
                    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                    MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
                }
            } catch (_: Exception) {
                // Gallery save is best-effort; don't fail the whole save
            }
        }
}
