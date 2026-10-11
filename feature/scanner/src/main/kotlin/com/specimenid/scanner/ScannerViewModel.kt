package com.specimenid.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.specimenid.domain.AnalyzeIngredientStreamUseCase
import com.specimenid.domain.Hazard
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
        // Écouteur pour le mode OCR (Texte)
        viewModelScope.launch {
            analyzeUseCase(ocrAnalyzer.scannedTextFlow).collect { hazards ->
                if (!isBarcodeMode.value) {
                    if (hazards.isNotEmpty()) _uiState.value = ScannerState.Danger(hazards)
                    else _uiState.value = ScannerState.Safe
                }
            }
        }

        // Écouteur pour le mode Code-barres (API)
        viewModelScope.launch {
            barcodeAnalyzer.scannedBarcodeFlow
                .distinctUntilChanged()
                .collect { barcode ->
                    if (isBarcodeMode.value) {
                        _uiState.value = ScannerState.Scanning
                        try {
                            // Appel réseau Retrofit
                            val response = api.getProductByBarcode(barcode)
                            val ingredients = response.product?.ingredients_text ?: ""
                            
                            // Analyse des ingrédients téléchargés
                            analyzeUseCase(flowOf(ingredients.lowercase())).collect { hazards ->
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
