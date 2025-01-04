package com.kisayo.bloodsugarrecord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.kisayo.bloodsugarrecord.data.database.GlucoseDatabase
import com.kisayo.bloodsugarrecord.data.model.DailyRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GlucoseDatabase.getDatabase(application)
    private val dailyRecordDao = database.dailyRecordDao()

    fun getRecentRecords(limit: Int): LiveData<List<DailyRecord>> =
        dailyRecordDao.getRecentRecords(limit)

    fun updateNotes(date: String, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dailyRecordDao.updateNotes(date, notes, System.currentTimeMillis())
        }
    }
    fun deleteRecord(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dailyRecordDao.deleteRecord(date)
        }
    }
}