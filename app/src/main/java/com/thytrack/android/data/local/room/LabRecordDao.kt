package com.thytrack.android.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LabRecordDao {
    @Query("SELECT * FROM records ORDER BY date DESC")
    fun observeAll(): Flow<List<LabRecordEntity>>

    @Query("SELECT * FROM records ORDER BY date DESC")
    suspend fun getAll(): List<LabRecordEntity>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: String): LabRecordEntity?

    @Query("SELECT * FROM records WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun getBetween(start: Long, end: Long): List<LabRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: LabRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<LabRecordEntity>)

    @Update
    suspend fun update(record: LabRecordEntity)

    @Delete
    suspend fun delete(record: LabRecordEntity)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM records")
    suspend fun deleteAll()
}
