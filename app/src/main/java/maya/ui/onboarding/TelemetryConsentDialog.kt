package maya.ui.onboarding

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import maya.ui.base.FormFactorPreviews

/**
 * Mandatory, non-dismissible first-run telemetry consent dialog.
 *
 * Per requirements (`first-run-consent-and-api-keys.yaml`):
 * - Explains that mobile network triangulation details, GPS coordinates, and location photos
 *   are sent to the location service for location resolution.
 * - Explicitly states that this data will not be stored.
 * - Cannot be bypassed via the system Back button or outside taps.
 * - Provides explicit actions to Agree or Decline.
 */
@Composable
fun TelemetryConsentDialog(
    onAgree: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            // Mandatory consent gate: outside taps or back presses must not dismiss or bypass.
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = {
            Text(
                text = "Location & Telemetry Consent",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "To resolve your location and provide guidance, Maya Guide Me sends mobile " +
                    "network triangulation details, GPS coordinates, and location photos to our " +
                    "location resolution service.\n\n" +
                    "This data is processed ephemerally and will not be stored. " +
                    "Do you agree to allow Maya Guide Me to transmit this data when resolving your location?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onAgree) {
                Text("Agree")
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text("Decline")
            }
        }
    )
}

@FormFactorPreviews
@Composable
private fun TelemetryConsentDialogPreview() {
    MaterialTheme {
        Surface {
            TelemetryConsentDialog(
                onAgree = {},
                onDecline = {}
            )
        }
    }
}
