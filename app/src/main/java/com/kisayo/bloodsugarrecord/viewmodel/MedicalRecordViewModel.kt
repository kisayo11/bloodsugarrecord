package com.kisayo.bloodsugarrecord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kisayo.bloodsugarrecord.data.database.MedicalDatabase
import com.kisayo.bloodsugarrecord.data.model.MedicalRecord
import com.kisayo.bloodsugarrecord.data.repository.MedicalRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicalRecordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MedicalRecordRepository
    val recentRecords: Flow<List<MedicalRecord>>

    init {
        val medicalRecordDao = MedicalDatabase.getDatabase(application).medicalRecordDao()
        repository = MedicalRecordRepository(medicalRecordDao)
        recentRecords = repository.recentRecords
    }

    fun insert(record: MedicalRecord) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(record)
    }

    fun update(record: MedicalRecord) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(record)
    }

    fun delete(record: MedicalRecord) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(record)
    }

    // date로 조회하는 함수 추가
    suspend fun getRecordByDate(date: String): MedicalRecord? {
        return withContext(Dispatchers.IO) {
            repository.getRecordByDate(date)
        }
    }

    // date로 삭제하는 함수 추가
    fun deleteRecordByDate(date: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteRecordByDate(date)
    }
}