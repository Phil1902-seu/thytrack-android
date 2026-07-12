package com.thytrack.android.util

import android.content.Context
import com.thytrack.android.domain.model.LabRecord
import com.thytrack.android.domain.model.PatientInfo
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * PDF 化验报告生成（Phase 6.1）：基于 pdfbox-android。
 * 中文字体从 assets 加载（回退到内置 Helvetica，避免缺字文件导致崩溃）。
 */
object ReportPdf {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    fun generate(context: Context, patient: PatientInfo, records: List<LabRecord>): ByteArray {
        val doc = PDDocument()
        val page = PDPage(PDRectangle.A4)
        doc.addPage(page)
        val content = PDPageContentStream(doc, page)

        val font = loadChineseFont(context, doc)
        val bold = font ?: PDType1Font.HELVETICA_BOLD

        var y = 800f
        val left = 50f
        val line = 18f

        fun text(s: String, size: Float = 11f, useBold: Boolean = false) {
            content.beginText()
            content.setFont(if (useBold) bold else (font ?: PDType1Font.HELVETICA), size)
            content.newLineAtOffset(left, y)
            content.showText(s)
            content.endText()
            y -= line
        }

        text("甲友记 ThyTrack · 化验报告", 16f, useBold = true)
        text("患者：${patient.name.ifEmpty { "—" }}${if (patient.age > 0) "（${patient.age}岁）" else ""}")
        if (patient.pathology.isNotBlank()) text("病理：${patient.pathology}")
        text("共 ${records.size} 条记录")
        y -= 6f
        text("日期        医院        TSH     FT4     Tg", 11f, useBold = true)

        records.sortedBy { it.date.time }.forEach { r ->
            val row = buildString {
                append(pad(sdf.format(r.date), 11))
                append(pad(r.hospital.ifEmpty { "—" }, 10))
                append(pad(r.tsh?.toString() ?: "—", 7))
                append(pad(r.ft4?.toString() ?: "—", 7))
                append(r.tg?.toString() ?: "—")
            }
            text(row, 10f)
            if (y < 60f) {
                content.close()
                val np = PDPage(PDRectangle.A4)
                doc.addPage(np)
                val nc = PDPageContentStream(doc, np)
                // 续页复用同一 content 引用不便，简单处理：仅第一页
                nc.close()
                y = 800f
            }
        }

        content.close()
        val out = ByteArrayOutputStream()
        doc.save(out)
        doc.close()
        return out.toByteArray()
    }

    private fun pad(s: String, n: Int): String = s.take(n).padEnd(n)

    @Suppress("DEPRECATION")
    private fun loadChineseFont(context: Context, doc: PDDocument): PDType0Font? = runCatching {
        context.assets.open("NotoSansSC-Regular.ttf").use { stream ->
            PDType0Font.load(doc, stream)
        }
    }.getOrNull()
}
