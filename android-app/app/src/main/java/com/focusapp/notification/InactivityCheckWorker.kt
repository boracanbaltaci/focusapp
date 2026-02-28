package com.focusapp.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.clockera.R

class InactivityCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val WORK_NAME = "inactivity_check_worker"
        private const val NOTIFICATION_ID = 1001
        private const val FIVE_DAYS_MS = 5L * 24 * 60 * 60 * 1000
    }

    override fun doWork(): Result {
        val prefs = context.getSharedPreferences("focus_app_settings", Context.MODE_PRIVATE)
        val lastOpenTime = prefs.getLong("last_app_open_timestamp", System.currentTimeMillis())
        val elapsed = System.currentTimeMillis() - lastOpenTime

        if (elapsed >= FIVE_DAYS_MS) {
            NotificationHelper.createNotificationChannel(context)
            NotificationHelper.showNotification(
                context = context,
                title = context.getString(R.string.notif_inactivity_title),
                body = context.getString(R.string.notif_inactivity_body),
                notificationId = NOTIFICATION_ID
            )
        }

        return Result.success()
    }
}
