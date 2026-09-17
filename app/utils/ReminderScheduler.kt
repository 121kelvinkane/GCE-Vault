package com.gcevault.app.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    fun scheduleDailyReminder(context: Context) {
        // ✅ NEW: Add Network Constraint (Only runs when online)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Waits for internet!
            .build()

        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.HOURS) // First notification 1 hour after install
            .setConstraints(constraints) // Apply the online-only rule
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailyGceReminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("DailyGceReminder")
    }
}