---
okf_version: "0.1"
concept: "coding-rules"
project: "mayaGuideme"
source_files:
  - "gemini-code-guidence.md"
updated_at: "2026-11-21T00:35:00Z"
---

# Project Rules & Coding Pacing (Mandatory Contract)

Distilled directly from `gemini-code-guidence.md` in the repository root. Every AI collaborator touching `mayaGuideme` must obey these constraints:

## Dual Objective
1. Build the **Maya, guide me** Android application.
2. Teach the developer Kotlin, Jetpack Compose, and Android architecture step by step.

## Strict Pacing Constraints
- **No multi-file scaffolds or jumping ahead:** Never generate complete features, services, repositories, or UI wiring in one go.
- **One small step at a time:** Write or modify only ONE small, isolated component or function per turn.
- **Explain before/as you build:** Briefly explain the Kotlin/Android concepts involved (mapping to **C# / WPF / Prism / XAML** patterns where helpful) and **stop and wait for explicit confirmation** before moving to the next layer.
- **No unrequested UI wiring:** Do not hook new services or repositories into Compose screens unless explicitly asked in the current step.
- **Respect separation of concerns:** Keep mock/fake repositories strictly decoupled from real hardware/edge implementations (GPS, CameraX, sensors).

## Auto-Fix Permissions (Do Without Waiting)
- Resolve Gradle sync errors, dependency conflicts, compiler errors, and broken build configurations automatically.
- Resolve ADB/device connection errors, manifest/signing/packaging problems, and emulator/device installation failures automatically.
- Fix missing imports, package declarations, and broken references after file moves/deletions.
- Clean up unused imports or minor syntax/lint errors when asked.
