package com.thytrack.android.util

import com.thytrack.android.data.local.ReferenceRanges
import com.thytrack.android.domain.model.LabRecord
import com.thytrack.android.domain.model.RefRange

/** 参考范围管理器：优先自定义范围，回退默认。 */
object RefRangeManager {
    fun getRange(fieldKey: String, record: LabRecord?): RefRange {
        val custom = record?.customRefRanges?.get(fieldKey)
        if (custom != null) return custom
        return ReferenceRanges.DEFAULTS[fieldKey] ?: RefRange(0.0, 0.0)
    }

    fun isAbnormal(value: Double, fieldKey: String, record: LabRecord?): Boolean {
        val range = getRange(fieldKey, record)
        return value < range.low || value > range.high
    }

    fun getAbnormalFlag(value: Double, fieldKey: String, record: LabRecord?): String {
        val range = getRange(fieldKey, record)
        return when {
            value < range.low -> "↓"
            value > range.high -> "↑"
            else -> ""
        }
    }

    /** 合理范围警告：超出（中心 ± 5×跨度）视为可疑。 */
    fun isSanityWarning(value: Double, fieldKey: String): Boolean {
        val range = ReferenceRanges.DEFAULTS[fieldKey] ?: return false
        val span = range.high - range.low
        val center = (range.low + range.high) / 2.0
        return value < center - span * 5 || value > center + span * 5
    }
}
