package com.specimenid.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AllergenEntity::class], version = 1, exportSchema = false)
abstract class SpecimenDatabase : RoomDatabase() {
    abstract fun allergenDao(): AllergenDao
}
