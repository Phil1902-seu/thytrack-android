package com.thytrack.android.util

import com.thytrack.android.domain.model.LabRecord
import com.thytrack.android.domain.model.RecordSource
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
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
            for (rec in parser) {
                val date = runCatching { sdf.parse(rec.get("date")) }.getOrNull() ?: Date()
                var record = LabRecord(
                    id = (rec.get("id") ?: "").ifBlank { UUID.randomUUID().toString() },
                    date = date,
                    hospital = rec.get("hospital") ?: "",
                    notes = rec.get("notes") ?: "",
                    source = RecordSource.fromValue(rec.get("source")),
                )
                LabRecordFields.METRIC_KEYS.forEach { key ->
                    val v = rec.get(key)
                    if (!v.isNullOrBlank()) {
                        v.toDoubleOrNull()?.let { d -> record = LabRecordFields.set(record, key, d) }
                    }
                }
                result.add(record)
            }
        }
        return result
    }
}
