package com.thytrack.android.di

import com.thytrack.android.data.repository.MedicationRepository
import com.thytrack.android.data.repository.RoomMedicationRepository
import com.thytrack.android.data.repository.RoomRecordRepository
import com.thytrack.android.data.repository.RecordRepository
import com.thytrack.android.data.repository.SettingsRepository
import com.thytrack.android.data.repository.DataStoreSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecordRepository(impl: RoomRecordRepository): RecordRepository

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(impl: RoomMedicationRepository): MedicationRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository
}
