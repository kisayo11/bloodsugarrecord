package com.kisayo.bloodsugarrecord.fragments

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.kisayo.bloodsugarrecord.R
import com.kisayo.bloodsugarrecord.data.model.DailyRecord
import com.kisayo.bloodsugarrecord.databinding.FragmentHomeBinding
import com.kisayo.bloodsugarrecord.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    private val dateFormat = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
    private var currentDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // ViewModel 초기화
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // 초기 날짜 설정
        binding.tvDate.text = dateFormat.format(currentDate.time)

        // 달력 감추기
        binding.calendarView.visibility = View.GONE

        setupListeners()

        // 초기 데이터 로드
        updateDate()

        return view
    }

    private fun setupListeners() {
        // 날짜 텍스트뷰 클릭: 달력 표시/숨김
        binding.tvDate.setOnClickListener { toggleCalendarVisibility() }
        // 이전 날짜 버튼
        binding.btnPrevDay.setOnClickListener { changeDate(-1) }
        // 다음 날짜 버튼
        binding.btnNextDay.setOnClickListener { changeDate(1) }
        // 달력 날짜 선택
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            currentDate.set(year, month, dayOfMonth)
            updateDate()
            binding.calendarView.visibility = View.GONE
        }
        // 각 카드뷰 클릭 리스너 설정
        setupCardViewListeners()
    }

    private fun setupCardViewListeners() {
        val glucoseCards = listOf(
            Triple("공복", binding.cardFasting, binding.tvFastingValue),
            Triple("아침 식전", binding.cardBreakfastBefore, binding.tvBreakfastBeforeValue),
            Triple("아침 식후", binding.cardBreakfastAfter, binding.tvBreakfastAfterValue),
            Triple("점심 식전", binding.cardLunchBefore, binding.tvLunchBeforeValue),
            Triple("점심 식후", binding.cardLunchAfter, binding.tvLunchAfterValue),
            Triple("저녁 식전", binding.cardDinnerBefore, binding.tvDinnerBeforeValue),
            Triple("저녁 식후", binding.cardDinnerAfter, binding.tvDinnerAfterValue)
        )

        glucoseCards.forEach { (type, card, valueView) ->
            card.setOnClickListener {
                showGlucoseInputDialog(type) { value ->
                    lifecycleScope.launch {
                        viewModel.updateGlucoseRecord(dateFormat.format(currentDate.time), type, value)
                        updateDate()
                    }
                }
            }
        }

        binding.cardWeight.setOnClickListener {
            showWeightInputDialog()
        }
    }

    private fun showGlucoseInputDialog(type: String, onSave: (Int) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_glucose_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextGlucose)

        AlertDialog.Builder(requireContext())
            .setTitle("$type 혈당 기록")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val value = editText.text.toString().toIntOrNull()
                value?.let { onSave(it) }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showWeightInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_weight_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextWeight)

        AlertDialog.Builder(requireContext())
            .setTitle("체중 기록")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val value = editText.text.toString().toDoubleOrNull()
                value?.let {
                    lifecycleScope.launch {
                        viewModel.updateWeightRecord(dateFormat.format(currentDate.time), it)
                        updateDate()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateGlucoseCard(valueTextView: TextView, progressBar: ProgressBar, value: Int?) {
        if (value != null) {
            valueTextView.text = "$value mg/dL"

            val colorResId = when {
                value < 70 -> R.color.status_low
                value in 70..125 -> R.color.status_normal
                value in 126..180 -> R.color.status_warning
                else -> R.color.status_danger
            }

            val color = ContextCompat.getColor(requireContext(), colorResId)

            progressBar.apply {
                progress = when {
                    value < 70 -> 25
                    value in 70..99 -> 50
                    value in 100..125 -> 75
                    else -> 100
                }
                progressTintList = ColorStateList.valueOf(color)
            }
        } else {
            valueTextView.text = "-- mg/dL"
            progressBar.progress = 0
        }
    }

    private fun toggleCalendarVisibility() {
        binding.calendarView.visibility =
            if (binding.calendarView.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun changeDate(days: Int) {
        currentDate.add(Calendar.DAY_OF_MONTH, days)
        updateDate()
    }

    private fun updateDate() {
        val dateString = dateFormat.format(currentDate.time)
        binding.tvDate.text = dateString
        Log.d("HomeFragment", "Date updated to: $dateString")

        viewModel.getDailyRecord(dateString)

        viewModel.dailyRecord.observe(viewLifecycleOwner) { dailyRecord ->
            updateUIWithDailyRecord(dailyRecord)
        }

        viewModel.prepareDailyChartData(dateString)
        viewModel.chartData.observe(viewLifecycleOwner) { chartData ->
            setupDailyChart(chartData)
        }
    }

    private fun updateUIWithDailyRecord(record: DailyRecord) {
        updateGlucoseCard(binding.tvFastingValue, binding.statusIndicator, record.fasting)
        updateGlucoseCard(binding.tvBreakfastBeforeValue, binding.statusIndicatorBreakfastBefore, record.breakfastBefore)
        updateGlucoseCard(binding.tvBreakfastAfterValue, binding.statusIndicatorBreakfastAfter, record.breakfastAfter)
        updateGlucoseCard(binding.tvLunchBeforeValue, binding.statusIndicatorLunchBefore, record.lunchBefore)
        updateGlucoseCard(binding.tvLunchAfterValue, binding.statusIndicatorLunchAfter, record.lunchAfter)
        updateGlucoseCard(binding.tvDinnerBeforeValue, binding.statusIndicatorDinnerBefore, record.dinnerBefore)
        updateGlucoseCard(binding.tvDinnerAfterValue, binding.statusIndicatorDinnerAfter, record.dinnerAfter)
        binding.tvWeightValue.text = record.weight?.let { String.format("%.1f kg", it) } ?: "-- kg"
    }

    private fun setupDailyChart(records: List<Entry>) {
        val chartDaily = binding.chartDaily

        // 차트 기본 설정
        chartDaily.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
        }

        // 데이터가 없을 경우 처리
        if (records.isEmpty()) {
            chartDaily.clear()
            chartDaily.setNoDataText("데이터가 없습니다")
            chartDaily.invalidate()
            return
        }

        // X축 설정
        chartDaily.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            labelCount = records.size
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < records.size) {
                        records[index].data as? String ?: ""
                    } else {
                        ""
                    }
                }
            }
        }

        // Y축 설정
        chartDaily.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 300f
        }
        chartDaily.axisRight.isEnabled = false

        val dataSet = LineDataSet(records, "혈당").apply {
            color = ContextCompat.getColor(requireContext(), R.color.black)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.black))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(true)
        }

        val lineData = LineData(dataSet)
        chartDaily.data = lineData
        chartDaily.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}