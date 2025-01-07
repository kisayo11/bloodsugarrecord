package com.kisayo.bloodsugarrecord.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.kisayo.bloodsugarrecord.data.model.MedicalRecord


@Dao
interface MedicalRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: MedicalRecord)

    @Query("SELECT * FROM medical_records ORDER BY created_at DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<MedicalRecord>>

    @Query("SELECT * FROM medical_records ORDER BY created_at DESC")
    fun getAllRecords(): Flow<List<MedicalRecord>>

    @Update
    fun updateRecord(record: MedicalRecord)

    @Delete
    fun deleteRecord(record: MedicalRecord)

    // date로 조회
    @Query("SELECT * FROM medical_records WHERE visit_date = :date")
    fun getRecordByDate(date: String): MedicalRecord?

    // date로 삭제
    @Query("DELETE FROM medical_records WHERE visit_date = :date")
    fun deleteRecordByDate(date: String)
}