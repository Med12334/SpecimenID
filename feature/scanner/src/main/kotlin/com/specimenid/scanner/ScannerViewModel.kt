package com.specimenid.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.specimenid.domain.AnalyzeIngredientStreamUseCase
import com.specimenid.domain.Hazard
import com.specimenid.domain.ScannedBlock
import com.specimenid.domain.network.OpenFoodFactsApi
import com.specimenid.vision.BarcodeAnalyzer
import com.specimenid.vision.OcrAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScannerState {
    object Scanning : ScannerState()
    object Safe : ScannerState()
    data class Danger(val hazards: List<Hazard>) : ScannerState()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    val ocrAnalyzer: OcrAnalyzer,
    val barcodeAnalyzer: BarcodeAnalyzer,
    private val analyzeUseCase: AnalyzeIngredientStreamUseCase,
    private val api: OpenFoodFactsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerState>(ScannerState.Scanning)
    val uiState: StateFlow<ScannerState> = _uiState.asStateFlow()
    
    val isBarcodeMode = MutableStateFlow(false)

    init {
        // Mode OCR (Maintenant branché sur scannedBlocksFlow)
        viewModelScope.launch {
            analyzeUseCase(ocrAnalyzer.scannedBlocksFlow).collect { hazards ->
                if (!isBarcodeMode.value) {
                    if (hazards.isNotEmpty()) _uiState.value = ScannerState.Danger(hazards)
                    else _uiState.value = ScannerState.Safe
                }
            }
        }

        // Mode API (Code-barres)
        viewModelScope.launch {
            barcodeAnalyzer.scannedBarcodeFlow
                .distinctUntilChanged()
                .collect { barcode ->
                    if (isBarcodeMode.value) {
                        _uiState.value = ScannerState.Scanning
                        try {
                            val response = api.getProductByBarcode(barcode)
                            val ingredients = response.product?.ingredients_text ?: ""
                            
                            // L'API ne renvoie pas de coordonnées, on crée donc un bloc artificiel sans boundingBox
                            val simulatedBlock = listOf(ScannedBlock(ingredients.lowercase(), null))
                            
                            analyzeUseCase(flowOf(simulatedBlock)).collect { hazards ->
                                if (hazards.isNotEmpty()) _uiState.value = ScannerState.Danger(hazards)
                                else if (ingredients.isNotBlank()) _uiState.value = ScannerState.Safe
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
        }
    }
    
    fun toggleMode() {
        isBarcodeMode.value = !isBarcodeMode.value
        _uiState.value = ScannerState.Scanning
    }
}
