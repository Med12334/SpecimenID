package com.specimenid.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AllergenDao {
    @Query("""
        SELECT * FROM allergen_dictionary 
        WHERE allergen_dictionary MATCH :scannedOcrText 
        AND parentAllergen IN (:userActiveAllergies)
    """)
    fun detectToxinsInText(scannedOcrText: String, userActiveAllergies: List<String>): Flow<List<AllergenEntity>>
}
