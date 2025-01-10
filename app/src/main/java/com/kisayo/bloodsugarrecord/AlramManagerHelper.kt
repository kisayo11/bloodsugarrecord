package com.kisayo.bloodsugarrecord

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kisayo.bloodsugarrecord.Adapters.AlarmReceiver
import java.util.Calendar

class AlarmManagerHelper(private val context: Context) {
    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val CHANNEL_NAME = "Alarm Notifications"
    }

    // 알림 채널 생성
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "알람 알림을 위한 채널입니다"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 권한 체크 및 요청
    fun checkAndRequestPermissions(activity: ComponentActivity, onPermissionResult: (Boolean) -> Unit) {
        // 알림 권한 체크 (Android 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    checkScheduleExactAlarm(activity)
                    onPermissionResult(true)
                }

                activity.shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    showNotificationPermissionDialog(activity, onPermissionResult)
                }

                else -> {
                    requestNotificationPermission(activity, onPermissionResult)
                }
            }
        } else {
            // Android 13 미만에서는 알람 권한만 체크
            checkScheduleExactAlarm(activity)
            onPermissionResult(true)
        }
    }

    // 정확한 알람 권한 체크
    private fun checkScheduleExactAlarm(activity: ComponentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                showScheduleExactAlarmDialog(activity)
            }
        }
    }

    // 알람 설정
    fun setAlarm(label: String, calendar: Calendar, onSuccess: () -> Unit) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("label", label)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            label.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e("Alarm", "Cannot schedule exact alarm - permission not granted")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        onSuccess()
    }

    private fun showNotificationPermissionDialog(
        activity: ComponentActivity,
        onPermissionResult: (Boolean) -> Unit
    ) {
        MaterialAlertDialogBuilder(activity, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("알림 설정")
            .setMessage("앱 알림을 받으려면 권한이 필요해요")
            .setIcon(R.drawable.ic_notifications)
            .setPositiveButton("설정하기") { _, _ ->
                requestNotificationPermission(activity, onPermissionResult)
            }
            .setCancelable(false)
            .create()
            .apply {
                setCanceledOnTouchOutside(false)
                show()
            }
    }

    private fun showScheduleExactAlarmDialog(activity: ComponentActivity) {
        MaterialAlertDialogBuilder(activity, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("알람 권한 요청")
            .setMessage("설정 화면의 '알람 및 리마인더' 에서\n[나의 혈당 수첩]" +
                    "\n스위치를 활성화 해주세요")
            .setIcon(R.drawable.ic_notifications)
            .setPositiveButton("설정하기") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    activity.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            }
            .setCancelable(false)
            .create()
            .apply {
                setCanceledOnTouchOutside(false)
                show()
            }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission(
        activity: ComponentActivity,
        onPermissionResult: (Boolean) -> Unit
    ) {
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                checkScheduleExactAlarm(activity)
            }
            onPermissionResult(isGranted)
        }.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}