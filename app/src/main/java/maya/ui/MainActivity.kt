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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
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
import com.mayaguideme.service.camera.CameraCaptureService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val scope = rememberCoroutineScope()
                val cameraService = remember { CameraCaptureService(this@MainActivity) }

                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
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
                    val providerFuture = ProcessCameraProvider.getInstance(this@MainActivity)
                    providerFuture.addListener({
                        try {
                            val cameraProvider = providerFuture.get()
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                this@MainActivity,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                cameraService.imageCapture
                            )
                            isCameraBound = true
                            statusMessage = null
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Camera bind failed", e)
                            statusMessage = "Bind failed: ${e.message}"
                        }
                    }, ContextCompat.getMainExecutor(this@MainActivity))
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
            }
        }
    }
}