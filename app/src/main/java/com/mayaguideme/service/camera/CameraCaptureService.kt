package com.mayaguideme.service.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class RawCaptureFrame(
    val bitmap: Bitmap,
    val captureEpochMs: Long
)

class CameraCaptureService(
    private val context: Context
) {
    val imageCapture: ImageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        .setFlashMode(ImageCapture.FLASH_MODE_OFF)
        .build()

    suspend fun captureRawFrame(): RawCaptureFrame =
        suspendCancellableCoroutine { continuation ->
            val mainExecutor = ContextCompat.getMainExecutor(context)

            imageCapture.takePicture(
                mainExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val captureEpochMs = System.currentTimeMillis()
                        try {
                            val uprightBitmap = image.toUprightBitmap()
                            if (continuation.isActive) {
                                continuation.resume(
                                    RawCaptureFrame(
                                        bitmap = uprightBitmap,
                                        captureEpochMs = captureEpochMs
                                    )
                                )
                            } else {
                                uprightBitmap.recycle()
                            }
                        } catch (t: Throwable) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(t)
                            }
                        } finally {
                            image.close()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(exception)
                        }
                    }
                }
            )
        }

    private fun ImageProxy.toUprightBitmap(): Bitmap {
        val rawBitmap = this.toBitmap()
        val rotationDegrees = this.imageInfo.rotationDegrees
        if (rotationDegrees == 0) return rawBitmap

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }
        val rotatedBitmap = Bitmap.createBitmap(
            rawBitmap,
            0,
            0,
            rawBitmap.width,
            rawBitmap.height,
            matrix,
            true
        )
        if (rotatedBitmap !== rawBitmap) {
            rawBitmap.recycle()
        }
        return rotatedBitmap
    }
}