package com.specimenid.app

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.specimenid.database.AllergenDao
import com.specimenid.database.AllergenEntity
import com.specimenid.database.SpecimenDatabase
import com.specimenid.vision.BarcodeAnalyzer
import com.specimenid.vision.OcrAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        provider: Provider<AllergenDao>
    ): SpecimenDatabase {
        return Room.databaseBuilder(
            context,
            SpecimenDatabase::class.java,
            "specimen_db"
        )
        .fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    provider.get().insertAll(
                        listOf(
                            AllergenEntity("milk", "dairy", 3),
                            AllergenEntity("lait", "dairy", 3),
                            AllergenEntity("حليب", "dairy", 3),
                            AllergenEntity("wheat", "gluten", 3),
                            AllergenEntity("blé", "gluten", 3),
                            AllergenEntity("قمح", "gluten", 3),
                            AllergenEntity("peanuts", "nuts", 5),
                            AllergenEntity("arachides", "nuts", 5),
                            AllergenEntity("فول سوداني", "nuts", 5),
                            AllergenEntity("soy", "soy", 2),
                            AllergenEntity("soja", "soy", 2)
                        )
                    )
                }
            }
        })
        .build()
    }

    @Provides
    fun provideAllergenDao(database: SpecimenDatabase): AllergenDao = database.allergenDao()

    @Provides
    fun provideOcrAnalyzer(): OcrAnalyzer = OcrAnalyzer()
    
    @Provides
    fun provideBarcodeAnalyzer(): BarcodeAnalyzer = BarcodeAnalyzer()
}
