package com.app.ocrscanner.domain.usecases

import android.graphics.Bitmap
import com.app.ocrscanner.ocr.OcrProcessor
import com.app.ocrscanner.ocr.OcrResult
import javax.inject.Inject

class ProcessOcrUseCase @Inject constructor(
    private val ocrProcessor: OcrProcessor,
) {
    suspend operator fun invoke(bitmap: Bitmap): OcrResult =
        ocrProcessor.processImage(bitmap)
}
