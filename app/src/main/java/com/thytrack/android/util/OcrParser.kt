package com.thytrack.android.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 化验单 OCR（Phase 6.2）：ML Kit 中文识别，按指标关键字抽取数值并映射到 29 指标 key。
 * 关键字按"特异性优先"排列，命中即 break，避免 TG/TgAb 等子串误匹配。
 */
object OcrParser {
    private val KEYWORDS = listOf(
        "促甲状腺激素" to "tsh", "TSH" to "tsh",
        "游离T3" to "ft3", "FT3" to "ft3",
        "游离T4" to "ft4", "FT4" to "ft4",
        "总T3" to "tt3", "TT3" to "tt3",
        "总T4" to "tt4", "TT4" to "tt4",
        "甲状腺球蛋白抗体" to "tgab", "TgAb" to "tgab",
        "甲状腺球蛋白" to "tg", "TG" to "tg",
        "TG脂" to "tgLipid", "甘油三酯" to "tgLipid",
        "TPO抗体" to "tpoab", "过氧化物酶抗体" to "tpoab", "TPOAb" to "tpoab",
        "空腹血糖" to "fpg", "FPG" to "fpg",
        "餐后" to "twoHpg", "2hPG" to "twoHpg",
        "糖化" to "hba1c", "HbA1c" to "hba1c",
        "总胆固醇" to "tc", "TC" to "tc",
        "HDL" to "hdl", "HDL-C" to "hdl",
        "LDL" to "ldl", "LDL-C" to "ldl",
        "谷丙" to "alt", "ALT" to "alt",
        "谷草" to "ast", "AST" to "ast",
        "胆红素" to "tbil", "TBIL" to "tbil",
        "肌酐" to "cr", "Cr" to "cr",
        "尿素氮" to "bun", "BUN" to "bun",
        "尿酸" to "ua", "UA" to "ua",
        "钙" to "calcium", "Ca" to "calcium",
        "PTH" to "pth",
        "维生素D" to "vitaminD", "VitD" to "vitaminD",
    )

    suspend fun parseImage(context: Context, uri: Uri): Map<String, Double> =
        suspendCancellableCoroutine { cont ->
            val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            cont.invokeOnCancellation { recognizer.close() }
            val bitmap = runCatching {
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
            if (bitmap == null) {
                cont.resume(emptyMap())
                recognizer.close()
                return@suspendCancellableCoroutine
            }
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    cont.resume(extract(visionText))
                    recognizer.close()
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                    recognizer.close()
                }
        }

    private fun extract(visionText: Text): Map<String, Double> {
        val result = mutableMapOf<String, Double>()
        val num = Regex("""-?\d+(\.\d+)?""")
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val text = line.text
                for ((kw, key) in KEYWORDS) {
                    if (text.contains(kw, ignoreCase = true)) {
                        val v = num.find(text)?.value?.toDoubleOrNull()
                        if (v != null) { result[key] = v; break }
                    }
                }
            }
        }
        return result
    }
}
