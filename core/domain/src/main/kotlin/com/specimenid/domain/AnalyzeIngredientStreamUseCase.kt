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
    private val activeAllergies = listOf("dairy", "gluten", "nuts", "soy")

    // Cette fonction gère maintenant une liste de ScannedBlock
    operator fun invoke(ocrStream: Flow<List<ScannedBlock>>): Flow<List<Hazard>> {
        val allergensFlow = allergenDao.getActiveAllergens(activeAllergies)
        
        return ocrStream
            .debounce(300L)
            .distinctUntilChanged()
            .combine(allergensFlow) { blocks, entities ->
                val hazards = mutableListOf<Hazard>()
                
                // Pour chaque bloc de texte détecté par la caméra
                for (block in blocks) {
                    // On vérifie s'il contient l'un de nos allergènes
                    val matchedEntity = entities.find { entity -> 
                        block.text.contains(entity.ingredientName) 
                    }
                    
                    if (matchedEntity != null) {
                        hazards.add(
                            Hazard(
                                ingredient = matchedEntity.ingredientName,
                                matchedAllergen = matchedEntity.parentAllergen,
                                severity = matchedEntity.severityLevel,
                                boundingBox = block.boundingBox // On conserve les coordonnées !
                            )
                        )
                    }
                }
                hazards
            }
    }
}
