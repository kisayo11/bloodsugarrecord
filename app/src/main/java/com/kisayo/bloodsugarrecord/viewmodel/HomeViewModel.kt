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
            getDailyRecord(date)  // Refresh the daily record
        }
    }

    fun updateWeightRecord(date: String, value: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateField(date, "weight", value)
            getDailyRecord(date)  // Refresh the daily record
        }
    }

    fun prepareDailyChartData(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val record = repository.getOrCreateRecord(date)
            val dataPoints = listOf(
                "공복" to record.fasting,
                "아침 식전" to record.breakfastBefore,
                "아침 식후" to record.breakfastAfter,
                "점심 식전" to record.lunchBefore,
                "점심 식후" to record.lunchAfter,
                "저녁 식전" to record.dinnerBefore,
                "저녁 식후" to record.dinnerAfter
            )

            val entries = dataPoints.mapIndexedNotNull { index, (type, value) ->
                value?.let { Entry(index.toFloat(), it.toFloat(), type) }
            }
            _chartData.postValue(entries)
        }
    }
}