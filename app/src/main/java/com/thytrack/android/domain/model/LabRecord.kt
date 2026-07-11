package com.thytrack.android.domain.model

import java.util.Date

/**
 * 化验记录领域模型（与 Flutter 版 LabRecord 字段一一对应）。
 * 29 个指标均为 nullable：null = 本次未检测，不可与 0 混淆。
 */
data class LabRecord(
    val id: String,
    val date: Date,
    val hospital: String = "",
    val notes: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val source: RecordSource = RecordSource.MANUAL,
    val schemaVersion: Int = 1,

    // 甲状腺功能
    val tsh: Double? = null,
    val ft3: Double? = null,
    val ft4: Double? = null,
    val tt3: Double? = null,
    val tt4: Double? = null,
    val tg: Double? = null,        // 甲状腺球蛋白
    val tgab: Double? = null,
    val tpoab: Double? = null,

    // 血糖
    val fpg: Double? = null,
    val twoHpg: Double? = null,
    val hba1c: Double? = null,

    // 血脂
    val tc: Double? = null,
    val tgLipid: Double? = null,   // 甘油三酯（与 tg 永远独立）
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
    val vitaminD: Double? = null,

    // 关联用药
    val levothyroxineDose: Double? = null,
    val calciumDose: Double? = null,
    val calcitriolDose: Double? = null,

    // 自定义参考范围（仅作用本条记录）
    val customRefRanges: Map<String, RefRange> = emptyMap(),
)

enum class RecordSource(val value: String) {
    MANUAL("manual"),
    CSV("csv"),
    OCR("ocr");

    companion object {
        fun fromValue(value: String?): RecordSource =
            entries.firstOrNull { it.value == value } ?: MANUAL
    }
}
