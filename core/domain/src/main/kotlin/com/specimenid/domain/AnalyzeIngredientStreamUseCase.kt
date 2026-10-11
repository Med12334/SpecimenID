package com.specimenid.domain

import com.specimenid.database.AllergenDao
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@OptIn(FlowPreview::class)
class AnalyzeIngredientStreamUseCase @Inject constructor(
    private val allergenDao: AllergenDao
) {
    // J'ai ajouté "soy" pour que le soja soit détecté
    private val activeAllergies = listOf("dairy", "gluten", "nuts", "soy")

    operator fun invoke(ocrStream: Flow<String>): Flow<List<Hazard>> {
        val allergensFlow = allergenDao.getActiveAllergens(activeAllergies)
        
        return ocrStream
            .debounce(300L)
            .distinctUntilChanged()
            .combine(allergensFlow) { text, entities ->
                if (text.length < 3) return@combine emptyList()
                
                entities.filter { entity ->
                    text.contains(entity.ingredientName)
                }.map { entity ->
                    Hazard(
                        ingredient = entity.ingredientName,
                        matchedAllergen = entity.parentAllergen,
                        severity = entity.severityLevel
                    )
                }
            }
    }
}
