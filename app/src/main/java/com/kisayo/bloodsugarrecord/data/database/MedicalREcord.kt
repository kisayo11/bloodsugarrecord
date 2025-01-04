package com.kisayo.bloodsugarrecord.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_records")
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "hospital_name") val hospitalName: String,
    @ColumnInfo(name = "visit_date") val visitDate: String,
    @ColumnInfo(name = "doctor_name") val doctorName: String,
    @ColumnInfo(name = "prescription") val prescription: String?,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "next_appointment") val nextAppointment: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
