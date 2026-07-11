package com.thytrack.android.domain.model

/** 药物枚举，name 与 Flutter 版保持一致以便 JSON/CSV 互通。 */
enum class Drug(
    val displayName: String,
) {
    LEVOTHYROXINE("优甲乐"),
    CALCIUM("钙片"),
    CALCITRIOL("骨化三醇");

    companion object {
        fun fromName(name: String?): Drug =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: LEVOTHYROXINE
    }
}
