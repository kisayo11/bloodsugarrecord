package com.kisayo.bloodsugarrecord.data.repository

import com.kisayo.bloodsugarrecord.data.dao.MedicalRecordDao
import com.kisayo.bloodsugarrecord.data.model.MedicalRecord

class MedicalRecordRepository(private val medicalRecordDao: MedicalRecordDao) {
    val recentRecords = medicalRecordDao.getRecentRecords(5)
    val allRecords = medicalRecordDao.getAllRecords()

    suspend fun insert(record: MedicalRecord) {
        medicalRecordDao.insert(record)
    }

    suspend fun update(record: MedicalRecord) {
        medicalRecordDao.updateRecord(record)
    }

    suspend fun delete(record: MedicalRecord) {
        medicalRecordDao.deleteRecord(record)
    }

    suspend fun getRecordByDate(date: String): MedicalRecord? {
        return medicalRecordDao.getRecordByDate(date)
    }

    suspend fun deleteRecordByDate(date: String) {
        medicalRecordDao.deleteRecordByDate(date)
    }
}