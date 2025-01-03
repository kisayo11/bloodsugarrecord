package com.kisayo.bloodsugarrecord.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisayo.bloodsugarrecord.data.model.DailyRecord

@Dao
interface DailyRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: DailyRecord)

    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    fun getRecordByDate(date: String): DailyRecord?

    @Query("UPDATE daily_records SET fasting = :value, updated_at = :updatedAt WHERE date = :date")
    fun updateFasting(date: String, value: Int, updatedAt: Long)

    @Query("UPDATE daily_records SET breakfast_before = :value, updated_at = :updatedAt WHERE date = :date")
    fun updateBreakfastBefore(date: String, value: Int, updatedAt: Long)

    @Query("UPDATE daily_records SET breakfast_after = :value, updated_at = :updatedAt WHERE date = :date")
    fun updateBreakfastAfter(date: String, value: Int, updatedAt: Long)

    @Query("UPDATE daily_records SET lunch_before = :value, updated_at = :updatedAt WHERE date = :date")
    fun updateLunchBefore(date: String, value: Int, updatedAt: Long)

    @Query("UPDATE daily_records SET lunch_after = :value, updated_at = :updatedAt WHERE date = :date")
    fun updateLunchAfter(date: String, value: Int, updatedAt: Long)

    @Query("UPDATE daily_records SET dinner_before = :value, updated_at = :updatedAt WHERE date = :date")
    fun updateDinnerBefore(date: String, value: Int, updatedAt: Long)

    @Query("UPDATE daily_records SET dinner_after = :value, updated_at = :updatedAt WHERE date = :date")
    fun updateDinnerAfter(date: String, value: Int, updatedAt: Long)

    @Query("UPDATE daily_records SET weight = :value, updated_at = :updatedAt WHERE date = :date")
    fun updateWeight(date: String, value: Double, updatedAt: Long)

    @Query("SELECT * FROM daily_records ORDER BY date DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): LiveData<List<DailyRecord>>

    @Query("UPDATE daily_records SET notes = :notes, updated_at = :updatedAt WHERE date = :date")
    fun updateNotes(date: String, notes: String?, updatedAt: Long)

    @Query("SELECT * FROM daily_records ORDER BY date DESC LIMIT :periodDays")
    fun getRecordsForPeriod(periodDays: Int): List<DailyRecord>
}