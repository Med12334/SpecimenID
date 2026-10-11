package com.specimenid.vision

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.specimenid.domain.ScannedBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class OcrAnalyzer : ImageAnalysis.Analyzer {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    private val _scannedBlocksFlow = MutableSharedFlow<List<ScannedBlock>>(extraBufferCapacity = 1)
    val scannedBlocksFlow = _scannedBlocksFlow.asSharedFlow()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val blocks = visionText.textBlocks.mapNotNull { block ->
                            val sanitized = block.text.lowercase().replace(Regex("[^\\p{L}\\p{N} ]"), "")
                            if (sanitized.isNotBlank()) {
                                ScannedBlock(sanitized, block.boundingBox)
                            } else null
                        }
                        if (blocks.isNotEmpty()) {
                            _scannedBlocksFlow.emit(blocks)
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
