package com.kisayo.bloodsugarrecord.data.repository

import com.kisayo.bloodsugarrecord.data.dao.DailyRecordDao
import com.kisayo.bloodsugarrecord.data.model.DailyRecord

class DailyRecordRepository(private val dailyRecordDao: DailyRecordDao) {
    suspend fun getOrCreateRecord(date: String): DailyRecord {
        return dailyRecordDao.getRecordByDate(date) ?: DailyRecord(date = date).also {
            dailyRecordDao.insert(it)
        }
    }

    suspend fun updateField(date: String, field: String, value: Any) {
        val updatedAt = System.currentTimeMillis()
        when (field) {
            "fasting" -> dailyRecordDao.updateFasting(date, value as Int, updatedAt)
            "breakfast_before" -> dailyRecordDao.updateBreakfastBefore(date, value as Int, updatedAt)
            "breakfast_after" -> dailyRecordDao.updateBreakfastAfter(date, value as Int, updatedAt)
            "lunch_before" -> dailyRecordDao.updateLunchBefore(date, value as Int, updatedAt)
            "lunch_after" -> dailyRecordDao.updateLunchAfter(date, value as Int, updatedAt)
            "dinner_before" -> dailyRecordDao.updateDinnerBefore(date, value as Int, updatedAt)
            "dinner_after" -> dailyRecordDao.updateDinnerAfter(date, value as Int, updatedAt)
            "weight" -> dailyRecordDao.updateWeight(date, value as Double, updatedAt)
            else -> throw IllegalArgumentException("Invalid field: $field")
        }
    }

    suspend fun getRecentRecords(days: Int): List<DailyRecord> {
        return dailyRecordDao.getRecordsForPeriod(days).sortedByDescending { it.date }
    }

    suspend fun deleteRecord(date: String) {
        dailyRecordDao.deleteRecord(date)
    }
}