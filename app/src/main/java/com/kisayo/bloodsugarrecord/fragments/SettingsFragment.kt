package com.kisayo.bloodsugarrecord.fragments

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.kisayo.bloodsugarrecord.Adapters.AlarmReceiver
import com.kisayo.bloodsugarrecord.data.dao.DailyRecordDao
import com.kisayo.bloodsugarrecord.data.database.GlucoseDatabase
import com.kisayo.bloodsugarrecord.databinding.DialogSecureResetBinding
import com.kisayo.bloodsugarrecord.databinding.FragmentSettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    private lateinit var switchWakeUpTime: Switch
    private lateinit var switchBreakfastTime: Switch
    private lateinit var switchLunchTime: Switch
    private lateinit var switchDinnerTime: Switch
    private lateinit var btnExportCSV: Button
    private lateinit var btnResetData: Button

    private lateinit var alarmManager: AlarmManager
    private var selectedWakeUpTime: Calendar = Calendar.getInstance()
    private var selectedBreakfastTime: Calendar = Calendar.getInstance()
    private var selectedLunchTime: Calendar = Calendar.getInstance()
    private var selectedDinnerTime: Calendar = Calendar.getInstance()

    private lateinit var dao: DailyRecordDao
    private lateinit var db: GlucoseDatabase


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 알림 설정 토글들 초기화
        switchWakeUpTime = binding.switchWakeUpTime
        switchBreakfastTime = binding.switchBreakfastTime
        switchLunchTime = binding.switchLunchTime
        switchDinnerTime = binding.switchDinnerTime

        // 데이터 관리 버튼들 초기화
        btnExportCSV = binding.btnExportCSV
        btnResetData = binding.btnResetData

        // 알림 매니저 초기화
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // dao 초기화 추가
        val db = Room.databaseBuilder(
            requireContext().applicationContext,
            GlucoseDatabase::class.java,
            "glucose_database"
        ).build()
        dao = db.dailyRecordDao()

        // 각 토글의 상태를 변경했을 때 알림 설정 적용
        switchWakeUpTime.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showTimePickerDialog("기상", selectedWakeUpTime) { hour, minute ->
                    selectedWakeUpTime.set(Calendar.HOUR_OF_DAY, hour)
                    selectedWakeUpTime.set(Calendar.MINUTE, minute)
                    enableAlarm("기상", selectedWakeUpTime)
                }
            } else {
                disableAlarm("기상")
            }
        }

        switchBreakfastTime.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showTimePickerDialog("아침 식전", selectedBreakfastTime) { hour, minute ->
                    selectedBreakfastTime.set(Calendar.HOUR_OF_DAY, hour)
                    selectedBreakfastTime.set(Calendar.MINUTE, minute)
                    enableAlarm("아침 식전", selectedBreakfastTime)
                }
            } else {
                disableAlarm("아침 식전")
            }
        }

        switchLunchTime.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showTimePickerDialog("점심 식전", selectedLunchTime) { hour, minute ->
                    selectedLunchTime.set(Calendar.HOUR_OF_DAY, hour)
                    selectedLunchTime.set(Calendar.MINUTE, minute)
                    enableAlarm("점심 식전", selectedLunchTime)
                }
            } else {
                disableAlarm("점심 식전")
            }
        }

        switchDinnerTime.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showTimePickerDialog("저녁 식전", selectedDinnerTime) { hour, minute ->
                    selectedDinnerTime.set(Calendar.HOUR_OF_DAY, hour)
                    selectedDinnerTime.set(Calendar.MINUTE, minute)
                    enableAlarm("저녁 식전", selectedDinnerTime)
                }
            } else {
                disableAlarm("저녁 식전")
            }
        }

        // 데이터 내보내기
        btnExportCSV.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                             val file = exportDataToCSV(requireContext(), dao)
                withContext(Dispatchers.Main) {
                    if (file != null) {
                                              shareCSVFile(requireContext(), file)
                    } else {
                                    Toast.makeText(requireContext(), "데이터 내보내기 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 데이터 리셋
        btnResetData.setOnClickListener {showSecureResetDialog()       }
    }

    private fun showSecureResetDialog() {
        val dialogBinding = DialogSecureResetBinding.inflate(layoutInflater)

        // 6자리 랜덤 숫자 생성
        val randomCode = (100000..999999).random()

        with(dialogBinding) {
            tvRandomCode.text = "다음 코드를 입력하세요: $randomCode"

            AlertDialog.Builder(requireContext())
                .setTitle("데이터 초기화")
                .setMessage("데이터를 초기화하려면 아래 표시된 코드를 정확히 입력하세요.")
                .setView(root)
                .setPositiveButton("확인") { dialog, _ ->
                    val inputCode = etInputCode.text.toString()
                    if (inputCode == randomCode.toString()) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            dao.clearAllRecords()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "모든 데이터가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "코드가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    // CSV 파일을 공유하는 함수
    private fun shareCSVFile(context: Context, file: File) {
        try {
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)

                // ClipData 추가
                clipData = ClipData.newRawUri("", fileUri)

                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "CSV 파일 공유")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(chooserIntent)

        } catch (e: Exception) {
            Toast.makeText(context, "파일 공유 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    // 데이터 초기화 함수
    private fun resetData(dao: DailyRecordDao) {
        dao.clearAllRecords() // YourDao의 `clearAllRecords` 메소드로 모든 데이터를 삭제
    }

    // 시간 선택 다이얼로그를 띄우는 함수
    private fun showTimePickerDialog(
        label: String,
        calendar: Calendar,
        onTimeSet: (Int, Int) -> Unit
    ) {
        val timePickerDialog = TimePickerDialog(
            requireContext(), { _, hourOfDay, minute ->
                onTimeSet(hourOfDay, minute)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        )
        timePickerDialog.setTitle("$label")
        timePickerDialog.show()
    }

    // 알람을 활성화하는 함수
    private fun enableAlarm(label: String, calendar: Calendar) {
        checkAndRequestAlarmPermission(label, calendar)
    }


    // 알람 설정 함수
    private fun setAlarm(label: String, calendar: Calendar) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            label.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }

        Toast.makeText(context, "$label 알림 설정", Toast.LENGTH_SHORT).show()
    }

    private fun checkAndRequestAlarmPermission(label: String, calendar: Calendar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also {
                    startActivity(it)
                }
                Toast.makeText(context, "정확한 알람 권한을 허용해주세요", Toast.LENGTH_LONG).show()
                return
            }
        }
        // 권한이 있거나 Android 12 미만인 경우 알람 설정
        setAlarm(label, calendar)
    }

    private fun disableAlarm(label: String) {
        cancelAlarm(label)
    }

    // 알람 취소 함수
    private fun cancelAlarm(label: String) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            label.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알람 취소
        alarmManager.cancel(pendingIntent)

        Toast.makeText(context, "$label 알림을 해제합니다", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == ALARM_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우 알람 설정 로직 실행
                when {
                    switchWakeUpTime.isChecked -> setAlarm("기상 시간", selectedWakeUpTime)
                    switchBreakfastTime.isChecked -> setAlarm("아침 식전", selectedBreakfastTime)
                    switchLunchTime.isChecked -> setAlarm("점심 식전", selectedLunchTime)
                    switchDinnerTime.isChecked -> setAlarm("저녁 식전", selectedDinnerTime)
                }
            } else {
                // 권한이 거부된 경우 알람 설정 불가 메시지 표시
                Toast.makeText(context, "알람 설정을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                // 토글 스위치를 off 상태로 변경
                when {
                    switchWakeUpTime.isChecked -> switchWakeUpTime.isChecked = false
                    switchBreakfastTime.isChecked -> switchBreakfastTime.isChecked = false
                    switchLunchTime.isChecked -> switchLunchTime.isChecked = false
                    switchDinnerTime.isChecked -> switchDinnerTime.isChecked = false
                }
            }
        }
    }

    private fun exportDataToCSV(context: Context, dao: DailyRecordDao): File? {
        val records = dao.getAllRecords()
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val fileName = "my_blood_sugar_note$currentDate.csv"
        val file = File(context.getExternalFilesDir(null), fileName)

        return try {
            // Kotlin CSV 라이브러리 사용
            file.bufferedWriter().use { writer ->
                // CSV 헤더 작성
                val headers = listOf(
                    "Date", "Fasting", "Breakfast Before", "Breakfast After",
                    "Lunch Before", "Lunch After", "Dinner Before", "Dinner After",
                    "Weight", "Notes", "Updated At"
                )
                writer.write(headers.joinToString(",") + "\n")

                // 데이터 작성
                records.forEach { record ->
                    val row = listOf(
                        record.date,
                        record.fasting?.toString() ?: "",
                        record.breakfastBefore?.toString() ?: "",
                        record.breakfastAfter?.toString() ?: "",
                        record.lunchBefore?.toString() ?: "",
                        record.lunchAfter?.toString() ?: "",
                        record.dinnerBefore?.toString() ?: "",
                        record.dinnerAfter?.toString() ?: "",
                        record.weight?.toString() ?: "",
                        record.notes?.replace(",", ";") ?: "",
                        record.updatedAt.toString()
                    ).joinToString(",") { value ->
                        // 쉼표나 줄바꿈이 포함된 경우 큰따옴표로 감싸기
                        if (value.contains(",") || value.contains("\n")) {
                            "\"${value.replace("\"", "\"\"")}\""
                        } else {
                            value
                        }
                    }
                    writer.write(row + "\n")
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val ALARM_PERMISSION_REQUEST_CODE = 100
    }
}