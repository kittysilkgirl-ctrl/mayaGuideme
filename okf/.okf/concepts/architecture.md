&nbsp;

## Updated Package & OKF Path Notes (2026-11-28)

Recent structural changes introduce the *maya.ui.onboarding* package containing *OnboardingViewModel*, as well as *maya.core.storage* housing *SecurePreferencesStore* and *TinkDataStorePreferencesStore*.

Additionally, the active *2026-11-25* OKF bundle now resides under *src/okf/*, alongside the older *2026-11-21* top-level *okf/* directory.

---

## okf\_version: "0.1" concept: "architecture" project: "mayaGuideme" updated\_at: "2026-11-21T00:35:00Z"

# Architecture & Package Topology

## Current Source Tree (`app/src/main/`)

```
app/src/main/
├── AndroidManifest.xml
├── keepRules/
│   └── rules.keep
├── java/
│   ├── maya/
│   │   └── ui/
│   │       ├── MainActivity.kt
│   │       └── base/
│   │           └── FormFactorPreviews.kt
│   └── com/
│       └── mayaguideme/
│           └── service/
│               └── camera/
│                   └── CameraCaptureService.kt
└── res/
    ├── drawable/
    │   ├── ic_launcher_background.xml
    │   └── ic_launcher_foreground.xml
    ├── mipmap-*/ (launcher icons)
    ├── values/
    │   ├── colors.xml
    │   └── strings.xml
    └── xml/
        ├── backup_rules.xml
        └── data_extraction_rules.xml
```

## Architectural Notes & Boundaries

1. **Dual Package Namespaces (Active Observation):**  
   - UI layer currently resides under `maya.ui` and `maya.ui.base`.  
   - Service layer resides under `com.mayaguideme.service.camera`.  
   - Keep imports explicit and avoid moving packages unless explicitly requested (resolving imports automatically if files are relocated).  
2. **Separation of Concerns:**  
   - Mock/fake repositories must remain strictly decoupled from real hardware/edge implementations (GPS, CameraX, sensors).  
   - `MainActivity.kt` currently instantiates `CameraCaptureService(this@MainActivity)` directly via `remember` as a working prototype; future refactoring will introduce ViewModel \+ repository boundaries one small step at a time.  
3. **Sync & Security Boundary:**  
   - Local Git repository (`~/projects/mayaGuideme`) is the single source of truth for source code.  
   - `rclone` filter (`~/.config/rclone/maya-filter.txt`) strips `.git/**`, `.gradle/**`, `build/**`, `local.properties`, `google-services.json`, `.env*`, and keystores before syncing to `gdrive:thinkpad-dev/mayaGuideme/src`.