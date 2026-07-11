package com.thytrack.android.data.local.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.thytrack.android.domain.model.RefRange

/**
 * 化验记录 Room 实体（宽表，与 Flutter 版字段一一对应）。
 * 指标列全部 nullable：null = 本次未检测。
 * 列名（snake_case）与 CSV 表头对齐，便于显式映射兼容。
 */
@Entity(tableName = "records")
@TypeConverters(Converters::class)
data class LabRecordEntity(
    @PrimaryKey val id: String,
    val date: Long,                      // epoch millis
    val hospital: String = "",
    val notes: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    val source: String = "manual",
    @ColumnInfo(name = "schema_version") val schemaVersion: Int = 1,

    // 甲状腺功能
    val tsh: Double? = null,
    val ft3: Double? = null,
    val ft4: Double? = null,
    val tt3: Double? = null,
    val tt4: Double? = null,
    val tg: Double? = null,
    val tgab: Double? = null,
    val tpoab: Double? = null,

    // 血糖
    val fpg: Double? = null,
    @ColumnInfo(name = "two_hpg") val twoHpg: Double? = null,
    val hba1c: Double? = null,

    // 血脂
    val tc: Double? = null,
    @ColumnInfo(name = "tg_lipid") val tgLipid: Double? = null,
    val hdl: Double? = null,
    val ldl: Double? = null,

    // 肝功能
    val alt: Double? = null,
    val ast: Double? = null,
    val tbil: Double? = null,

    // 肾功能
    val cr: Double? = null,
    val bun: Double? = null,
    val ua: Double? = null,
    val egfr: Double? = null,

    // 电解质与骨代谢
    val calcium: Double? = null,
    val phosphorus: Double? = null,
    val pth: Double? = null,
    @ColumnInfo(name = "vitamin_d") val vitaminD: Double? = null,

    // 关联用药
    @ColumnInfo(name = "levothyroxine_dose") val levothyroxineDose: Double? = null,
    @ColumnInfo(name = "calcium_dose") val calciumDose: Double? = null,
    @ColumnInfo(name = "calcitriol_dose") val calcitriolDose: Double? = null,

    @ColumnInfo(name = "custom_ref_ranges") val customRefRanges: Map<String, RefRange>? = null,
)
