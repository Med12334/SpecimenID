package com.specimenid.database

import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "allergen_dictionary")
data class AllergenEntity(
    val ingredientName: String,
    val parentAllergen: String,
    val severityLevel: Int
)
