package com.kisayo.bloodsugarrecord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.kisayo.bloodsugarrecord.data.database.GlucoseDatabase
import com.kisayo.bloodsugarrecord.data.model.DailyRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GlucoseDatabase.getDatabase(application)
    private val dailyRecordDao = database.dailyRecordDao()

    private val _lineChartData = MutableLiveData<List<Entry>>()
    val lineChartData: LiveData<List<Entry>> = _lineChartData

    private val _barChartData = MutableLiveData<List<BarEntry>>()
    val barChartData: LiveData<List<BarEntry>> = _barChartData

    private val _summaryStatistics = MutableLiveData<SummaryStatistics>()
    val summaryStatistics: LiveData<SummaryStatistics> = _summaryStatistics

    private val _healthInsight = MutableLiveData<String>()
    val healthInsight: LiveData<String> = _healthInsight

    data class SummaryStatistics(
        val averageBloodSugar: Double,
        val maxBloodSugar: Int,
        val minBloodSugar: Int
    )

    fun loadChartData(periodDays: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // 최근 periodDays 동안의 데이터 가져오기
            val records = dailyRecordDao.getRecordsForPeriod(periodDays)

            // 라인 차트 데이터 변환
            val lineEntries = records.mapIndexed { index, record ->
                // 예시: 날짜를 x축, 평균 혈당을 y축으로 사용
                Entry(index.toFloat(), calculateAverageBloodSugar(record))
            }

            // 바 차트 데이터 변환
            val barEntries = listOf(
                BarEntry(0f, records.mapNotNull { it.fasting }.average().toFloat()),
                BarEntry(1f, records.mapNotNull { it.breakfastBefore }.average().toFloat()),
                BarEntry(2f, records.mapNotNull { it.breakfastAfter }.average().toFloat()),
                BarEntry(3f, records.mapNotNull { it.lunchBefore }.average().toFloat()),
                BarEntry(4f, records.mapNotNull { it.lunchAfter }.average().toFloat()),
                BarEntry(5f, records.mapNotNull { it.dinnerBefore }.average().toFloat())
            )

            // UI 스레드에서 LiveData 업데이트
            withContext(Dispatchers.Main) {
                _lineChartData.value = lineEntries
                _barChartData.value = barEntries
            }
        }
    }

    private fun calculateAverageBloodSugar(record: DailyRecord): Float {
        val bloodSugarValues = listOfNotNull(
            record.fasting,
            record.breakfastBefore,
            record.breakfastAfter,
            record.lunchBefore,
            record.lunchAfter,
            record.dinnerBefore,
            record.dinnerAfter
        )
        return if (bloodSugarValues.isNotEmpty()) bloodSugarValues.average().toFloat()
        else 0f
    }

    private fun generateHealthInsight(stats: SummaryStatistics, records: List<DailyRecord>): String {
        return when {
            stats.averageBloodSugar > 180 ->
                "현재 혈당 수치가 높은 편입니다. 의사와 상담하여 식단 및 생활 습관을 조정해보세요."

            stats.averageBloodSugar in 140.0..180.0 ->
                "혈당 관리에 주의가 필요합니다. 규칙적인 운동과 균형 잡힌 식단을 유지하세요."

            stats.averageBloodSugar in 80.0..139.0 ->
                "현재 혈당 관리 상태가 양호합니다. 건강한 생활 습관을 계속 유지하세요."

            else ->
                "혈당 수치가 낮은 편입니다. 전문의와 상담하여 적절한 대응 방법을 찾아보세요."
        }
    }
}
