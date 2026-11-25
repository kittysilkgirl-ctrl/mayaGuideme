package maya.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mayaguideme.service.camera.CameraCaptureService
import kotlinx.coroutines.launch
import maya.core.storage.TinkDataStorePreferencesStore
import maya.ui.onboarding.ByokApiKeysDialog
import maya.ui.onboarding.OnboardingViewModel
import maya.ui.onboarding.TelemetryConsentDialog

class MainActivity : ComponentActivity() {

    private val onboardingViewModel: OnboardingViewModel by lazy {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
                    val store = TinkDataStorePreferencesStore(applicationContext)
                    return OnboardingViewModel(store) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
        ViewModelProvider(this, factory)[OnboardingViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val onboardingState by onboardingViewModel.uiState.collectAsState()

                LaunchedEffect(onboardingState.shouldExitApp) {
                    if (onboardingState.shouldExitApp) {
                        finishAndRemoveTask()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        onboardingState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        onboardingState.consentGranted -> {
                            MainCameraContent(activity = this@MainActivity)

                            if (onboardingState.showByokSetup) {
                                ByokApiKeysDialog(
                                    initialGeminiApiKey = onboardingState.geminiApiKey,
                                    initialWhat3wordsApiKey = onboardingState.what3wordsApiKey,
                                    onSave = onboardingViewModel::saveByokKeys,
                                    onSkip = onboardingViewModel::skipByokSetup
                                )
                            }
                        }
                    }

                    if (onboardingState.showConsentDialog) {
                        TelemetryConsentDialog(
                            onAgree = onboardingViewModel::acceptConsent,
                            onDecline = onboardingViewModel::declineConsent
                        )
                    }
                }
            }
        }
    }
}
//TODO:package maya.ui.onboarding disposable spike code
@Composable
private fun MainCameraContent(activity: ComponentActivity) {
    val scope = rememberCoroutineScope()
    val cameraService = remember { CameraCaptureService(activity) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isCameraBound by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) statusMessage = "Camera permission denied"
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) return@LaunchedEffect
        val providerFuture = ProcessCameraProvider.getInstance(activity)
        providerFuture.addListener({
            try {
                val cameraProvider = providerFuture.get()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    activity,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    cameraService.imageCapture
                )
                isCameraBound = true
                statusMessage = null
            } catch (e: Exception) {
                Log.e("MainActivity", "Camera bind failed", e)
                statusMessage = "Bind failed: ${e.message}"
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        statusMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }

        Button(
            onClick = {
                if (!hasCameraPermission) {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                } else if (isCameraBound) {
                    scope.launch {
                        try {
                            val frame = cameraService.captureRawFrame()
                            capturedBitmap = frame.bitmap
                            statusMessage = null
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Capture failed", e)
                            statusMessage = "Capture failed: ${e.message}"
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text("Location")
        }

        capturedBitmap?.let { bitmap ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(width = 140.dp, height = 180.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured landmark",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}