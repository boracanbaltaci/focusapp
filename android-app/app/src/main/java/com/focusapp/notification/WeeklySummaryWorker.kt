package com.focusapp.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.clockera.R
import com.focusapp.data.StatisticsRepository

class WeeklySummaryWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val WORK_NAME = "weekly_summary_worker"
        private const val NOTIFICATION_ID = 1002
    }

    override fun doWork(): Result {
        val statsRepo = StatisticsRepository(context)
        val weeklyData = statsRepo.getWeeklyData()
        val totalMinutes = statsRepo.getTotalMinutes(weeklyData)

        // Only send notification if 3+ hours (180 minutes)
        if (totalMinutes < 180) {
            return Result.success()
        }

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        val hourLabel = context.getString(R.string.notif_hours)
        val minuteLabel = context.getString(R.string.notif_minutes)

        // Build time string
        val timeString = when {
            hours > 0 && minutes > 0 -> "$hours $hourLabel $minutes $minuteLabel"
            hours > 0 -> "$hours $hourLabel"
            else -> "$minutes $minuteLabel"
        }

        // Choose emoji based on total hours
        val emoji = when {
            totalMinutes >= 600 -> "🏆"  // 10+ hours
            totalMinutes >= 300 -> "🔥"  // 5+ hours
            else -> "🎉"                 // 3+ hours
        }

        // Build notification body — always show actual time, only emoji changes
        val body = String.format(
            context.getString(R.string.notif_weekly_body),
            timeString,
            emoji
        )

        val title = context.getString(R.string.notif_weekly_title)

        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.showNotification(
            context = context,
            title = title,
            body = body,
            notificationId = NOTIFICATION_ID
        )

        return Result.success()
    }
}
