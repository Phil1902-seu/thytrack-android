package com.thytrack.android.util

import android.content.Context
import com.thytrack.android.BuildConfig
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 全局未捕获异常捕获器。
 * - 将堆栈写入 filesDir/crash_log.txt，便于无 adb 的真机抓取。
 * - trace 头携带 versionName / versionCode / buildType，用于确认用户实际运行的构建。
 * - release 构建仅静默写文件；debug 构建由 MainActivity 弹窗展示并可一键复制。
 */
object CrashReporter {
    private const val FILE = "crash_log.txt"

    fun install(context: Context) {
        val default = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                sw.append("ThyTrack crash @ ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
                sw.append(
                    "version=${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE}) " +
                        "buildType=${BuildConfig.BUILD_TYPE}\n",
                )
                throwable.printStackTrace(PrintWriter(sw))
                context.openFileOutput(FILE, Context.MODE_PRIVATE).use {
                    it.write(sw.toString().toByteArray())
                }
            } catch (_: Throwable) {
                // 抓取失败不影响原有异常传播
            }
            default?.uncaughtException(thread, throwable)
        }
    }

    fun consume(context: Context): String? {
        val file = java.io.File(context.filesDir, FILE)
        if (!file.exists()) return null
        val text = file.readText()
        file.delete()
        return text.ifBlank { null }
    }
}
