package com.kisayo.bloodsugarrecord.Adapters

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kisayo.bloodsugarrecord.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alarm Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // 메인 액티비티를 열기 위한 Intent 생성
        val openAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        openAppIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // PendingIntent 생성
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationIcon = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            // Android 8.0 이하에서 흑백 아이콘
            R.drawable.icon_logo
        } else {
            // Android 8.1 이상에서는 컬러 아이콘
            R.drawable.icon_logo_1024
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("알림")
            .setContentText("혈당을 기록하세요!")
            .setSmallIcon(notificationIcon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}