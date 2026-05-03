package com.app.ocrscanner.ui.screens.crop

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CropUiState(
    val selectedFilter: ImageFilter = ImageFilter.ORIGINAL,
    val selectedTab: CropTab = CropTab.CROP,
    val rotation: Float = 0f,
    val cropLeft: Float = 0.02f,
    val cropTop: Float = 0.02f,
    val cropRight: Float = 0.98f,
    val cropBottom: Float = 0.98f,
    val brightness: Float = 0f,
)

enum class ImageFilter(val label: String) {
    ORIGINAL("Original"),
    AUTO("Auto"),
    MAGIC("Magic"),
    GRAYSCALE("Grayscale"),
    BW("B & W"),
    LIGHTEN("Lighten"),
}

enum class CropTab(val label: String) {
    CROP("Crop"),
    ENHANCE("Enhance"),
    FILTER("Filter"),
    ROTATE("Rotate"),
}

@HiltViewModel
class CropViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CropUiState())
    val uiState: StateFlow<CropUiState> = _uiState.asStateFlow()

    fun setFilter(filter: ImageFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun setTab(tab: CropTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun rotate90() {
        _uiState.value = _uiState.value.copy(rotation = (_uiState.value.rotation + 90f) % 360f)
    }

    fun rotateLeft() {
        _uiState.value = _uiState.value.copy(rotation = (_uiState.value.rotation - 90f + 360f) % 360f)
    }

    fun setBrightness(value: Float) {
        _uiState.value = _uiState.value.copy(brightness = value.coerceIn(-100f, 100f))
    }

    fun updateCropRect(left: Float, top: Float, right: Float, bottom: Float) {
        val s = _uiState.value
        _uiState.value = s.copy(
            cropLeft = left.coerceIn(0f, s.cropRight - 0.08f),
            cropTop = top.coerceIn(0f, s.cropBottom - 0.08f),
            cropRight = right.coerceIn(s.cropLeft + 0.08f, 1f),
            cropBottom = bottom.coerceIn(s.cropTop + 0.08f, 1f),
        )
    }

    fun cropBitmap(bitmap: Bitmap): Bitmap {
        val s = _uiState.value
        val x = (bitmap.width * s.cropLeft).toInt().coerceAtLeast(0)
        val y = (bitmap.height * s.cropTop).toInt().coerceAtLeast(0)
        val w = (bitmap.width * (s.cropRight - s.cropLeft)).toInt()
            .coerceAtLeast(1).coerceAtMost(bitmap.width - x)
        val h = (bitmap.height * (s.cropBottom - s.cropTop)).toInt()
            .coerceAtLeast(1).coerceAtMost(bitmap.height - y)
        return Bitmap.createBitmap(bitmap, x, y, w, h)
    }

    fun applyFilter(original: Bitmap, filter: ImageFilter, brightness: Float = 0f): Bitmap {
        val base = when (filter) {
            ImageFilter.ORIGINAL -> original.copy(Bitmap.Config.ARGB_8888, true)
            ImageFilter.AUTO -> applyColorMatrix(original, autoEnhanceMatrix())
            ImageFilter.MAGIC -> applyColorMatrix(original, magicColorMatrix())
            ImageFilter.GRAYSCALE -> applyColorMatrix(original, grayscaleMatrix())
            ImageFilter.BW -> applyColorMatrix(original, bwMatrix())
            ImageFilter.LIGHTEN -> applyColorMatrix(original, lightenMatrix())
        }
        return if (brightness != 0f) applyColorMatrix(base, brightnessMatrix(brightness)) else base
    }

    fun applyRotation(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        val matrix = android.graphics.Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun applyBrightness(bitmap: Bitmap, brightness: Float): Bitmap {
        if (brightness == 0f) return bitmap
        return applyColorMatrix(bitmap, brightnessMatrix(brightness))
    }

    private fun applyColorMatrix(bitmap: Bitmap, matrix: ColorMatrix): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun brightnessMatrix(brightness: Float) = ColorMatrix(floatArrayOf(
        1f, 0f, 0f, 0f, brightness,
        0f, 1f, 0f, 0f, brightness,
        0f, 0f, 1f, 0f, brightness,
        0f, 0f, 0f, 1f, 0f,
    ))

    private fun autoEnhanceMatrix() = ColorMatrix().apply {
        setScale(1.1f, 1.1f, 1.1f, 1f)
        val sat = ColorMatrix(); sat.setSaturation(1.2f); postConcat(sat)
    }

    private fun magicColorMatrix() = ColorMatrix().apply {
        setScale(1.2f, 1.2f, 1.2f, 1f)
        val sat = ColorMatrix(); sat.setSaturation(1.4f); postConcat(sat)
    }

    private fun grayscaleMatrix() = ColorMatrix().apply {
        setSaturation(0f)
        postConcat(ColorMatrix(floatArrayOf(
            1.1f, 0f, 0f, 0f, 0f,
            0f, 1.1f, 0f, 0f, 0f,
            0f, 0f, 1.1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )))
    }

    private fun bwMatrix() = ColorMatrix().apply {
        setSaturation(0f)
        postConcat(ColorMatrix(floatArrayOf(
            3f, 0f, 0f, 0f, -255f,
            0f, 3f, 0f, 0f, -255f,
            0f, 0f, 3f, 0f, -255f,
            0f, 0f, 0f, 1f, 0f,
        )))
    }

    private fun lightenMatrix() = ColorMatrix(floatArrayOf(
        0.9f, 0f, 0f, 0f, 40f,
        0f, 0.9f, 0f, 0f, 40f,
        0f, 0f, 0.9f, 0f, 40f,
        0f, 0f, 0f, 1f, 0f,
    ))
}
