---
okf_version: "0.1"
concept: "manifest-and-capabilities"
project: "mayaGuideme"
source_files:
  - "app/src/main/AndroidManifest.xml"
updated_at: "2026-11-21T00:35:00Z"
---

# Android Manifest & Hardware Capabilities

## Declared Permissions
- **Network:** `android.permission.INTERNET`
- **Location:** `android.permission.ACCESS_FINE_LOCATION`, `android.permission.ACCESS_COARSE_LOCATION`
- **Camera:** `android.permission.CAMERA`
- **Foreground Services:**
  - `android.permission.FOREGROUND_SERVICE`
  - `android.permission.FOREGROUND_SERVICE_LOCATION`
  - `android.permission.FOREGROUND_SERVICE_CAMERA`

## Hardware Features
- `android.hardware.camera` declared with `android:required="false"` so the app remains installable on devices without a back camera or during desktop/emulator form-factor testing.

## Application Entry
- `android:label="MayaGuideMe"`, `android:supportsRtl="true"`
- Launcher activity: `maya.ui.MainActivity` (`android:exported="true"`, `MAIN` + `LAUNCHER`).
