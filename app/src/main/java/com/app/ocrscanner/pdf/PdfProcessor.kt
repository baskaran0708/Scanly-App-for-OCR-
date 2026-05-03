package com.app.ocrscanner.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfProcessor @Inject constructor() {

    suspend fun pdfToBitmaps(context: Context, uri: Uri): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext bitmaps

        descriptor.use { pfd ->
            val renderer = PdfRenderer(pfd)
            try {
                for (i in 0 until renderer.pageCount) {
                    renderer.openPage(i).use { page ->
                        val scale = 2.0f
                        val width = (page.width * scale).toInt()
                        val height = (page.height * scale).toInt()
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmaps.add(bitmap)
                    }
                }
            } catch (e: Exception) {
                renderer.close()
                throw e
            }
            renderer.close()
        }
        bitmaps
    }

    suspend fun createPdfFromText(
        context: Context,
        text: String,
        title: String,
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 18f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val datePaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 10f
        }

        val margin = 40f
        var y = margin + 20f

        canvas.drawText(title, margin, y, titlePaint)
        y += 24f

        val dateStr = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(System.currentTimeMillis())
        canvas.drawText(dateStr, margin, y, datePaint)
        y += 6f

        canvas.drawLine(margin, y, 595f - margin, y, Paint().apply { color = Color.parseColor("#E2E8F0"); strokeWidth = 1f })
        y += 16f

        val maxWidth = 595f - 2 * margin
        val lines = wrapText(text, bodyPaint, maxWidth)
        for (line in lines) {
            if (y > 842f - margin) break
            canvas.drawText(line, margin, y, bodyPaint)
            y += 16f
        }

        pdfDocument.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val file = File(context.getExternalFilesDir("exports") ?: context.filesDir, "Scanly_${timeStamp}.pdf")
        FileOutputStream(file).use { pdfDocument.writeTo(it) }
        pdfDocument.close()
        file
    }

    suspend fun saveTextFile(context: Context, text: String, title: String): File = withContext(Dispatchers.IO) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val file = File(context.getExternalFilesDir("exports") ?: context.filesDir, "Scanly_${timeStamp}.txt")
        file.writeText("$title\n\n$text")
        file
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        for (paragraph in text.split("\n")) {
            val words = paragraph.split(" ")
            var line = StringBuilder()
            for (word in words) {
                val candidate = if (line.isEmpty()) word else "${line} $word"
                if (paint.measureText(candidate) <= maxWidth) {
                    line.append(if (line.isEmpty()) word else " $word")
                } else {
                    if (line.isNotEmpty()) result.add(line.toString())
                    line = StringBuilder(word)
                }
            }
            if (line.isNotEmpty()) result.add(line.toString())
            result.add("")
        }
        return result
    }
}
