package com.thytrack.android.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.runBlocking

@Database(
    entities = [LabRecordEntity::class, MedicationEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun labRecordDao(): LabRecordDao
    abstract fun medicationDao(): MedicationDao

    companion object {
        const val DATABASE_NAME = "thytrack.db"

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(DatabaseCallback())
                .build()
    }
}

/**
 * 启动回调：对齐 Flutter 版 RecordRepository.migrateHospitalNames / dedupeRecords。
 * - 去除医院名前缀的 '#'
 * - 按「日期(天) + 医院 + 指标签名」去重
 */
private class DatabaseCallback : RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        runBlocking {
            // 去 '#' 前缀（轻量，直接 SQL）
            db.execSQL("UPDATE records SET hospital = substr(hospital, 2) WHERE hospital LIKE '#%'")
        }
    }
}
