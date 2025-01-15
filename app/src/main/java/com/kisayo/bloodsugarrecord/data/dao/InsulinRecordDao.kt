package com.kisayo.bloodsugarrecord.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisayo.bloodsugarrecord.data.model.DeletionReason
import com.kisayo.bloodsugarrecord.data.model.InsulinInjection
import com.kisayo.bloodsugarrecord.data.model.InsulinStock
import com.kisayo.bloodsugarrecord.data.model.InsulinStockStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InsulinRecordDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStock(stock: InsulinStock)

    @Query("SELECT * FROM insulin_stocks WHERE status = 'IN_USE' LIMIT 1")
    fun getInUseStock(): Flow<InsulinStock?>


    @Query("SELECT * FROM insulin_stocks ORDER BY created_at DESC")
    fun getAllStocks(): Flow<List<InsulinStock>>

    @Query("UPDATE insulin_stocks SET remaining_amount = :amount, updated_at = :updatedAt WHERE stock_id = :stockId")
    fun updateRemainingAmount(stockId: Long, amount: Int, updatedAt: Long)

    @Query("UPDATE insulin_stocks SET status = :status, updated_at = :updatedAt WHERE stock_id = :stockId")
    fun updateStatus(stockId: Long, status: InsulinStockStatus, updatedAt: Long)

    //Injection
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInjection(injection: InsulinInjection)

    @Query("SELECT * FROM insulin_injections WHERE date = :date")
    fun getInjectionsByDate(date: String): Flow<List<InsulinInjection>>

    @Query("SELECT * FROM insulin_injections WHERE stock_id = :stockId")
    fun getInjectionsByStockId(stockId: Long): Flow<List<InsulinInjection>>

    @Query("SELECT * FROM insulin_injections ORDER BY date DESC, injection_time DESC")
    fun getAllInjections(): Flow<List<InsulinInjection>>

    @Query("SELECT SUM(injection_amount) FROM insulin_injections WHERE date = :date")
    fun getTotalInjectionsAmountByDate(date: String): Flow<Int?>

    @Query("DELETE FROM insulin_injections WHERE date = :date")
    fun deleteInjectionsByDate(date: String): Int

    @Delete
    fun deleteInjection(injection: InsulinInjection)

    @Query("SELECT * FROM insulin_stocks WHERE stock_id = :stockId")
    fun getStockById(stockId: Long): InsulinStock?

    @Query("DELETE FROM insulin_stocks WHERE stock_id = :stockId")
    fun deleteStock(stockId: Long)

    @Query("DELETE FROM insulin_injections WHERE stock_id = :stockId")
    fun deleteInjectionsByStockId(stockId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDeletionReason(deletionReason: DeletionReason)

    @Query("UPDATE insulin_stocks SET status = :newStatus WHERE stock_id = :stockId")
    fun updateStatus(stockId: Long, newStatus: String)

    @Query("SELECT injection_site FROM insulin_injections WHERE date = :date")
    fun getInjectionSitesByDate(date: String): Flow<List<String>>

}