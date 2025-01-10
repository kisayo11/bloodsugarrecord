package com.kisayo.bloodsugarrecord.fragments

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.kisayo.bloodsugarrecord.Adapters.AlarmReceiver
import com.kisayo.bloodsugarrecord.AlarmManagerHelper
import com.kisayo.bloodsugarrecord.R
import com.kisayo.bloodsugarrecord.data.dao.DailyRecordDao
import com.kisayo.bloodsugarrecord.data.database.GlucoseDatabase
import com.kisayo.bloodsugarrecord.databinding.DialogSecureResetBinding
import com.kisayo.bloodsugarrecord.databinding.FragmentSettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var alarmManagerHelper: AlarmManagerHelper

    private lateinit var switchWakeUpTime: Switch
    private lateinit var switchBreakfastTime: Switch
    private lateinit var switchLunchTime: Switch
    private lateinit var switchDinnerTime: Switch
    private lateinit var btnExportCSV: Button
    private lateinit var btnResetData: Button

    private var selectedWakeUpTime: Calendar = Calendar.getInstance()
    private var selectedBreakfastTime: Calendar = Calendar.getInstance()
    private var selectedLunchTime: Calendar = Calendar.getInstance()
    private var selectedDinnerTime: Calendar = Calendar.getInstance()

    private lateinit var dao: DailyRecordDao
    private lateinit var db: GlucoseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // AlarmManagerHelper 초기화
        alarmManagerHelper = AlarmManagerHelper(requireContext())

        initializeViews()
        setupDatabase()
        setupClickListeners()
    }

    private fun initializeViews() {
        switchWakeUpTime = binding.switchWakeUpTime
        switchBreakfastTime = binding.switchBreakfastTime
        switchLunchTime = binding.switchLunchTime
        switchDinnerTime = binding.switchDinnerTime
        btnExportCSV = binding.btnExportCSV
        btnResetData = binding.btnResetData
    }

    private fun setupDatabase() {
        db = Room.databaseBuilder(
            requireContext().applicationContext,
            GlucoseDatabase::class.java,
            "glucose_database"
        ).build()
        dao = db.dailyRecordDao()
    }

    private fun setupClickListeners() {
        // 알람 토글 리스너 설정
        switchWakeUpTime.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                alarmManagerHelper.checkAndRequestPermissions(requireActivity()) { granted ->
                    if (granted) {
                        handleAlarmToggle("기상", selectedWakeUpTime, isChecked)
                    } else {
                        buttonView.isChecked = false
                        Toast.makeText(context, "알람 설정을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                handleAlarmToggle("기상", selectedWakeUpTime, isChecked)
            }
        }

        switchBreakfastTime.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                alarmManagerHelper.checkAndRequestPermissions(requireActivity()) { granted ->
                    if (granted) {
                        handleAlarmToggle("아침 식전", selectedBreakfastTime, isChecked)
                    } else {
                        buttonView.isChecked = false
                        Toast.makeText(context, "알람 설정을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                handleAlarmToggle("아침 식전", selectedBreakfastTime, isChecked)
            }
        }

        switchLunchTime.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                alarmManagerHelper.checkAndRequestPermissions(requireActivity()) { granted ->
                    if (granted) {
                        handleAlarmToggle("점심 식전", selectedLunchTime, isChecked)
                    } else {
                        buttonView.isChecked = false
                        Toast.makeText(context, "알람 설정을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                handleAlarmToggle("점심 식전", selectedLunchTime, isChecked)
            }
        }

        switchDinnerTime.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                alarmManagerHelper.checkAndRequestPermissions(requireActivity()) { granted ->
                    if (granted) {
                        handleAlarmToggle("저녁 식전", selectedDinnerTime, isChecked)
                    } else {
                        buttonView.isChecked = false
                        Toast.makeText(context, "알람 설정을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                handleAlarmToggle("저녁 식전", selectedDinnerTime, isChecked)
            }
        }

        // CSV 내보내기 버튼
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

        // 데이터 리셋 버튼
        btnResetData.setOnClickListener {
            showSecureResetDialog()
        }
    }

    private fun handleAlarmToggle(label: String, calendar: Calendar, isChecked: Boolean) {
        if (isChecked) {
            showTimePickerDialog(label, calendar) { hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                enableAlarm(label, calendar)
            }
        } else {
            disableAlarm(label)
        }
    }

    private fun enableAlarm(label: String, calendar: Calendar) {
        alarmManagerHelper.setAlarm(label, calendar) {
            Toast.makeText(context, "$label 알림 설정", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableAlarm(label: String) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("label", label)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            label.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent.cancel()
        Toast.makeText(context, "$label 알림을 해제합니다", Toast.LENGTH_SHORT).show()
    }

    private fun showTimePickerDialog(
        label: String,
        calendar: Calendar,
        onTimeSet: (Int, Int) -> Unit
    ) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText(label)
            .setTheme(R.style.CustomTimePickerTheme)
            .build()

        picker.addOnPositiveButtonClickListener {
            onTimeSet(picker.hour, picker.minute)
        }

        picker.show(childFragmentManager, "TIME_PICKER")
    }

    private fun showSecureResetDialog() {
        val dialogBinding = DialogSecureResetBinding.inflate(layoutInflater)
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
                                Toast.makeText(requireContext(), "모든 데이터가 초기화되었습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "코드가 일치하지 않습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

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

    private fun exportDataToCSV(context: Context, dao: DailyRecordDao): File? {
        val records = dao.getAllRecords()
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val fileName = "my_blood_sugar_note$currentDate.csv"
        val file = File(context.getExternalFilesDir(null), fileName)

        return try {
            file.bufferedWriter().use { writer ->
                val headers = listOf(
                    "Date", "Fasting", "Breakfast Before", "Breakfast After",
                    "Lunch Before", "Lunch After", "Dinner Before", "Dinner After",
                    "Weight", "Notes", "Updated At"
                )
                writer.write(headers.joinToString(",") + "\n")

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
}