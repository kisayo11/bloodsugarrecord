package com.kisayo.bloodsugarrecord

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.kisayo.bloodsugarrecord.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()

        // 알림 채널 설정 (안드로이드 8.0 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 바텀 네비게이션 설정
        binding.bottomNavigation.setupWithNavController(navController)

        // 앱바 타이틀 자동 변경
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.topAppBar.title = when(destination.id) {
                R.id.navigation_home -> "나의 혈당 수첩"
                R.id.navigation_records -> "기록"
                R.id.navigation_medinote -> "메디노트"
                R.id.navigation_analysis -> "분석보기"
                R.id.navigation_settings -> "설정"
                else -> "나의 혈당 수첩"
            }
        }
    }
}