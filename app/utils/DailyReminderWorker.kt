package com.gcevault.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gcevault.app.MainActivity // Import your main activity
import com.gcevault.app.R // Import your resources

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        showWhatsAppStyleNotification()
        return Result.success()
    }

    private fun showWhatsAppStyleNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_gce_whatsapp_channel"

        // 1. Create Notification Channel (High Importance for WhatsApp-style popup)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily GCE Reminders",
                NotificationManager.IMPORTANCE_HIGH // Makes it pop up like a message
            ).apply {
                description = "Daily practice questions notification"
                // Optional: Set a custom sound here if you have one
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Create an Intent to open the app when tapped
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Build the WhatsApp-Style Notification
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            // Small icon (Chat bubble style)
            .setSmallIcon(android.R.drawable.ic_dialog_email) 
            // Large icon (Like a contact's profile picture)
            .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, R.mipmap.ic_launcher)) 
            // Title (Like the contact name)
            .setContentTitle("GCE Vault Daily Quiz ") 
            // Message (Like the chat text)
            .setContentText("Your 10 questions are ready! Tap to start learning. 📝") 
            // Expandable text (When they pull down the notification)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Your 10 questions are ready! Tap to start learning and boost your streak. Don't break your chain! 🔥"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFF25D366.toInt()) // WhatsApp Green Color!
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Opens app when tapped
            .build()

        // 4. Show the notification
        notificationManager.notify(1001, notification)
    }
}