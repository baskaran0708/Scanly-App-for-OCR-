package com.app.ocrscanner.ocr

import android.graphics.Bitmap
import android.graphics.Matrix
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OcrTextBlock(
    val text: String,
    val boundingBox: android.graphics.Rect?,
    val confidence: Float,
    val kind: String = "body",
)

data class OcrResult(
    val fullText: String,
    val blocks: List<OcrTextBlock>,
    val confidence: Float,
    val error: String? = null,
)

@Singleton
class OcrProcessor @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun processImage(bitmap: Bitmap): OcrResult {
        val optimized = optimizeBitmap(bitmap)
        val image = InputImage.fromBitmap(optimized, 0)

        return suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (visionText.text.isBlank()) {
                        cont.resume(OcrResult(fullText = "", blocks = emptyList(), confidence = 0f, error = "No text detected"))
                        return@addOnSuccessListener
                    }

                    val blocks = visionText.textBlocks.flatMap { block ->
                        block.lines.map { line ->
                            val avgConf = line.elements.mapNotNull { it.confidence }.average()
                                .takeIf { it.isFinite() }?.toFloat() ?: 0.85f
                            OcrTextBlock(
                                text = line.text,
                                boundingBox = line.boundingBox,
                                confidence = avgConf,
                                kind = inferKind(line.text),
                            )
                        }
                    }

                    val overallConf = if (blocks.isEmpty()) 0f
                    else blocks.map { it.confidence }.average().toFloat()

                    cont.resume(
                        OcrResult(
                            fullText = visionText.text,
                            blocks = blocks,
                            confidence = overallConf,
                        )
                    )
                }
                .addOnFailureListener { e ->
                    cont.resume(OcrResult(fullText = "", blocks = emptyList(), confidence = 0f, error = e.message))
                }

            cont.invokeOnCancellation { recognizer.close() }
        }
    }

    private fun optimizeBitmap(original: Bitmap): Bitmap {
        val maxDimension = 2048
        val width = original.width
        val height = original.height

        if (width <= maxDimension && height <= maxDimension) return original

        val scale = maxDimension.toFloat() / maxOf(width, height)
        val matrix = Matrix().apply { postScale(scale, scale) }
        return Bitmap.createBitmap(original, 0, 0, width, height, matrix, true)
    }

    private fun inferKind(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains(Regex("\\d+\\.\\d+|\\d+/\\d+")) -> "value"
            lower.contains(Regex("mg|ml|g/dl|k/µl|%|mmhg")) -> "value"
            text.length < 30 && text.uppercase() == text -> "header"
            text.length > 100 -> "paragraph"
            else -> "body"
        }
    }
}
