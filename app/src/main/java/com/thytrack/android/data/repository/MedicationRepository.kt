package com.thytrack.android.data.repository

import com.thytrack.android.data.local.room.AppDatabase
import com.thytrack.android.data.local.room.toDomain
import com.thytrack.android.data.local.room.toEntity
import com.thytrack.android.domain.model.MedicationChange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface MedicationRepository {
    fun observeMedications(): Flow<List<MedicationChange>>
    suspend fun getById(id: String): MedicationChange?
    suspend fun insert(med: MedicationChange)
    suspend fun update(med: MedicationChange)
    suspend fun delete(id: String)
}

@Singleton
class RoomMedicationRepository @Inject constructor(
    private val db: AppDatabase,
) : MedicationRepository {
    override fun observeMedications(): Flow<List<MedicationChange>> =
        db.medicationDao().observeAll().map { it.map { e -> e.toDomain() } }

    override suspend fun getById(id: String): MedicationChange? =
        db.medicationDao().getById(id)?.toDomain()

    override suspend fun insert(med: MedicationChange) =
        db.medicationDao().insert(med.toEntity())

    override suspend fun update(med: MedicationChange) =
        db.medicationDao().update(med.copy(updatedAt = java.util.Date()).toEntity())

    override suspend fun delete(id: String) =
        db.medicationDao().deleteById(id)
}
