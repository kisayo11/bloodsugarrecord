package com.kisayo.bloodsugarrecord.fragments

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
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
import com.kisayo.bloodsugarrecord.ui.insulin.components.InsulinCard
import com.kisayo.bloodsugarrecord.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.kisayo.bloodsugarrecord.data.model.InsulinInjection
import com.kisayo.bloodsugarrecord.viewmodel.InsulinViewModel
import kotlinx.coroutines.flow.collect

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var insulinViewModel: InsulinViewModel

    private val dateFormat = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
    private var currentDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        insulinViewModel = ViewModelProvider(this).get(InsulinViewModel::class.java)


        binding.tvDate.text = dateFormat.format(currentDate.time)
        binding.calendarView.visibility = View.GONE

        // Compose 설정
        binding.insulinCardCompose.setContent {
            InsulinCard(
                viewModel = insulinViewModel
            )
        }


        setupListeners()
        updateDate()

        return view
    }


    private fun setupListeners() {
        binding.tvDate.setOnClickListener { toggleCalendarVisibility() }
        binding.btnPrevDay.setOnClickListener { changeDate(-1) }
        binding.btnNextDay.setOnClickListener { changeDate(1) }
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            currentDate.set(year, month, dayOfMonth)
            updateDate()
            binding.calendarView.visibility = View.GONE
        }
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
                        viewModel.updateGlucoseRecord(
                            dateFormat.format(currentDate.time),
                            type,
                            value
                        )
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

        AlertDialog.Builder(requireContext()).setTitle("$type 혈당 기록").setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val value = editText.text.toString().toIntOrNull()
                value?.let { onSave(it) }
            }.setNegativeButton("취소", null).show().also { dialog ->
                // "저장" 버튼 색상 변경
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.strong_blue)
                )
                // "취소" 버튼 색상 변경
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.strong_blue)
                )
            }
    }

    private fun showWeightInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_weight_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextWeight)

        AlertDialog.Builder(requireContext()).setTitle("체중 기록").setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val value = editText.text.toString().toDoubleOrNull()
                value?.let {
                    lifecycleScope.launch {
                        viewModel.updateWeightRecord(dateFormat.format(currentDate.time), it)
                        updateDate()
                    }
                }
            }.setNegativeButton("취소", null).show().also { dialog ->
                // "저장" 버튼 색상 변경
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.strong_blue)
                )
                // "취소" 버튼 색상 변경
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.strong_blue)
                )
            }

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

        viewModel.getDailyRecord(dateString)
        insulinViewModel.updateDate(dateString)

        viewModel.dailyRecord.observe(viewLifecycleOwner) { dailyRecord ->
            updateUIWithDailyRecord(dailyRecord)
        }

        viewModel.chartData.observe(viewLifecycleOwner) { entries ->
            setupDailyChart(entries)
        }

    }

    private fun updateUIWithDailyRecord(record: DailyRecord) {
        updateGlucoseCard(binding.tvFastingValue, binding.statusIndicator, record.fasting)
        updateGlucoseCard(
            binding.tvBreakfastBeforeValue,
            binding.statusIndicatorBreakfastBefore,
            record.breakfastBefore
        )
        updateGlucoseCard(
            binding.tvBreakfastAfterValue,
            binding.statusIndicatorBreakfastAfter,
            record.breakfastAfter
        )
        updateGlucoseCard(
            binding.tvLunchBeforeValue,
            binding.statusIndicatorLunchBefore,
            record.lunchBefore
        )
        updateGlucoseCard(
            binding.tvLunchAfterValue,
            binding.statusIndicatorLunchAfter,
            record.lunchAfter
        )
        updateGlucoseCard(
            binding.tvDinnerBeforeValue,
            binding.statusIndicatorDinnerBefore,
            record.dinnerBefore
        )
        updateGlucoseCard(
            binding.tvDinnerAfterValue,
            binding.statusIndicatorDinnerAfter,
            record.dinnerAfter
        )
        binding.tvWeightValue.text = record.weight?.let { String.format("%.1f kg", it) } ?: "-- kg"
    }

    private fun setupDailyChart(entries: List<Entry>) {
        val chartFastingDaily = binding.chartFasting

        // 평균 계산 및 표시
        val average = if (entries.isNotEmpty()) {
            entries.map { it.y }.average()
        } else 0.0
        binding.tvFastingAverage.text = if (average == 0.0) "--" else String.format("%.1f", average)

        chartFastingDaily.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)
            legend.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        if (index >= 0 && index < entries.size) {
                            val date = entries[index].data as String
                            Log.d("Chart", "Index: $index, Date: $date") // 날짜 확인용 로그
                            return SimpleDateFormat("M/d", Locale.getDefault()).format(
                                dateFormat.parse(date)!!
                            )
                        }
                        return ""
                    }
                }
                labelCount = entries.size  // 라벨 개수 명시적 설정
                granularity = 1f  // 최소 간격 설정
            }

            axisLeft.apply {
                axisMinimum = 60f
                axisMaximum = 200f
                setDrawGridLines(true)
            }
            axisRight.isEnabled = false
        }

        if (entries.isEmpty()) {
            chartFastingDaily.clear()
            chartFastingDaily.setNoDataText("데이터가 없습니다")
            return
        }

        val dataSet = LineDataSet(entries, "공복혈당").apply {
            color = ContextCompat.getColor(requireContext(), R.color.strong_blue)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.strong_blue))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}"
                }
            }
        }

        chartFastingDaily.data = LineData(dataSet)
        chartFastingDaily.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
