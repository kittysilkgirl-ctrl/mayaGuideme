

## Current Onboarding & Camera UI State (2026-11-28)

TelemetryConsentDialog and ByokApiKeysDialog are wired via OnboardingViewModel and TinkDataStorePreferencesStore in MainActivity. MainCameraContent is decomposed into BindCameraEffect, CameraCaptureOverlay, and CapturedLandmarkPreviewCard with specific exception catches, featuring a 100.dp x 100.dp grayscale thumbnail target alongside the prototype preview card.

---

okf\_version: "0.1" concept: "ui-and-permissions" project: "mayaGuideme" source\_files:

- "app/src/main/java/maya/ui/MainActivity.kt"  
- "app/src/main/java/maya/ui/base/FormFactorPreviews.kt"  
- "app/src/main/res/values/strings.xml" updated\_at: "2026-11-21T00:35:00Z"

---

# UI & Runtime Permission Flow

## `MainActivity` (`maya.ui`)

- **Root:** `ComponentActivity` hosting a `MaterialTheme` \-\> `Surface(fillMaxSize)` \-\> `Box(fillMaxSize)` Compose hierarchy.  
- **State Variables (currently local `remember`):**  
  - `hasCameraPermission: Boolean` — initialized via `ContextCompat.checkSelfPermission(..., Manifest.permission.CAMERA)`.  
  - `isCameraBound: Boolean` — tracks whether `ProcessCameraProvider` has bound `cameraService.imageCapture` to `CameraSelector.DEFAULT_BACK_CAMERA`.  
  - `capturedBitmap: Bitmap?` — most recent `RawCaptureFrame.bitmap`.  
  - `statusMessage: String?` — error/status feedback displayed centered in `MaterialTheme.colorScheme.error`.  
- **Lifecycle Binding:** `LaunchedEffect(hasCameraPermission)` retrieves `ProcessCameraProvider.getInstance(...)`, calls `unbindAll()`, and binds `cameraService.imageCapture` to `this@MainActivity`.  
- **Interactive Controls:**  
  - Bottom-centered Material 3 `Button` labeled `"Location"` (`padding(bottom = 32.dp)`): requests `CAMERA` permission if missing, or launches a coroutine calling `cameraService.captureRawFrame()` when bound.  
  - Top-end floating `Card` (`140.dp x 180.dp`, `8.dp` elevation, `16.dp` padding) rendering `capturedBitmap.asImageBitmap()` with `ContentScale.Crop` (`contentDescription = "Captured landmark"`).

## Multi-Device Previews (`FormFactorPreviews.kt`)

Custom multi-preview annotation in `maya.ui.base` covering four form factors (`showBackground = true`):

- `Phone` (`Devices.PHONE`)  
- `Foldable` (`Devices.FOLDABLE`)  
- `Tablet` (`Devices.TABLET`)  
- `Desktop` (`Devices.DESKTOP`)

## Grounding & Copy (`strings.xml`)

- `app_name`: `Maya Guide Me`  
- `grounding_default_prompt`: `You are safe. Take your time capturing your surroundings.`