package com.specimenid.scanner

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isBarcodeMode by viewModel.isBarcodeMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State to hold camera frame dimensions for coordinate scaling
    var frameWidth by remember { mutableFloatStateOf(480f) }
    var frameHeight by remember { mutableFloatStateOf(640f) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx -> PreviewView(ctx) },
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    val activeAnalyzer = if (isBarcodeMode) viewModel.barcodeAnalyzer else viewModel.ocrAnalyzer
                    
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            // Intercept the stream to grab frame dimensions, then pass to analyzer
                            analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                frameWidth = imageProxy.height.toFloat() // Swapped for portrait orientation
                                frameHeight = imageProxy.width.toFloat()
                                activeAnalyzer.analyze(imageProxy)
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, 
                            CameraSelector.DEFAULT_BACK_CAMERA, 
                            preview, 
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // The AR Overlay Canvas
        if (uiState is ScannerState.Danger && !isBarcodeMode) {
            val hazards = (uiState as ScannerState.Danger).hazards
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                // Calculate the scale between the camera sensor and the UI screen
                val scaleX = canvasWidth / frameWidth
                val scaleY = canvasHeight / frameHeight

                for (hazard in hazards) {
                    hazard.boundingBox?.let { box ->
                        val left = box.left * scaleX
                        val top = box.top * scaleY
                        val right = box.right * scaleX
                        val bottom = box.bottom * scaleY

                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(left, top),
                            size = Size(right - left, bottom - top),
                            style = Stroke(width = 8f) // 8 pixels thick red bounding box
                        )
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.toggleMode() },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
        ) {
            Text(if (isBarcodeMode) "Mode: Barcode (API)" else "Mode: Text (OCR)")
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(if (uiState is ScannerState.Danger) Color.Red else Color.DarkGray)
                .padding(24.dp)
        ) {
            val statusText = when (val state = uiState) {
                is ScannerState.Scanning -> if (isBarcodeMode) "Scanning Barcode..." else "Scanning Text..."
                is ScannerState.Safe -> "Clear: No Allergens"
                is ScannerState.Danger -> "DANGER: ${state.hazards.joinToString { it.ingredient.uppercase() }}"
            }
            Text(text = statusText, color = Color.White)
        }
    }
}
