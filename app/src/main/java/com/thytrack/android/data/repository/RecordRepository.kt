package com.thytrack.android.data.repository

import com.thytrack.android.data.local.room.AppDatabase
import com.thytrack.android.data.local.room.toDomain
import com.thytrack.android.data.local.room.toEntity
import com.thytrack.android.domain.model.LabRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface RecordRepository {
    fun observeRecords(): Flow<List<LabRecord>>
    suspend fun getAll(): List<LabRecord>
    suspend fun getById(id: String): LabRecord?
    suspend fun insert(record: LabRecord)
    suspend fun insertAll(records: List<LabRecord>)
    suspend fun update(record: LabRecord)
    suspend fun delete(id: String)
    suspend fun deleteAll()
}

@Singleton
class RoomRecordRepository @Inject constructor(
    private val db: AppDatabase,
) : RecordRepository {
    override fun observeRecords(): Flow<List<LabRecord>> =
        db.labRecordDao().observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getAll(): List<LabRecord> =
        db.labRecordDao().getAll().map { it.toDomain() }

    override suspend fun getById(id: String): LabRecord? =
        db.labRecordDao().getById(id)?.toDomain()

    override suspend fun insert(record: LabRecord) =
        db.labRecordDao().insert(record.toEntity())

    override suspend fun insertAll(records: List<LabRecord>) =
        db.labRecordDao().insertAll(records.map { it.toEntity() })

    override suspend fun update(record: LabRecord) =
        db.labRecordDao().update(record.copy(updatedAt = java.util.Date()).toEntity())

    override suspend fun delete(id: String) =
        db.labRecordDao().deleteById(id)

    override suspend fun deleteAll() =
        db.labRecordDao().deleteAll()
}
