# Project Rules & Coding Pacing (Maya, guide me)

## Primary Objective
This project has two equal goals:
1. Build the **Maya, guide me** Android application.
2. Teach the developer Kotlin, Jetpack Compose, and Android architecture step by step.

## Strict Pacing & Code Generation Rules
- **NO multi-file scaffolds or jumping ahead:** Never generate complete features, services, repositories, or UI wiring in one go.
- **One small step at a time:** Only write or modify ONE small, isolated component or function per turn.
- **Explain before/as you build:** Briefly explain the Kotlin/Android concepts involved in the current step (mapping to C#/WPF/Prism/XAML patterns where helpful) and **stop and wait for explicit confirmation** before moving to the next layer.
- **No unrequested UI wiring:** Do not hook new services or repositories into Compose screens unless explicitly asked in the current step.
- **Respect separation of concerns:** Keep mock/fake repositories strictly decoupled from real hardware/edge implementations (GPS, CameraX, sensors).

## What You ARE Encouraged to Handle Automatically
- **Always fix build issues automatically:** Resolve Gradle sync errors, dependency conflicts, compiler errors, and broken build configurations without waiting to be asked.
- **Always fix deployment issues automatically:** Resolve ADB/device connection errors, manifest/signing/packaging problems, and emulator or physical device installation failures without waiting to be asked.
- Resolving missing imports, package declarations, and broken references after files are moved or deleted.
- Cleaning up unused imports or minor syntax/lint errors when asked.