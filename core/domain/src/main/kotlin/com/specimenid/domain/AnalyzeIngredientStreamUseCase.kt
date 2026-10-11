package com.specimenid.domain

import com.specimenid.database.AllergenDao
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AnalyzeIngredientStreamUseCase @Inject constructor(
    private val allergenDao: AllergenDao
) {
    // Hardcoded for the prototype. In production, this comes from a UserRepository/DataStore.
    private val activeAllergies = listOf("dairy", "gluten", "nuts")

    operator fun invoke(ocrStream: Flow<String>): Flow<List<Hazard>> {
        return ocrStream
            .debounce(300L) // Wait 300ms after camera stops moving to query DB
            .distinctUntilChanged()
            .flatMapLatest { text ->
                if (text.length < 3) return@flatMapLatest flowOf(emptyList())
                
                allergenDao.detectToxinsInText(text, activeAllergies).map { entities ->
                    entities.map { entity ->
                        Hazard(
                            ingredient = entity.ingredientName,
                            matchedAllergen = entity.parentAllergen,
                            severity = entity.severityLevel
                        )
                    }
                }
            }
    }
}
