**Verify Google Drive Sync & Path Consistency:** Confirm that Google Drive sync state is up to date and that folder/file paths remain consistent across *thinkpad-dev/mayaGuideme/src* and *thinkpad-dev/mayaGuideme/src/okf* before completing closeout.

---

## okf\_version: "0.1" concept: "end-of-session-procedure" project: "mayaGuideme" priority: "critical" updated\_at: "2026-11-25T00:50:00Z"

# End-of-Session Closeout Procedure (Mandatory)

Run this checklist automatically whenever the developer signals the end of a coding session (e.g., "that's enough for tonight", "wrapping up", "done for now"). Do not wait for the developer to remember OKF updates manually.

## 1\. Verify Code & Build Hygiene

- Confirm the last Gradle build (`./gradlew :app:assembleDebug`) succeeded, or explicitly record any known WIP failure in the milestone snapshot.  
- Run `git status` and review `git diff --stat` to ensure no stray IDE-agent edits, untracked temp files, or secrets are sitting in the working tree.  
- Audit temporary scaffolding with:

```sh
git grep -n "DISPOSABLE SPIKE\|TODO:" app/src/
```

## cat \<\< 'EOF' \> okf/index.md

## okf\_version: "0.1" project: "mayaGuideme" display\_name: "Maya Guide Me" status: "active-prototype" updated\_at: "2026-11-25T00:50:00Z" source\_remote: "gdrive:thinkpad-dev/mayaGuideme/src"

# Maya Guide Me — OKF v0.1 Knowledge Index

**Maya Guide Me** (`mayaGuideme`) is an offline-first Android navigation, landmark capture, and grounding guidance application written in **Kotlin** and **Jetpack Compose** using Clean Architecture and MVVM principles.

This `index.md` is the progressive-disclosure entry point for AI collaborators and orchestration pipelines (such as Project Iris and Android Studio agents). Load only the concept and requirement files required for your current task.

## Quick Orientation

- **Platform:** Android (Kotlin \+ Jetpack Compose \+ Material 3 \+ CameraX)  
- **Pinned Map SDK:** OSMDroid (`org.osmdroid:osmdroid-android:6.1.20`) hosted in Compose via `AndroidView` with custom Canvas `'X'` marker (`#E91E63`)  
- **Pinned Secure Storage:** Jetpack Preferences DataStore (`androidx.datastore:datastore-preferences:1.3.0`) \+ Google Tink (`com.google.crypto.tink:tink-android:1.24.0` AEAD AES256-GCM backed by Android Keystore) — replaces deprecated `EncryptedSharedPreferences`  
- **Entry Point:** `maya.ui.MainActivity`  
- **Active Hardware Service:** `com.mayaguideme.service.camera.CameraCaptureService`  
- **Sync Bridge:** `~/projects/mayaGuideme` \<-\> `gdrive:thinkpad-dev/mayaGuideme` via `rclone` \+ Git `post-commit` hook  
- **Grounding Copy (`strings.xml`):** *"You are safe. Take your time capturing your surroundings."*

## Concept Map (Progressive Disclosure)

