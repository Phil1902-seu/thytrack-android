package com.thytrack.android.data.local

import com.thytrack.android.domain.model.RefRange

/**
 * 指标字段定义（端口化自 Flutter metric_field.dart），用于表单渲染、图表轴、CSV 映射。
 * key 采用 camelCase，与领域模型属性名 / Flutter MetricField.key 一致。
 */
data class MetricField(
    val key: String,
    val label: String,
    val unit: String,
    val defaultRefRange: RefRange,
)

object MetricDefinitions {
    private fun f(key: String, label: String, unit: String): MetricField =
        MetricField(key, label, unit, ReferenceRanges.DEFAULTS[key] ?: RefRange(0.0, 0.0))

    val ALL: List<MetricField> = listOf(
        // 甲状腺功能
        f("tsh", "TSH", "mIU/L"),
        f("ft3", "FT3", "pmol/L"),
        f("ft4", "FT4", "pmol/L"),
        f("tt3", "TT3", "nmol/L"),
        f("tt4", "TT4", "nmol/L"),
        f("tg", "Tg", "ng/mL"),
        f("tgab", "TgAb", "IU/mL"),
        f("tpoab", "TPOAb", "IU/mL"),
        // 血糖
        f("fpg", "空腹血糖", "mmol/L"),
        f("twoHpg", "餐后2h血糖", "mmol/L"),
        f("hba1c", "糖化血红蛋白", "%"),
        // 血脂
        f("tc", "总胆固醇", "mmol/L"),
        f("tgLipid", "甘油三酯", "mmol/L"),
        f("hdl", "高密度脂蛋白", "mmol/L"),
        f("ldl", "低密度脂蛋白", "mmol/L"),
        // 肝功能
        f("alt", "ALT", "U/L"),
        f("ast", "AST", "U/L"),
        f("tbil", "总胆红素", "μmol/L"),
        // 肾功能
        f("cr", "肌酐", "μmol/L"),
        f("bun", "尿素氮", "mmol/L"),
        f("ua", "尿酸", "μmol/L"),
        f("egfr", "eGFR", "mL/min"),
        // 电解质与骨代谢
        f("calcium", "钙", "mmol/L"),
        f("phosphorus", "磷", "mmol/L"),
        f("pth", "PTH", "pg/mL"),
        f("vitaminD", "维生素D", "ng/mL"),
        // 用药
        f("levothyroxineDose", "优甲乐", "μg"),
        f("calciumDose", "钙片", "mg"),
        f("calcitriolDose", "骨化三醇", "μg"),
    )

    val GROUPS: Map<String, List<MetricField>> = mapOf(
        "thyroid" to ALL.subList(0, 8),
        "glucose" to ALL.subList(8, 11),
        "lipid" to ALL.subList(11, 15),
        "liver" to ALL.subList(15, 18),
        "kidney" to ALL.subList(18, 22),
        "electrolyte" to ALL.subList(22, 26),
        "medication" to ALL.subList(26, 29),
    )

    val GROUP_DISPLAY_NAMES: Map<String, String> = mapOf(
        "thyroid" to "甲状腺功能",
        "glucose" to "血糖",
        "lipid" to "血脂",
        "liver" to "肝功能",
        "kidney" to "肾功能",
        "electrolyte" to "电解质与骨代谢",
        "medication" to "用药",
    )

    fun groupOf(key: String): String? =
        GROUPS.entries.firstOrNull { (_, fields) -> fields.any { it.key == key } }?.key
}
