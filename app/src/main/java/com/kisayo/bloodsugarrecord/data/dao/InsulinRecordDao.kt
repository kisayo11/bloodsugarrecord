package com.kisayo.bloodsugarrecord.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisayo.bloodsugarrecord.data.model.InsulinInjection
import com.kisayo.bloodsugarrecord.data.model.InsulinStock

@Dao
interface InsulinRecordDao{
    //Stock
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stock: InsulinStock)

    @Query("SELECT * FROM insulin_stocks WHERE status = 'IN_USE'")
    fun getInUseStock(): InsulinStock?

    @Query("SELECT * FROM insulin_stocks ORDER BY created_at DESC")
    fun getAllStocks(): List<InsulinStock>

    @Query("UPDATE insulin_stocks SET remaining_amount = :amount, updated_at = :updatedAt WHERE stock_id = :stockId")
    fun updateRemainingAmount(stockId: Long, amount: Int, updatedAt: Long)

    @Query("UPDATE insulin_stocks SET status = :status, updated_at = :updatedAt WHERE stock_id = :stockId")
    fun updateStatus(stockId: Long, status: String, updatedAt: Long)

    //Injection
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(injection: InsulinInjection)

    @Query("SELECT * FROM insulin_injections WHERE date = :date")
    fun getInjectionsByDate(date: String): List<InsulinInjection>

    @Query("SELECT * FROM insulin_injections WHERE stock_id = :stockId")
    fun getInjectionsByStockId(stockId: Long): List<InsulinInjection>

    @Query("SELECT * FROM insulin_injections ORDER BY date DESC, injection_time DESC")
    fun getAllInjections(): List<InsulinInjection>

}