package com.thytrack.android.util

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 全局未捕获异常捕获器（诊断用）。
 * 崩溃时把完整栈写入 filesDir/crash_log.txt，下次启动由 CrashDialog 弹窗展示，
 * 便于在无法连接 adb 的设备上直接拿到崩溃栈。
 */
object CrashReporter {
    private const val FILE_NAME = "crash_log.txt"

    fun install(context: Context) {
        val original = Thread.getDefaultUncaughtExceptionHandler()
        val target = File(context.filesDir, FILE_NAME)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val sw = StringWriter()
                sw.appendLine(
                    "ThyTrack crash @ ${
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                    }",
                )
                sw.appendLine("SDK=${Build.VERSION.SDK_INT} brand=${Build.BRAND} model=${Build.MODEL}")
                throwable.printStackTrace(PrintWriter(sw))
                target.writeText(sw.toString())
            }
            // 仍交给原 handler 终止进程，确保下次启动能读到日志
            original?.uncaughtException(thread, throwable)
        }
    }

    /** 读取并消费崩溃日志；无则返回 null。 */
    fun consume(context: Context): String? {
        val target = File(context.filesDir, FILE_NAME)
        if (!target.exists()) return null
        val text = runCatching { target.readText() }.getOrNull() ?: return null
        runCatching { target.delete() }
        return text
    }
}
