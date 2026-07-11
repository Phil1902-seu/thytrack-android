package com.thytrack.android.domain.model

/** 参考范围：[low, high]，单位与指标一致。 */
data class RefRange(
    val low: Double,
    val high: Double,
) {
    fun contains(value: Double): Boolean = value >= low && value <= high
}
