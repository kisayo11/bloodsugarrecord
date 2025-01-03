package com.kisayo.bloodsugarrecord.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey val date: String,
    @ColumnInfo(name = "fasting") val fasting: Int? = null,
    @ColumnInfo(name = "breakfast_before") val breakfastBefore: Int? = null,
    @ColumnInfo(name = "breakfast_after") val breakfastAfter: Int? = null,
    @ColumnInfo(name = "lunch_before") val lunchBefore: Int? = null,
    @ColumnInfo(name = "lunch_after") val lunchAfter: Int? = null,
    @ColumnInfo(name = "dinner_before") val dinnerBefore: Int? = null,
    @ColumnInfo(name = "dinner_after") val dinnerAfter: Int? = null,
    @ColumnInfo(name = "weight") val weight: Double? = null,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)