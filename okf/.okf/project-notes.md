---
okf_version: "0.1"
project: "mayaGuideme"
type: "project-notes"
updated_at: "2026-11-24T22:35:00Z"
---

# Maya Guide Me — Project Notes

## First-Run Optional Features & User-Supplied API Keys (BYOK)

- **what3words (`///word.word.word` designation):**
  - Presented as an optional opt-in during first-run onboarding (and accessible later in app settings).
  - Users can enable the feature and enter their own what3words API key.
  - If the user skips or does not supply an API key, the what3words three-word designation remains disabled and unavailable in the UI (secondary readout falls back to edge-resolved `lat_lon_display`).
  - Even when enabled, street address and what3words resolution are **best-effort** and never block or fail the core `'X'` coordinate update on the map.

- **Gemini API Key (Higher Tier / Custom Quota):**
  - Presented as an optional opt-in during first-run onboarding (and accessible later in app settings).
  - Users can enter their own Gemini API key to use their own quota/tier; if skipped or omitted, the application falls back to the built-in free-tier limit (and presents the HTTP 429 quota dialog linking to Google AI Studio if capacity is reached).

## First-Run Data & Telemetry Consent Dialog (Mandatory)

- **Mandatory First-Run Consent Gate:**
  - On first launch, before the application can be used (including all location capture and Panic / Send-a-Friend controls), display a mandatory non-dismissible consent dialog explaining that mobile network triangulation details, GPS coordinates, and location photos are sent to our service for location resolution and are **not stored**.
  - **Accept / Agree:** Persists consent locally in Jetpack Preferences DataStore encrypted with Google Tink (`tink-android` AEAD backed by Android Keystore) and unlocks the application (proceeding to optional first-run API key setup).
  - **Decline / Disagree:** Blocks access and exits/prevents the application from running until consent is granted.
