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
import androidx.compose.ui.tooling.preview.Preview
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

    AlertDialog(
        onDismissRequest = onSkip,
        title = { Text(text = "Optional API Keys") },
        text = {
            ByokApiKeysFormContent(
                geminiKeyInput = geminiKeyInput,
                onGeminiKeyChange = { geminiKeyInput = it },
                what3wordsKeyInput = what3wordsKeyInput,
                onWhat3wordsKeyChange = { what3wordsKeyInput = it }
            )
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

@Composable
private fun ByokApiKeysFormContent(
    geminiKeyInput: String,
    onGeminiKeyChange: (String) -> Unit,
    what3wordsKeyInput: String,
    onWhat3wordsKeyChange: (String) -> Unit
) {
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

        ApiKeyInputField(
            value = geminiKeyInput,
            onValueChange = onGeminiKeyChange,
            label = "Gemini API Key (Optional)",
            supportingText = "Uses your own quota instead of the shared free limit",
            imeAction = ImeAction.Next
        )

        ApiKeyInputField(
            value = what3wordsKeyInput,
            onValueChange = onWhat3wordsKeyChange,
            label = "what3words API Key (Optional)",
            supportingText = "Enables ///word.word.word display when configured",
            imeAction = ImeAction.Done
        )
    }
}

@Composable
private fun ApiKeyInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    supportingText: String,
    imeAction: ImeAction
) {
    var isKeyVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        supportingText = { Text(supportingText) },
        singleLine = true,
        visualTransformation = if (isKeyVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false,
            imeAction = imeAction
        ),
        trailingIcon = {
            TextButton(onClick = { isKeyVisible = !isKeyVisible }) {
                Text(if (isKeyVisible) "Hide" else "Show")
            }
        }
    )
}

@Preview
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
