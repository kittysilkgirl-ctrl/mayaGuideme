---
okf_version: "0.1"
concept: "camera-pipeline"
project: "mayaGuideme"
source_files:
  - "app/src/main/java/com/mayaguideme/service/camera/CameraCaptureService.kt"
updated_at: "2026-11-21T00:35:00Z"
---

# Camera Capture Pipeline

## Data Contract
```kotlin
data class RawCaptureFrame(
    val bitmap: Bitmap,
    val captureEpochMs: Long
)
```

## Implementation Summary (`CameraCaptureService`)
- **Use Case Configuration:** `ImageCapture.Builder()` configured with:
  - `ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY`
  - `ImageCapture.FLASH_MODE_OFF`
- **Async Bridge:** `suspend fun captureRawFrame(): RawCaptureFrame` wraps CameraX's callback-based `imageCapture.takePicture(...)` in `suspendCancellableCoroutine` executed on `ContextCompat.getMainExecutor(context)`.
- **Orientation Normalization (`toUprightBitmap`):**
  - Converts `ImageProxy` via `toBitmap()`.
  - Reads `imageInfo.rotationDegrees`; if non-zero, applies `android.graphics.Matrix.postRotate(...)` and creates a rotated `Bitmap`.
  - Recycles `rawBitmap` immediately when a new rotated instance is created.
- **Resource & Cancellation Safety:**
  - Always calls `image.close()` in a `finally` block to prevent CameraX buffer starvation.
  - Checks `continuation.isActive` before resuming; recycles `uprightBitmap` if the coroutine was cancelled mid-capture.

## Planned Evolution (Not Yet Implemented)
- Dedicated preprocessing stage (high-pass / high-contrast grayscale filter downscaled to 768px) decoupled from `CameraCaptureService` so raw capture and landmark preprocessing remain testable in isolation.
