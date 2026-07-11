package com.thytrack.android.di

import android.content.Context
import com.thytrack.android.data.local.room.AppDatabase
import com.thytrack.android.data.local.room.LabRecordDao
import com.thytrack.android.data.local.room.MedicationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.create(context)

    @Provides
    fun provideLabRecordDao(db: AppDatabase): LabRecordDao = db.labRecordDao()

    @Provides
    fun provideMedicationDao(db: AppDatabase): MedicationDao = db.medicationDao()
}
