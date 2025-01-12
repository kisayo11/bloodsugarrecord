package com.kisayo.bloodsugarrecord.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "insulin_stocks")
data class InsulinStock(
    @PrimaryKey(autoGenerate = true)
    val stock_id: Long = 0,

    val insulin_type: String,  // 인슐린 종류
    val prescription_date: String,  //  인슐린 처방일
    val start_date: String,  // 투약 시작일
    val total_amount: Int, // 총 용량
    val remaining_amount: Int, // 남은 용량

    @ColumnInfo(defaultValue = "UNUSED")
    val status: String, // UNUSED, IN_USE, COMPLETED, DISCARDED

    val discard_date: String?, //폐기일
    val discard_reason: String?, //폐기 이유
    val priority: Int, // 우선순위

    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "insulin_injections",
    foreignKeys = [
        ForeignKey(
            entity = InsulinStock::class,
            parentColumns = ["stock_id"],
            childColumns = ["stock_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class InsulinInjection(
    @PrimaryKey(autoGenerate = true)
    val injection_id: Long = 0,

    val stock_id: Long, //InsulinStock 참조
    val date: String, // 기록한 날짜
    val injection_time: String, // 투약 시간
    val injection_amount: Int,  // 투여량
    val injection_site: String, // 주사 부위
    val notes: String?, // 특이사항

    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis()
)