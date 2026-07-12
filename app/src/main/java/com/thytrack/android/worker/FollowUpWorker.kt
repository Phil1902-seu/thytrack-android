package com.thytrack.android.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

/** 每日复诊提醒 Worker（Phase 4）：无需依赖注入，仅展示通用提醒。 */
class FollowUpWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        postReminder(applicationContext)
        return Result.success()
    }

    private fun postReminder(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channelId = CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "复诊提醒",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle("复诊提醒")
            .setContentText("该按计划复查啦，请关注近期的化验与用药记录。")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(NOTIFY_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "thytrack_followup"
        const val NOTIFY_ID = 1001
    }
}
