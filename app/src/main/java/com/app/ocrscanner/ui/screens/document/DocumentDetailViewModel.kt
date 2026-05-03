package com.app.ocrscanner.ui.screens.document

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.app.ocrscanner.util.buildRtf
import com.app.ocrscanner.util.saveFileToDownloads
import com.app.ocrscanner.util.showSavedToast
import androidx.lifecycle.viewModelScope
import com.app.ocrscanner.data.local.DocumentEntity
import com.app.ocrscanner.data.repository.DocumentRepository
import com.app.ocrscanner.domain.usecases.DeleteDocumentUseCase
import com.app.ocrscanner.pdf.PdfProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class DocumentDetailUiState(
    val document: DocumentEntity? = null,
    val scannedBitmap: Bitmap? = null,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val currentPage: Int = 0,
    val isEditingTitle: Boolean = false,
    val editTitle: String = "",
    val error: String? = null,
)

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val pdfProcessor: PdfProcessor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentDetailUiState())
    val uiState: StateFlow<DocumentDetailUiState> = _uiState.asStateFlow()

    fun loadDocument(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val doc = repository.getDocumentById(id)
            val bitmap = withContext(Dispatchers.IO) {
                doc?.filePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                        BitmapFactory.decodeFile(path, opts)
                    } else null
                }
            }
            _uiState.value = _uiState.value.copy(
                document = doc,
                scannedBitmap = bitmap,
                isLoading = false,
                editTitle = doc?.title ?: "",
            )
        }
    }

    fun setCurrentPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }

    fun startEditTitle() {
        _uiState.value = _uiState.value.copy(isEditingTitle = true)
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(editTitle = title)
    }

    fun commitTitleEdit() {
        val doc = _uiState.value.document ?: return
        val newTitle = _uiState.value.editTitle.trim().ifBlank { doc.title }
        viewModelScope.launch {
            repository.updateDocument(doc.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
            _uiState.value = _uiState.value.copy(
                document = doc.copy(title = newTitle),
                isEditingTitle = false,
            )
        }
    }

    fun cancelTitleEdit() {
        _uiState.value = _uiState.value.copy(
            isEditingTitle = false,
            editTitle = _uiState.value.document?.title ?: "",
        )
    }

    fun exportPdf(context: Context) {
        val doc = _uiState.value.document ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val file = runCatching {
                pdfProcessor.createPdfFromText(context, doc.extractedText, doc.title)
            }.getOrElse { e ->
                _uiState.value = _uiState.value.copy(isExporting = false, error = e.message)
                return@launch
            }
            _uiState.value = _uiState.value.copy(isExporting = false)
            val path = saveFileToDownloads(context, file, file.name, "application/pdf")
            showSavedToast(context, path)
        }
    }

    fun exportImage(context: Context) {
        val bitmap = _uiState.value.scannedBitmap ?: return
        val doc = _uiState.value.document ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val file = runCatching {
                withContext(Dispatchers.IO) {
                    val dir = context.getExternalFilesDir("exports") ?: context.filesDir
                    val f = File(dir, "${doc.title.take(20)}_export.jpg")
                    java.io.FileOutputStream(f).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
                    f
                }
            }.getOrElse { e ->
                _uiState.value = _uiState.value.copy(isExporting = false, error = e.message)
                return@launch
            }
            _uiState.value = _uiState.value.copy(isExporting = false)
            val path = saveFileToDownloads(context, file, file.name, "image/jpeg")
            showSavedToast(context, path)
        }
    }

    fun exportTxt(context: Context) {
        val doc = _uiState.value.document ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val file = runCatching {
                pdfProcessor.saveTextFile(context, doc.extractedText, doc.title)
            }.getOrElse { e ->
                _uiState.value = _uiState.value.copy(isExporting = false, error = e.message)
                return@launch
            }
            _uiState.value = _uiState.value.copy(isExporting = false)
            val path = saveFileToDownloads(context, file, file.name, "text/plain")
            showSavedToast(context, path)
        }
    }

    fun exportDoc(context: Context) {
        val doc = _uiState.value.document ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val file = runCatching {
                withContext(Dispatchers.IO) {
                    val rtf = buildRtf(doc.extractedText, doc.title)
                    val dir = context.getExternalFilesDir("exports") ?: context.filesDir
                    val safe = doc.title.take(20).replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                    val f = File(dir, "$safe.rtf")
                    f.writeText(rtf, Charsets.UTF_8)
                    f
                }
            }.getOrElse { e ->
                _uiState.value = _uiState.value.copy(isExporting = false, error = e.message)
                return@launch
            }
            _uiState.value = _uiState.value.copy(isExporting = false)
            val path = saveFileToDownloads(context, file, file.name, "application/rtf")
            showSavedToast(context, path)
        }
    }

    fun exportPng(context: Context) {
        val bitmap = _uiState.value.scannedBitmap ?: return
        val doc = _uiState.value.document ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val file = runCatching {
                withContext(Dispatchers.IO) {
                    val dir = context.getExternalFilesDir("exports") ?: context.filesDir
                    val safe = doc.title.take(20).replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                    val f = File(dir, "${safe}_export.png")
                    java.io.FileOutputStream(f).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    f
                }
            }.getOrElse { e ->
                _uiState.value = _uiState.value.copy(isExporting = false, error = e.message)
                return@launch
            }
            _uiState.value = _uiState.value.copy(isExporting = false)
            val path = saveFileToDownloads(context, file, file.name, "image/png")
            showSavedToast(context, path)
        }
    }

    fun shareText(context: Context) {
        val doc = _uiState.value.document ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, doc.extractedText)
            putExtra(Intent.EXTRA_SUBJECT, doc.title)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    fun deleteDocument(onDeleted: () -> Unit) {
        val doc = _uiState.value.document ?: return
        viewModelScope.launch {
            deleteDocumentUseCase(doc.id, doc.filePath)
            onDeleted()
        }
    }

    fun toggleStar() {
        val doc = _uiState.value.document ?: return
        viewModelScope.launch {
            repository.setStarred(doc.id, !doc.isStarred)
            _uiState.value = _uiState.value.copy(document = doc.copy(isStarred = !doc.isStarred))
        }
    }
}
