package com.specimenid.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.specimenid.domain.AnalyzeIngredientStreamUseCase
import com.specimenid.domain.Hazard
import com.specimenid.vision.OcrAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface ScannerState {
    data object Scanning : ScannerState
    data object Safe : ScannerState
    data class Danger(val hazards: List<Hazard>) : ScannerState
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    val ocrAnalyzer: OcrAnalyzer,
    analyzeStream: AnalyzeIngredientStreamUseCase
) : ViewModel() {

    val uiState: StateFlow<ScannerState> = analyzeStream(ocrAnalyzer.scannedTextFlow)
        .map { hazards ->
            if (hazards.isEmpty()) ScannerState.Safe else ScannerState.Danger(hazards)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ScannerState.Scanning
        )
}
