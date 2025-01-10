package com.kisayo.bloodsugarrecord

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.kisayo.bloodsugarrecord.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var alarmManagerHelper: AlarmManagerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()



        alarmManagerHelper = AlarmManagerHelper(this)
        alarmManagerHelper.createNotificationChannel()
        alarmManagerHelper.checkAndRequestPermissions(this) { granted ->
            if (granted) {
                Log.d("Permission", "All necessary permissions granted")
            } else {
                Log.d("Permission", "Some permissions were denied")
            }
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