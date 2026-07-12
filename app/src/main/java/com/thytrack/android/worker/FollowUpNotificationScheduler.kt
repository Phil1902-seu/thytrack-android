package com.thytrack.android.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** 复诊提醒调度（Phase 4）：每日周期任务，开关控制启停。 */
object FollowUpNotificationScheduler {
    private const val NAME = "thytrack_followup"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<FollowUpWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(NAME)
    }
}
