package maya.ui.onboarding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import maya.ui.base.FormFactorPreviews

@Composable
fun ByokApiKeysDialog(
    initialGeminiApiKey: String? = null,
    initialWhat3wordsApiKey: String? = null,
    onSave: (geminiApiKey: String?, what3wordsApiKey: String?) -> Unit,
    onSkip: () -> Unit
) {
    var geminiKeyInput by rememberSaveable(initialGeminiApiKey) {
        mutableStateOf(initialGeminiApiKey.orEmpty())
    }
    var what3wordsKeyInput by rememberSaveable(initialWhat3wordsApiKey) {
        mutableStateOf(initialWhat3wordsApiKey.orEmpty())
    }
    var showGeminiKey by rememberSaveable { mutableStateOf(false) }
    var showWhat3wordsKey by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onSkip,
        title = {
            Text(text = "Optional API Keys")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Both keys are optional and encrypted on your device. " +
                            "Leave Gemini blank to use the built-in free tier, or leave " +
                            "what3words blank to keep three-word addresses disabled."
                )

                OutlinedTextField(
                    value = geminiKeyInput,
                    onValueChange = { geminiKeyInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Gemini API Key (Optional)") },
                    supportingText = { Text("Uses your own quota instead of the shared free limit") },
                    singleLine = true,
                    visualTransformation = if (showGeminiKey) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        TextButton(onClick = { showGeminiKey = !showGeminiKey }) {
                            Text(if (showGeminiKey) "Hide" else "Show")
                        }
                    }
                )

                OutlinedTextField(
                    value = what3wordsKeyInput,
                    onValueChange = { what3wordsKeyInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("what3words API Key (Optional)") },
                    supportingText = { Text("Enables ///word.word.word display when configured") },
                    singleLine = true,
                    visualTransformation = if (showWhat3wordsKey) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        TextButton(onClick = { showWhat3wordsKey = !showWhat3wordsKey }) {
                            Text(if (showWhat3wordsKey) "Hide" else "Show")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        geminiKeyInput.trim().ifEmpty { null },
                        what3wordsKeyInput.trim().ifEmpty { null }
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("Skip")
            }
        }
    )
}

@FormFactorPreviews
@Composable
private fun ByokApiKeysDialogPreview() {
    MaterialTheme {
        Surface {
            ByokApiKeysDialog(
                initialGeminiApiKey = "",
                initialWhat3wordsApiKey = "",
                onSave = { _, _ -> },
                onSkip = {}
            )
        }
    }
}

