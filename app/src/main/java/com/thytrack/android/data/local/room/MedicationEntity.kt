package com.thytrack.android.data.local.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/** 用药变更 Room 实体。 */
@Entity(tableName = "medications")
@TypeConverters(Converters::class)
data class MedicationEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val drug: String,             // Drug.name
    @ColumnInfo(name = "old_dose") val oldDose: Double,
    @ColumnInfo(name = "new_dose") val newDose: Double,
    val reason: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "record_id") val recordId: String? = null,
)
