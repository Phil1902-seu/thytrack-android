package com.thytrack.android.util

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.charset.StandardCharsets

/**
 * WebDAV 备份（Phase 5.3）：基于 OkHttp 的 PUT/GET（WebDAV 兼容标准 HTTP 动词）。
 * 备份内容为 CSV（与导入导出同格式），落盘到用户填写的 WebDAV 文件 URL。
 */
object WebDavBackup {
    private val client = OkHttpClient.Builder().build()
    private val MEDIA = "text/csv".toMediaType()

    private fun basic(user: String, pass: String): String =
        "Basic " + Base64.encodeToString("$user:$pass".toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

    suspend fun upload(url: String, user: String, pass: String, content: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val req = Request.Builder().url(url).put(content.toRequestBody(MEDIA))
                    .header("Authorization", basic(user, pass))
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) error("HTTP ${resp.code}")
                }
            }
        }

    suspend fun download(url: String, user: String, pass: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val req = Request.Builder().url(url).get()
                    .header("Authorization", basic(user, pass))
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) error("HTTP ${resp.code}")
                    resp.body?.string() ?: ""
                }
            }
        }
}
