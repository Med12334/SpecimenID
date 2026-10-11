package com.specimenid.scanner

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                    
                    // Bascule dynamique entre l'OCR et le lecteur de codes-barres
                    val activeAnalyzer = if (isBarcodeMode) viewModel.barcodeAnalyzer else viewModel.ocrAnalyzer
                    
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(context), activeAnalyzer)
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
        
        // Bouton de basculement de mode
        Button(
            onClick = { viewModel.toggleMode() },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
        ) {
            Text(if (isBarcodeMode) "Mode: Code-barres (API)" else "Mode: Texte (OCR)")
        }

        // Zone d'alerte en bas
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(if (uiState is ScannerState.Danger) Color.Red else Color.DarkGray)
                .padding(24.dp)
        ) {
            val statusText = when (val state = uiState) {
                is ScannerState.Scanning -> if (isBarcodeMode) "Scan du code-barres..." else "Lecture du texte..."
                is ScannerState.Safe -> "Clear : Aucun allergène détecté"
                is ScannerState.Danger -> "DANGER : ${state.hazards.joinToString { it.ingredient.uppercase() }}"
            }
            Text(text = statusText, color = Color.White)
        }
    }
}
