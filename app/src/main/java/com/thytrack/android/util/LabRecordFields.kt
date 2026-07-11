package com.thytrack.android.util

import com.thytrack.android.domain.model.LabRecord

/**
 * 29 指标按 key 的读取/写入访问器（端口化自 Flutter metric_field.dart 的取字段逻辑）。
 * 用显式 when 而非反射，避免 R8 内联/混淆导致 release 运行期丢失字段。
 */
object LabRecordFields {
    val METRIC_KEYS: List<String> = listOf(
        "tsh", "ft3", "ft4", "tt3", "tt4", "tg", "tgab", "tpoab",
        "fpg", "twoHpg", "hba1c",
        "tc", "tgLipid", "hdl", "ldl",
        "alt", "ast", "tbil",
        "cr", "bun", "ua", "egfr",
        "calcium", "phosphorus", "pth", "vitaminD",
        "levothyroxineDose", "calciumDose", "calcitriolDose",
    )

    fun get(record: LabRecord, key: String): Double? = when (key) {
        "tsh" -> record.tsh
        "ft3" -> record.ft3
        "ft4" -> record.ft4
        "tt3" -> record.tt3
        "tt4" -> record.tt4
        "tg" -> record.tg
        "tgab" -> record.tgab
        "tpoab" -> record.tpoab
        "fpg" -> record.fpg
        "twoHpg" -> record.twoHpg
        "hba1c" -> record.hba1c
        "tc" -> record.tc
        "tgLipid" -> record.tgLipid
        "hdl" -> record.hdl
        "ldl" -> record.ldl
        "alt" -> record.alt
        "ast" -> record.ast
        "tbil" -> record.tbil
        "cr" -> record.cr
        "bun" -> record.bun
        "ua" -> record.ua
        "egfr" -> record.egfr
        "calcium" -> record.calcium
        "phosphorus" -> record.phosphorus
        "pth" -> record.pth
        "vitaminD" -> record.vitaminD
        "levothyroxineDose" -> record.levothyroxineDose
        "calciumDose" -> record.calciumDose
        "calcitriolDose" -> record.calcitriolDose
        else -> null
    }

    fun set(record: LabRecord, key: String, value: Double?): LabRecord = when (key) {
        "tsh" -> record.copy(tsh = value)
        "ft3" -> record.copy(ft3 = value)
        "ft4" -> record.copy(ft4 = value)
        "tt3" -> record.copy(tt3 = value)
        "tt4" -> record.copy(tt4 = value)
        "tg" -> record.copy(tg = value)
        "tgab" -> record.copy(tgab = value)
        "tpoab" -> record.copy(tpoab = value)
        "fpg" -> record.copy(fpg = value)
        "twoHpg" -> record.copy(twoHpg = value)
        "hba1c" -> record.copy(hba1c = value)
        "tc" -> record.copy(tc = value)
        "tgLipid" -> record.copy(tgLipid = value)
        "hdl" -> record.copy(hdl = value)
        "ldl" -> record.copy(ldl = value)
        "alt" -> record.copy(alt = value)
        "ast" -> record.copy(ast = value)
        "tbil" -> record.copy(tbil = value)
        "cr" -> record.copy(cr = value)
        "bun" -> record.copy(bun = value)
        "ua" -> record.copy(ua = value)
        "egfr" -> record.copy(egfr = value)
        "calcium" -> record.copy(calcium = value)
        "phosphorus" -> record.copy(phosphorus = value)
        "pth" -> record.copy(pth = value)
        "vitaminD" -> record.copy(vitaminD = value)
        "levothyroxineDose" -> record.copy(levothyroxineDose = value)
        "calciumDose" -> record.copy(calciumDose = value)
        "calcitriolDose" -> record.copy(calcitriolDose = value)
        else -> record
    }
}
