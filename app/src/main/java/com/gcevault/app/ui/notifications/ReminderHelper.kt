package com.gcevault.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderHelper {
    
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_AUTO_REMINDER_SCHEDULED = "auto_reminder_scheduled"
    private const val REQUEST_CODE = 12345
    
    private fun isAutoReminderScheduled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_REMINDER_SCHEDULED, false)
    }
    
    private fun markAutoReminderScheduled(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putBoolean(KEY_AUTO_REMINDER_SCHEDULED, true)
            apply()
        }
    }
    
    fun scheduleAutomaticReminder(context: Context) {
        if (isAutoReminderScheduled(context)) return // Don't schedule twice
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Set the alarm for exactly 12:00 AM (Midnight) every day
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0) // 0 = Midnight
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If midnight today has already passed, schedule for tomorrow's midnight
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        // Fire every 24 hours at midnight
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        
        markAutoReminderScheduled(context)
    }
}