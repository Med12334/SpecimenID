package com.specimenid.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AllergenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(allergens: List<AllergenEntity>)

    @Query("""
        SELECT * FROM allergen_dictionary 
        WHERE allergen_dictionary MATCH :scannedOcrText 
        AND parentAllergen IN (:userActiveAllergies)
    """)
    fun detectToxinsInText(scannedOcrText: String, userActiveAllergies: List<String>): Flow<List<AllergenEntity>>
}
