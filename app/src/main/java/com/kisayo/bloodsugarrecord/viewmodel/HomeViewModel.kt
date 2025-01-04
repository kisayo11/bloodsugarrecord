package com.kisayo.bloodsugarrecord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.kisayo.bloodsugarrecord.data.dao.DailyRecordDao
import com.kisayo.bloodsugarrecord.data.database.GlucoseDatabase
import com.kisayo.bloodsugarrecord.data.model.DailyRecord
import com.kisayo.bloodsugarrecord.data.repository.DailyRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DailyRecordRepository

    init {
        val database = GlucoseDatabase.getDatabase(application)
        repository = DailyRecordRepository(database.dailyRecordDao())
    }

    private val _dailyRecord = MutableLiveData<DailyRecord>()
    val dailyRecord: LiveData<DailyRecord> = _dailyRecord

    private val _chartData = MutableLiveData<List<Entry>>()
    val chartData: LiveData<List<Entry>> = _chartData

    fun getDailyRecord(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val record = repository.getOrCreateRecord(date)
            _dailyRecord.postValue(record)

            // 최근 7일간의 공복 데이터도 가져옴
            val weeklyRecords = repository.getRecentRecords(8)
            val chartEntries = weeklyRecords
                .filter { it.fasting != null && it.fasting > 0 }
                .reversed()
                .mapIndexed { index, record ->
                    Entry(index.toFloat(), record.fasting!!.toFloat(), record.date)
                }
            _chartData.postValue(chartEntries)
        }
    }

    fun updateGlucoseRecord(date: String, type: String, value: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val field = when (type) {
                "공복" -> "fasting"
                "아침 식전" -> "breakfast_before"
                "아침 식후" -> "breakfast_after"
                "점심 식전" -> "lunch_before"
                "점심 식후" -> "lunch_after"
                "저녁 식전" -> "dinner_before"
                "저녁 식후" -> "dinner_after"
                else -> throw IllegalArgumentException("Invalid type: $type")
            }
            repository.updateField(date, field, value)
            getDailyRecord(date)
        }
    }

    fun updateWeightRecord(date: String, value: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateField(date, "weight", value)
            getDailyRecord(date)
        }
    }
}