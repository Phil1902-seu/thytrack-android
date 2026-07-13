package com.thytrack.android.util

import com.thytrack.android.domain.model.LabRecord
import com.thytrack.android.domain.model.RecordSource
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 化验记录 CSV 序列化（Phase 5.1）：与 Flutter 版字段名互通。
 * 列 = 基础字段(id,date,hospital,notes,source) + 29 个指标 key。
 */
object CsvHelper {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val HEADER = listOf("id", "date", "hospital", "notes", "source") + LabRecordFields.METRIC_KEYS

    fun toCsv(records: List<LabRecord>): String {
        val sw = StringWriter()
        CSVPrinter(sw, CSVFormat.EXCEL.builder().setHeader(*HEADER.toTypedArray()).build()).use { printer ->
            records.forEach { r ->
                val row = mutableListOf<Any?>(
                    r.id,
                    sdf.format(r.date),
                    r.hospital,
                    r.notes,
                    r.source.value,
                )
                LabRecordFields.METRIC_KEYS.forEach { key ->
                    row.add(LabRecordFields.get(r, key)?.toString() ?: "")
                }
                printer.printRecord(row)
            }
        }
        return sw.toString()
    }

    fun fromCsv(text: String): List<LabRecord> {
        val result = mutableListOf<LabRecord>()
        val format = CSVFormat.EXCEL.builder().setHeader().setSkipHeaderRecord(true).build()
        CSVParser.parse(text, format).use { parser ->
            // 归一化表头：小写并去除下划线/连字符，使 camelCase 与 snake_case 互通。
            // 例如代码侧 twoHpg / tgLipid / vitaminD / levothyroxineDose 可与导出侧
            // twohpg / tg_lipid / vitamin_d / levothyroxine_dose 正确匹配——否则 commons-csv
            // 在缺失列名时抛 IllegalArgumentException，整次导入会被上层 runCatching 吞掉成空列表。
            val headerIndex = parser.headerMap ?: emptyMap()
            val normToOriginal = headerIndex.mapKeys { (name, _) ->
                name.lowercase().replace("_", "").replace("-", "")
            }
            fun valueOf(rec: CSVRecord, rawKey: String): String? {
                val norm = rawKey.lowercase().replace("_", "").replace("-", "")
                val original = normToOriginal[norm] ?: return null
                val idx = headerIndex[original] ?: return null
                return runCatching { rec.get(idx) }.getOrNull()
            }
            for (rec in parser) {
                val date = runCatching { sdf.parse(valueOf(rec, "date")) }.getOrNull() ?: Date()
                var record = LabRecord(
                    id = (valueOf(rec, "id") ?: "").ifBlank { UUID.randomUUID().toString() },
                    date = date,
                    hospital = valueOf(rec, "hospital") ?: "",
                    notes = valueOf(rec, "notes") ?: "",
                    source = RecordSource.fromValue(valueOf(rec, "source")),
                )
                var hasMetric = false
                LabRecordFields.METRIC_KEYS.forEach { key ->
                    val v = valueOf(rec, key)
                    if (!v.isNullOrBlank()) {
                        v.toDoubleOrNull()?.let { d ->
                            record = LabRecordFields.set(record, key, d)
                            hasMetric = true
                        }
                    }
                }
                // 跳过完全为空的数据行（如文件末尾空行），避免产生无意义的空记录
                val hasDate = !valueOf(rec, "date").isNullOrBlank()
                if (hasMetric || hasDate) result.add(record)
            }
        }
        return result
    }
}
