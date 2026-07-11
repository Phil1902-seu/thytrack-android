package com.thytrack.android.data.local

import com.thytrack.android.domain.model.RefRange

/**
 * 全局默认参考范围（逐值对齐 Flutter 版 reference_ranges.dart）。
 * 键名采用 camelCase，与领域模型属性名、Flutter MetricField.key 一致，
 * 以保证 customRefRanges 的 JSON 与原版互通。
 * 单位与 Flutter 完全一致，禁止随意修改以免破坏异常判定。
 */
object ReferenceRanges {
    val DEFAULTS: Map<String, RefRange> = mapOf(
        "tsh" to RefRange(0.27, 4.2),
        "ft3" to RefRange(3.1, 6.8),
        "ft4" to RefRange(12.0, 22.0),
        "tt3" to RefRange(0.9, 2.5),
        "tt4" to RefRange(58.0, 161.0),
        "tg" to RefRange(0.0, 0.2),
        "tgab" to RefRange(0.0, 115.0),
        "tpoab" to RefRange(0.0, 34.0),
        "fpg" to RefRange(3.9, 6.1),
        "twoHpg" to RefRange(0.0, 7.8),
        "hba1c" to RefRange(4.0, 6.0),
        "tc" to RefRange(0.0, 5.18),
        "tgLipid" to RefRange(0.0, 1.70),
        "hdl" to RefRange(1.04, 99.0),
        "ldl" to RefRange(0.0, 3.37),
        "alt" to RefRange(7.0, 40.0),
        "ast" to RefRange(13.0, 35.0),
        "tbil" to RefRange(5.1, 28.0),
        "cr" to RefRange(44.0, 133.0),
        "bun" to RefRange(2.9, 8.2),
        "ua" to RefRange(149.0, 416.0),
        "egfr" to RefRange(90.0, 999.0),
        "calcium" to RefRange(2.15, 2.55),
        "phosphorus" to RefRange(0.81, 1.45),
        "pth" to RefRange(15.0, 65.0),
        "vitaminD" to RefRange(30.0, 100.0),
        // 用药剂量无临床参考范围
        "levothyroxineDose" to RefRange(0.0, 0.0),
        "calciumDose" to RefRange(0.0, 0.0),
        "calcitriolDose" to RefRange(0.0, 0.0),
    )
}
