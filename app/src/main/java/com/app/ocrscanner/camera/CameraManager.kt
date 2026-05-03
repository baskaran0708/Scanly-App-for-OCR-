package com.app.ocrscanner.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CameraManager @Inject constructor() {

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    suspend fun bindCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        flashEnabled: Boolean = false,
    ) {
        val provider = getCameraProvider(context)
        cameraProvider = provider

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
            .build()

        provider.unbindAll()
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture!!,
        )
    }

    fun updateFlash(enabled: Boolean) {
        imageCapture?.flashMode = if (enabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    suspend fun capturePhoto(context: Context): Bitmap = suspendCancellableCoroutine { cont ->
        val capture = imageCapture ?: run {
            cont.resumeWithException(IllegalStateException("Camera not bound"))
            return@suspendCancellableCoroutine
        }

        val outputFile = createImageFile(context)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bitmap = loadAndCorrectBitmap(outputFile.absolutePath)
                    cont.resume(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    cont.resumeWithException(exception)
                }
            }
        )
    }

    fun unbind() {
        cameraProvider?.unbindAll()
    }

    private suspend fun getCameraProvider(context: Context): ProcessCameraProvider =
        suspendCancellableCoroutine { cont ->
            ProcessCameraProvider.getInstance(context).also { future ->
                future.addListener(
                    { cont.resume(future.get()) },
                    ContextCompat.getMainExecutor(context)
                )
            }
        }

    private fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val storageDir = context.getExternalFilesDir("scans") ?: context.filesDir
        return File.createTempFile("SCAN_${timeStamp}_", ".jpg", storageDir)
    }

    private fun loadAndCorrectBitmap(path: String): Bitmap {
        val options = BitmapFactory.Options().apply { inSampleSize = 1 }
        val bitmap = BitmapFactory.decodeFile(path, options)
            ?: throw IllegalStateException("Failed to decode captured image")

        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (rotation == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(rotation) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

