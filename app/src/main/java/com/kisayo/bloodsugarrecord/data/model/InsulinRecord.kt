package com.kisayo.bloodsugarrecord.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class InsulinStockStatus {
    UNUSED, IN_USE, COMPLETED, DISCARDED
}

@Entity(tableName = "insulin_stocks")
data class InsulinStock(
    @PrimaryKey(autoGenerate = true)
    val stock_id: Long = 0,

    val insulin_type: String,  // 인슐린 종류
    val prescription_date: String,  //  인슐린 처방일
    val start_date: String,  // 투약 시작일
    val total_amount: Int, // 총 용량
    val remaining_amount: Int, // 남은 용량


    val status: String, // UNUSED, IN_USE, COMPLETED, DISCARDED

    val discard_date: String?, //폐기일
    val discard_reason: String?, //폐기 이유
    val priority: Int, // 우선순위

    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "insulin_injections",
    primaryKeys = ["date"],
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
    val stock_id: Long,
    val date: String,
    val injection_time: String,
    val injection_amount: Int,
    val injection_site: String,
    val notes: String?,
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis()
)

@Entity(tableName = "deletion_reasons")
data class DeletionReason(
    @PrimaryKey val stockId: Long,
    val reason: String
)