| Concept File | Scope | When to Load |
| :---- | :---- | :---- |
| `.okf/manifest.json` | Machine-readable inventory, file paths, module graph, and sync metadata | Cold-start ingestion by Project Iris, Android Studio, or automated indexers |
| `.okf/concepts/architecture.md` | Package structure, Clean Architecture/MVVM target state, and current vs. planned boundaries | Before creating new packages, services, repositories, or ViewModels |
| `.okf/concepts/camera-pipeline.md` | `CameraCaptureService`, `RawCaptureFrame`, CameraX lifecycle, rotation/memory handling, and planned 768px preprocessing | Any work on camera capture, image preprocessing, or landmark snapshots |
| `.okf/concepts/ui-and-permissions.md` | `MainActivity`, Compose state management, runtime `CAMERA` permission flow, floating preview card, and `FormFactorPreviews` | Any UI layout, state hoisting, or Compose preview work |
| `.okf/concepts/manifest-and-capabilities.md` | `AndroidManifest.xml` permissions (GPS, Camera, Foreground Services) and hardware feature flags | Any change involving Android permissions, background services, or device compatibility |
| `.okf/concepts/coding-rules.md` | Strict pacing rules from `gemini-code-guidence.md` (single-step builds, C\#/WPF/Prism analogies, auto-fix boundaries) | **Always load before writing or modifying code** |
| `.okf/concepts/end-of-session-procedure.md` | Mandatory closeout checklist: build/TODO audit, requirement YAML status updates, OKF index/manifest refresh, and Git/Drive push | **Always run when wrapping up a coding session** |

## Approved Requirements Map (Build & Execution Order)

| Requirement File | Scope | When to Load |
| :---- | :---- | :---- |
| `.okf/requirements/non-functional-requirements.yaml` | Cross-cutting Clean Architecture/MVVM/UDF rules, Maya brand colour palette, OSMDroid map contract, DataStore \+ Tink Keystore security & Auto Backup exclusions, panic-alert consent gate & UI-lock exemption, and self-healing standards | **Always load before writing or modifying code** |
| `.okf/requirements/first-run-consent-and-api-keys.yaml` | Mandatory first-run telemetry consent gate and optional BYOK Gemini / what3words API key setup backed by DataStore \+ Tink *(Iteration 1 client onboarding done 2026-11-25; edge/settings hooks pending)* | First-run onboarding, consent persistence, or BYOK settings work |
| `.okf/requirements/app-launch-initial-state.yaml` | Post-consent launch sequence: BYOK check, neutral OSMDroid map with hidden empty readouts, and automatic live camera capture prompt | Launch screen state, initial map state, or startup permission gating |
| `.okf/requirements/camerax-and-location-update.yaml` | `Find Me` capture flow, `100.dp x 100.dp` grayscale thumbnail (replacing prototype `140.dp x 180.dp` Card), 768px Rec. 709 high-pass grayscale pipeline, atomic GPS+image dispatch, API 29+ guarded `CellInfoNr`, OSMDroid `'X'` marker, and HTTP 429 quota dialog | Core location capture, image preprocessing, map update, or quota handling |
| `.okf/requirements/edge-service-contract.yaml` | `POST /v1/resolve-location` headers and strict JSON schemas (`ResolveLocationRequest`, `ResolveLocationResponse`, `ResolveLocationError`) | Edge client networking, DTOs, or Cloud Run `maya-spatial-edge` service |
| `.okf/requirements/emergency-contact-settings.yaml` | Material 3 modal dialog to configure trusted contact via zero-permission Contact Picker (API 37+ `ACTION_PICK_CONTACTS` with one-shot Session URI copy / API \< 37 `ACTION_PICK`) or manual entry | Emergency contact configuration UI or encrypted contact persistence |
| `.okf/requirements/send-a-friend-panic-alert.yaml` | Consent-gated emergency dispatch: silent non-cancelable multipart SMS via API 31+ `SmsManager` first, fresh GPS \+ rear/front photos, cold-start-retained `FileProvider` attachments, and sequential email \+ WhatsApp/share-sheet handoffs | Emergency panic button, dual-camera capture, `SmsManager`, or `FileProvider` handoffs |
| `.okf/project-notes.md` | Plain-Markdown summary notes for first-run BYOK keys and mandatory telemetry consent | Quick onboarding/consent reference |

## Current Milestone Snapshot (2026-11-25)

- **Implemented (Iteration 1 Complete & Verified on Pixel 8 Pro):**  
  - `SecurePreferencesStore` interface (`maya.core.storage`) and concrete `TinkDataStorePreferencesStore` using Jetpack Preferences DataStore `1.3.0` \+ Google Tink `1.24.0` (`AES256_GCM` via Android Keystore) with `AEADBadTagException` self-healing.  
  - Explicit Auto Backup & Device Transfer exclusions wired in `res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`, and `AndroidManifest.xml`.  
  - `OnboardingViewModel` (UDF `StateFlow`), non-dismissible `TelemetryConsentDialog`, and optional `ByokApiKeysDialog` (reusable for Settings).  
  - `MainActivity` wired with `ViewModelProvider.Factory` gating the temporary `MainCameraContent` (`// TODO: DISPOSABLE SPIKE`) strictly behind persisted consent.  
  - CameraX `ImageCapture` service (`CameraCaptureService`) with upright bitmap rotation and safe `ImageProxy` closing.  
- **Configured in Manifest (Not Yet Wired):** `INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `FOREGROUND_SERVICE_CAMERA`.  
- **Remaining Manifest & Resource Prerequisites for Upcoming Iterations:**  
  - `<uses-permission android:name="android.permission.SEND_SMS" />` (silent panic SMS).  
  - `<queries>` block (Android 11+ / API 30+ package visibility): `ACTION_SENDTO` (`mailto:`), `ACTION_SEND_MULTIPLE` (`image/*`), and `<package android:name="com.whatsapp" />`.  
  - `<provider>` for `androidx.core.content.FileProvider` (`${applicationId}.fileprovider`) with `res/xml/file_paths.xml` scoped to `cache-path` for ephemeral panic photos.  
- **Pinned Gradle Dependencies Still Pending:** `org.osmdroid:osmdroid-android:6.1.20`, Google Play Services Location (`play-services-location`).  
- **Next Planned Layers (Iteration 2):** Strip default Activity action bar, remove `MainCameraContent` disposable spike from `MainActivity.kt`, and build the stateless `GuidanceScreen`, `GuidanceViewModel`, neutral OSMDroid map host, and live camera capture pipeline.