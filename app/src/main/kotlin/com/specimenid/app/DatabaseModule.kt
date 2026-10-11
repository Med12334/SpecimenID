package com.specimenid.app

import android.content.Context
import androidx.room.Room
import com.specimenid.database.AllergenDao
import com.specimenid.database.SpecimenDatabase
import com.specimenid.vision.OcrAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SpecimenDatabase {
        return Room.databaseBuilder(
            context,
            SpecimenDatabase::class.java,
            "specimen_db"
        ).build()
    }

    @Provides
    fun provideAllergenDao(database: SpecimenDatabase): AllergenDao = database.allergenDao()

    @Provides
    fun provideOcrAnalyzer(): OcrAnalyzer = OcrAnalyzer()
}
