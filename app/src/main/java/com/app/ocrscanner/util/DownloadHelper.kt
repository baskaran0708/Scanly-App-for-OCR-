package com.app.ocrscanner.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

private const val DOWNLOAD_FOLDER = "LMI Scanly"

/**
 * Saves [sourceFile] to the public Downloads/LMI Scanly folder (visible in Files app).
 * Returns the display path string for showing in a Toast.
 * Never throws — failures return null.
 */
suspend fun saveFileToDownloads(
    context: Context,
    sourceFile: File,
    filename: String,
    mimeType: String,
): String? = withContext(Dispatchers.IO) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cv = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/$DOWNLOAD_FOLDER")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv,
            ) ?: return@withContext null

            context.contentResolver.openOutputStream(uri)?.use { os ->
                FileInputStream(sourceFile).use { it.copyTo(os) }
            }

            cv.clear()
            cv.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, cv, null, null)

            "Downloads/$DOWNLOAD_FOLDER/$filename"
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                DOWNLOAD_FOLDER,
            )
            dir.mkdirs()
            val dest = File(dir, filename)
            sourceFile.copyTo(dest, overwrite = true)
            MediaScannerConnection.scanFile(
                context, arrayOf(dest.absolutePath), arrayOf(mimeType), null,
            )
            "Downloads/$DOWNLOAD_FOLDER/$filename"
        }
    } catch (_: Exception) {
        null
    }
}

fun showSavedToast(context: Context, path: String?) {
    val msg = if (path != null) "Saved to $path" else "Save failed — check storage permission"
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
