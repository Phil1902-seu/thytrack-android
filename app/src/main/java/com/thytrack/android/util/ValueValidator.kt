package com.thytrack.android.util

import com.thytrack.android.data.local.ReferenceRanges
import com.thytrack.android.domain.model.RefRange

/** 数值合理性校验（端口化自 Flutter value_validator.dart）。 */
object ValueValidator {
    private val sanityCheck: Map<String, Pair<Double, Double>> = mapOf(
        "tsh" to (0.01 to 100.0), "ft3" to (1.0 to 50.0), "ft4" to (1.0 to 100.0),
        "tt3" to (0.1 to 10.0), "tt4" to (10.0 to 400.0), "tg" to (0.0 to 500.0),
        "tgab" to (0.0 to 1000.0), "tpoab" to (0.0 to 1000.0),
        "fpg" to (1.0 to 30.0), "twoHpg" to (1.0 to 40.0), "hba1c" to (3.0 to 15.0),
        "tc" to (1.0 to 15.0), "tgLipid" to (0.1 to 20.0), "hdl" to (0.1 to 5.0),
        "ldl" to (0.1 to 10.0), "alt" to (1.0 to 500.0), "ast" to (1.0 to 500.0),
        "tbil" to (1.0 to 200.0), "cr" to (10.0 to 500.0), "bun" to (0.5 to 30.0),
        "ua" to (50.0 to 800.0), "egfr" to (1.0 to 200.0), "calcium" to (1.0 to 5.0),
        "phosphorus" to (0.1 to 3.0), "pth" to (1.0 to 500.0), "vitaminD" to (1.0 to 200.0),
    )

    /** 校验一组解析值，返回警告列表。 */
    fun validate(parsed: Map<String, Double>): List<String> =
        parsed.mapNotNull { (key, value) ->
            val range = sanityCheck[key] ?: return@mapNotNull null
            if (value < range.first || value > range.second) {
                "$key: 值 $value 异常，请检查"
            } else null
        }

    fun isAbnormal(value: Double, field: String, custom: Map<String, RefRange>): Boolean {
        val range = custom[field] ?: (ReferenceRanges.DEFAULTS[field] ?: RefRange(0.0, 0.0))
        return value < range.low || value > range.high
    }

    fun abnormalFlag(value: Double, field: String, custom: Map<String, RefRange>): String {
        val range = custom[field] ?: (ReferenceRanges.DEFAULTS[field] ?: RefRange(0.0, 0.0))
        return when {
            value < range.low -> "↓"
            value > range.high -> "↑"
            else -> ""
        }
    }
}
