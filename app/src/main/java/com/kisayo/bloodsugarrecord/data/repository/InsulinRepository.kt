package com.kisayo.bloodsugarrecord.data.repository

import android.util.Log
import com.kisayo.bloodsugarrecord.data.dao.InsulinRecordDao
import com.kisayo.bloodsugarrecord.data.model.DeletionReason
import com.kisayo.bloodsugarrecord.data.model.InsulinInjection
import com.kisayo.bloodsugarrecord.data.model.InsulinStock
import com.kisayo.bloodsugarrecord.data.model.InsulinStockStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class InsulinRepository(private val insulinDao: InsulinRecordDao) {
    fun getInUseStock() = insulinDao.getInUseStock()
    fun getAllStocks(): Flow<List<InsulinStock>> = insulinDao.getAllStocks()

    suspend fun insertStock(stock: InsulinStock) = withContext(Dispatchers.IO) {
        insulinDao.insertStock(stock)
    }

    suspend fun updateRemainingAmount(stockId: Long, amount: Int) = withContext(Dispatchers.IO) {
        insulinDao.updateRemainingAmount(stockId, amount, System.currentTimeMillis())
    }

    suspend fun updateStatus(stockId: Long, status: InsulinStockStatus) =
        withContext(Dispatchers.IO) {
            insulinDao.updateStatus(stockId, status, System.currentTimeMillis())
        }

    //Injection
    fun getInjectionsByDate(date: String): Flow<List<InsulinInjection>> {
        Log.d("InsulinRepository", "===== getInjectionsByDate =====")
        Log.d("InsulinRepository", "요청된 날짜: $date")
        return insulinDao.getInjectionsByDate(date)
    }

    suspend fun insertInjection(injection: InsulinInjection) = withContext(Dispatchers.IO) {
        Log.d("InsulinRepository", "===== insertInjection =====")
        Log.d("InsulinRepository", "저장 시도: 날짜=${injection.date}, 용량=${injection.injection_amount}, 부위=${injection.injection_site}")
        try {
            insulinDao.insertInjection(injection)
            Log.d("InsulinRepository", "저장 성공")
        } catch (e: Exception) {
            Log.e("InsulinRepository", "저장 실패", e)
            Log.e("InsulinRepository", "에러 메시지: ${e.message}")
        }
    }
    fun getTotalInjectionsAmount(date: String) = insulinDao.getTotalInjectionsAmountByDate(date)

    suspend fun deleteStock(stockId: Long, reason: String) = withContext(Dispatchers.IO) {
        try {
            val stock = insulinDao.getStockById(stockId)
            if (stock == null) {
                Log.d("Repository", "해당 ID의 데이터가 없음, ID: $stockId")
                return@withContext
            }

            // 상태 상관없이 삭제 진행
            Log.d("Repository", "삭제할 데이터 있음, ID: $stockId")
            insulinDao.deleteStock(stockId)
            insulinDao.deleteInjectionsByStockId(stockId)

            // 삭제 이유 저장 로직 추가
            insulinDao.insertDeletionReason(DeletionReason(stockId, reason))
            Log.d("Repository", "삭제 완료, ID: $stockId")
        } catch (e: Exception) {
            Log.e("Repository", "삭제 실패, ID: $stockId", e)
        }
    }

    suspend fun deleteStockById(id: Long, reason: String) {
        // reason을 전달하여 deleteStock을 호출
        deleteStock(id, reason)
    }

    suspend fun updateStatus(stockId: Long, newStatus: String) {
        withContext(Dispatchers.IO) {
            // DB에서 stockId에 해당하는 데이터를 찾아서 상태를 업데이트
            insulinDao.updateStatus(stockId, newStatus)
        }
    }
    suspend fun deleteInjectionsByDate(date: String) = withContext(Dispatchers.IO) {
        insulinDao.deleteInjectionsByDate(date)
    }
}