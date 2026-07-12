package com.thytrack.android.util

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.thytrack.android.domain.model.LabRecord
import com.thytrack.android.domain.model.PatientInfo
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * PDF 化验报告生成（Phase 6.1）：基于 Android 框架 PdfDocument（无第三方依赖，
 * 中文字形由系统字体渲染，避免外置 AAR 在编译期不可见的问题）。
 */
object ReportPdf {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    fun generate(patient: PatientInfo, records: List<LabRecord>): ByteArray {
        val doc = PdfDocument()
        val paint = Paint().apply { textSize = 11f }
        val titlePaint = Paint().apply { textSize = 16f; isFakeBoldText = true }
        val headerPaint = Paint().apply { textSize = 11f; isFakeBoldText = true }

        var page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        var canvas = page.canvas
        var y = 50f
        val left = 40f

        fun ensureSpace(need: Float) {
            if (y + need > 820f) {
                doc.finishPage(page)
                page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 2).create())
                canvas = page.canvas
                y = 50f
            }
        }

        canvas.drawText("甲友记 ThyTrack · 化验报告", left, y, titlePaint); y += 24f
        canvas.drawText(
            "患者：${patient.name.ifEmpty { "—" }}${if (patient.age > 0) "（${patient.age}岁）" else ""}",
            left, y, paint,
        ); y += 18f
        if (patient.pathology.isNotBlank()) {
            canvas.drawText("病理：${patient.pathology}", left, y, paint); y += 18f
        }
        canvas.drawText("共 ${records.size} 条记录", left, y, paint); y += 18f
        y += 6f
        canvas.drawText("日期        医院            TSH     FT4     Tg", left, y, headerPaint); y += 18f

        records.sortedBy { it.date.time }.forEach { r ->
            ensureSpace(18f)
            val row = buildString {
                append(sdf.format(r.date).padEnd(11))
                append((r.hospital.ifEmpty { "—" }).take(8).padEnd(10))
                append((r.tsh?.toString() ?: "—").padEnd(7))
                append((r.ft4?.toString() ?: "—").padEnd(7))
                append(r.tg?.toString() ?: "—")
            }
            canvas.drawText(row, left, y, paint)
            y += 18f
        }
        ensureSpace(18f)
        y += 10f
        canvas.drawText("免责声明：本报告由个人健康管理工具生成，不能替代专业医疗诊断。", left, y, paint)

        doc.finishPage(page)
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }
}
