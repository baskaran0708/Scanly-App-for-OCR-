
package com.app.ocrscanner.ui.screens.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ocrscanner.camera.CameraManager
import com.app.ocrscanner.pdf.PdfProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CameraUiState(
    val flashEnabled: Boolean = false,
    val autoMode: Boolean = true,
    val selectedMode: ScanMode = ScanMode.DOCUMENT,
    val isCapturing: Boolean = false,
    val error: String? = null,
)

enum class ScanMode(val label: String) {
    DOCUMENT("Document"),
    ID_CARD("ID Card"),
    BOOK("Book"),
    QR("QR / Barcode"),
    WHITEBOARD("Whiteboard"),
}

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraManager: CameraManager,
    private val pdfProcessor: PdfProcessor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun toggleFlash() {
        val newState = !_uiState.value.flashEnabled
        _uiState.value = _uiState.value.copy(flashEnabled = newState)
        cameraManager.updateFlash(newState)
    }

    fun setAutoMode(auto: Boolean) {
        _uiState.value = _uiState.value.copy(autoMode = auto)
    }

    fun setScanMode(mode: ScanMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun capturePhoto(context: Context, onSuccess: (Bitmap) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCapturing = true, error = null)
            runCatching { cameraManager.capturePhoto(context) }
                .onSuccess { bitmap ->
                    _uiState.value = _uiState.value.copy(isCapturing = false)
                    onSuccess(bitmap)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isCapturing = false, error = e.message)
                }
        }
    }

    fun bindCamera(context: Context, lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        viewModelScope.launch {
            runCatching {
                cameraManager.bindCamera(context, lifecycleOwner, previewView, _uiState.value.flashEnabled)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun unbindCamera() {
        cameraManager.unbind()
    }

    fun importPdf(context: Context, uri: Uri, onSuccess: (Bitmap) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCapturing = true, error = null)
            runCatching {
                val mimeType = context.contentResolver.getType(uri)
                if (mimeType == "application/pdf") {
                    pdfProcessor.pdfToBitmaps(context, uri).firstOrNull()
                        ?: throw IllegalStateException("Empty or unreadable PDF")
                } else {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        } ?: throw IllegalStateException("Cannot open image file")
                    }
                }
            }.onSuccess { bitmap ->
                _uiState.value = _uiState.value.copy(isCapturing = false)
                onSuccess(bitmap)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isCapturing = false, error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
