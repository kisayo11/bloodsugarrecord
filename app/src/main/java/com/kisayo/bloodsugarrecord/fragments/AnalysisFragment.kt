package com.kisayo.bloodsugarrecord.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.kisayo.bloodsugarrecord.R
import com.kisayo.bloodsugarrecord.databinding.FragmentAnalysisBinding
import com.kisayo.bloodsugarrecord.viewmodel.AnalysisViewModel

class AnalysisFragment : Fragment() {
    private lateinit var binding: FragmentAnalysisBinding
    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart
    private val viewModel: AnalysisViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 차트 초기화
        lineChart = binding.bloodSugarLineChart
        barChart = binding.bloodSugarBarChart

        // 기간 선택 칩 설정
        setupPeriodChips()

        // 데이터 관찰
        observeChartData()
    }

    private fun setupPeriodChips() {
        binding.chipGroupPeriod.setOnCheckedChangeListener { group, checkedId ->
            val period = when (checkedId) {
                R.id.chipWeek -> 7
                R.id.chipMonth -> 30
                R.id.chipThreeMonths -> 90
                R.id.chipSixMonths -> 180
                R.id.chipYear -> 365
                R.id.chipAll -> Int.MAX_VALUE
                else -> 30 // 기본값
            }
            viewModel.loadChartData(period)
        }

        // 기본적으로 1개월 선택
        binding.chipMonth.isChecked = true
    }

    private fun observeChartData() {
        viewModel.lineChartData.observe(viewLifecycleOwner) { data ->
            setupLineChart(data)
        }

        viewModel.barChartData.observe(viewLifecycleOwner) { data ->
            setupBarChart(data)
        }
    }

    private fun setupLineChart(entries: List<Entry>) {
        val dataSet = LineDataSet(entries, "혈당").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
        }

        lineChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            invalidate()
        }
    }

    private fun setupBarChart(entries: List<BarEntry>) {
        val dataSet = BarDataSet(entries, "시간대별 혈당").apply {
            colors = listOf(
                Color.BLUE,
                Color.GREEN,
                Color.RED,
                Color.YELLOW,
                Color.CYAN,
                Color.MAGENTA
            )
            setDrawValues(true)
        }

        barChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            invalidate()
        }
    }
}