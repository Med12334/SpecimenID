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

    @Query("SELECT * FROM allergen_dictionary WHERE parentAllergen IN (:userActiveAllergies)")
    fun getActiveAllergens(userActiveAllergies: List<String>): Flow<List<AllergenEntity>>
}
