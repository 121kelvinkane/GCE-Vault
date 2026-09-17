package com.gcevault.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gcevault.app.MainActivity
import com.gcevault.app.R

class ReminderReceiver : BroadcastReceiver() {
    
    companion object {
        const val CHANNEL_ID = "study_reminder_channel"
        const val NOTIFICATION_ID = 1001
        
        // ✅ Rotating motivational messages
        val MESSAGES = listOf(
            "🔥 Time for your Daily 10! Keep that streak alive!",
            "📚 Your future self will thank you. Start studying now!",
            "⚡ Quick quiz break? 10 questions = 100 XP!",
            "🎯 You're closer to your goals than you think. Let's go!",
            "💪 Champions study a little every day. Are you ready?",
            "🌟 Don't break your streak! Tap to start your quiz.",
            "📖 Past questions are waiting. Let's crush them!"
        )
    }
    
    override fun onReceive(context: Context, intent: Intent?) {
        createNotificationChannel(context)
        showNotification(context)
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Study Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders to keep you studying"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification(context: Context) {
        // Pick a random motivational message
        val message = MESSAGES.random()
        
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Uses your app icon
            .setContentTitle("GCE Vault 📚")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}