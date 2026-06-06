package com.example.engflash.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.engflash.MainActivity
import com.example.engflash.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent khi nhấn vào thông báo (mở app)
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Builder cho thông báo
        val notification = NotificationCompat.Builder(context, "engflash_reminder_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground) 
            .setContentTitle("Đã đến giờ học tiếng Anh! 📚")
            .setContentText("Hãy dành 10 phút để học từ vựng mới và duy trì chuỗi ngày học của bạn nhé!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1001, notification)

        // Reschedule for next day
        val prefs = context.getSharedPreferences("engflash_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("push_notifs_enabled", true)) {
            val hour = prefs.getInt("reminder_hour", 9)
            val minute = prefs.getInt("reminder_minute", 0)
            NotificationHelper.scheduleDailyReminder(context, hour, minute)
        }
    }
}
