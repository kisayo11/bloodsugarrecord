package com.kisayo.bloodsugarrecord.viewmodel

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kisayo.bloodsugarrecord.data.database.InsulinDatabase
import com.kisayo.bloodsugarrecord.data.model.InsulinInjection
import com.kisayo.bloodsugarrecord.data.model.InsulinStock
import com.kisayo.bloodsugarrecord.data.repository.InsulinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first


class InsulinViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val DEFAULT_PRIORITY = 1
        const val STATUS_UNUSED = "UNUSED"
        const val STATUS_IN_USE = "IN_USE"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_DISCARDED = "DISCARDED"

        // 인슐린 종류 목록 추가
        val INSULIN_TYPES = listOf(
            "속효성 인슐린",
            "중간형 인슐린",
            "지속형 인슐린",
            "혼합형 인슐린"
        )
    }

    private val repository: InsulinRepository

    private val _stocks = MutableStateFlow<List<InsulinStock>>(emptyList())
    val stocks: StateFlow<List<InsulinStock>> = _stocks.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())

    private val _currentDate = MutableStateFlow(dateFormat.format(Calendar.getInstance().time))
    val currentDate = _currentDate.asStateFlow()

    // UI 상태
    private val _isExpanded = MutableStateFlow(false)
    val isExpanded = _isExpanded.asStateFlow()

    private val _showInsulinInfoComponent = MutableStateFlow(false)
    val showInsulinInfoComponent = _showInsulinInfoComponent.asStateFlow()

    private val _showInjectionDialog = MutableStateFlow(false)
    val showInjectionDialog = _showInjectionDialog.asStateFlow()

    private val _showSiteDialog = MutableStateFlow(false)
    val showSiteDialog = _showSiteDialog.asStateFlow()

    private val _showNotesDialog = MutableStateFlow(false)
    val showNotesDialog = _showNotesDialog.asStateFlow()

    // 현재 선택된 인슐린 정보
    private val _selectedInsulinType = MutableStateFlow("")
    val selectedInsulinType = _selectedInsulinType.asStateFlow()

    // 현재 선택된 투여 부위
    private val _selectedInjectionSite = MutableStateFlow<String?>(null)
    val selectedInjectionSite = _selectedInjectionSite.asStateFlow()

    // 현재 투여량 (하루 1회)
    private val _todayInjectionAmount = MutableStateFlow<Int?>(null)
    val todayInjectionAmount = _todayInjectionAmount.asStateFlow()

    private val _dailyInjectionRecord = MutableStateFlow<List<InsulinInjection>>(emptyList())
    val dailyInjectionRecord: StateFlow<List<InsulinInjection>> = _dailyInjectionRecord.asStateFlow()

    init {
        val dao = InsulinDatabase.getDatabase(application).insulinRecordDao()
        repository = InsulinRepository(dao)
        Log.d("InsulinViewModel", "===== ViewModel Init Start =====")
        loadStocks()
        loadInsulinData(_currentDate.value)

        viewModelScope.launch {
            Log.d("InsulinViewModel", "Start: Flow chain 설정")
            currentDate.collect { date ->
                Log.d("InsulinViewModel", " 1.currentDate collect 감지: $date")
                repository.getInjectionsByDate(date).collect { records ->
                    Log.d("InsulinViewModel", "2.getInjectionsByDate Flow collect")
                    Log.d("InsulinViewModel", "3.injection records : ${records.size}개")
                    _dailyInjectionRecord.value = records
                    if (records.isNotEmpty()) {
                        Log.d("InsulinViewModel", "4.records, dating")
                        _todayTotalAmount.value = records.first().injection_amount
                        _selectedInjectionSite.value = records.first().injection_site
                    } else {
                        Log.d("InsulinViewModel", "4.records empty, 초기화")
                        _todayTotalAmount.value = 0
                        _selectedInjectionSite.value = null
                    }
                }
            }
        }
    }

    private fun loadStocks() {
        viewModelScope.launch {
            repository.getAllStocks().collect { stocksList ->
                _stocks.value = stocksList
                Log.d("ViewModel", "Loaded stocks: ${_stocks.value}")

            }
        }
    }


    private val _currentStock = MutableStateFlow<InsulinStock?>(null)
    val currentStock = _currentStock.asStateFlow()

    private val _todayTotalAmount = MutableStateFlow(0)
    val todayTotalAmount = _todayTotalAmount.asStateFlow()

    private fun getCurrentDate(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    private fun loadInsulinData(date: String) {
        viewModelScope.launch {
            repository.getInUseStock().collect { stock ->
                _currentStock.value = stock
                Log.d("ViewModel", "Current stock: ${_currentStock.value}")
            }
        }
    }

    fun updateDate(newDate: String) {
        Log.d("InsulinViewModel", "updateDate 호출됨: $newDate")
        viewModelScope.launch {
            _currentDate.value = newDate
            _todayTotalAmount.value = 0
            _selectedInjectionSite.value = null
            Log.d("InsulinViewModel", "currentDate 값 변경됨: ${_currentDate.value}")
        }
    }


    fun setSelectedInsulinType(type: String) {
        _selectedInsulinType.value = type
    }

    fun insertStock(
        insulinType: String,
        totalAmount: Int,
        prescriptionDate: String,
        startDate: String
    ) = viewModelScope.launch {
        val stock = InsulinStock(
            insulin_type = insulinType,
            total_amount = totalAmount,
            remaining_amount = totalAmount,
            prescription_date = prescriptionDate,
            start_date = startDate,
            status = STATUS_IN_USE,
            priority = DEFAULT_PRIORITY,
            discard_date = null,
            discard_reason = null
        )
        repository.insertStock(stock)
        _showInsulinInfoComponent.value = false
    }

    fun recordInjection(amount: Int, site: String?, notes: String? = null) = viewModelScope.launch {
        currentStock.value?.let { stock ->
            // 현재 날짜의 기존 데이터를 한 번만 가져오기 위해 first() 사용
            val currentRecord = try {
                repository.getInjectionsByDate(getCurrentDate()).first().firstOrNull()
            } catch (e: Exception) {
                Log.e("InsulinViewModel", "기존 데이터 조회 실패", e)
                null
            }

            val injection = InsulinInjection(
                stock_id = stock.stock_id,
                date = getCurrentDate(),
                injection_time = getCurrentTime(),
                injection_amount = amount,
                injection_site = site ?: currentRecord?.injection_site ?: "",
                notes = notes
            )

            repository.insertInjection(injection)
            Log.d("InsulinViewModel", "새로운 주입 기록 추가 완료")

            // 남은 용량 업데이트
            val newAmount = stock.remaining_amount - amount
            repository.updateRemainingAmount(stock.stock_id, newAmount)

            _todayTotalAmount.value = amount
        } ?: Log.e("InsulinViewModel", "currentStock이 null입니다")

        _showInjectionDialog.value = false
    }

    // UI 이벤트 핸들러들
    fun onExpandClick() {
        _isExpanded.value = !_isExpanded.value
    }

    fun onInsulinInfoComponent() {
        _showInsulinInfoComponent.value = true
    }

    fun onInsulinInfoComponentDismiss() {
        _showInsulinInfoComponent.value = false
    }

    fun onInjectionAmountClick() {
        _showInjectionDialog.value = true
    }

    fun onInjectionDialogDismiss() {
        _showInjectionDialog.value = false
    }

    fun onInjectionSiteClick() {
        _showSiteDialog.value = true
    }

    fun onSiteDialogDismiss() {
        _showSiteDialog.value = false
    }

    fun onNotesClick() {
        _showNotesDialog.value = true
    }

    fun onNotesDialogDismiss() {
        _showNotesDialog.value = false
    }

    fun deleteStock(stockId: String, reason: String) {
        // stockId를 Long 타입으로 변환
        val id = stockId.toLongOrNull()
        if (id != null) {
            Log.d("ViewModel", "삭제할 ID: $id")
            viewModelScope.launch {
                // 삭제 이유와 함께 전달하여 삭제
                repository.deleteStockById(id, reason)
            }
        } else {
            // 잘못된 stockId가 전달된 경우 처리
            Log.e("ViewModel", "Invalid stock ID: $stockId")
        }
    }

    fun deleteStockById(stockId: Long, deleteReason: String) {
        Log.d("ViewModel", "deleteStockById 호출, ID: $stockId, 이유: $deleteReason")
        viewModelScope.launch {
            // 실제 삭제 수행
            repository.deleteStock(stockId, deleteReason)
        }
    }

    fun updateStatus(stockId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateStatus(stockId, newStatus)
        }
    }

    fun updateInjectionSite(site: String) {
        viewModelScope.launch {
            currentStock.value?.let { stock ->
                val currentDate = getCurrentDate()
                Log.d("InsulinViewModel", "투여 부위 기록 시작 - 날짜: $currentDate, 부위: $site")

                // 새로운 주입 기록 추가
                val injection = InsulinInjection(
                    stock_id = stock.stock_id,
                    date = currentDate,
                    injection_time = getCurrentTime(),
                    injection_amount = _todayTotalAmount.value,  // 현재 투여량 유지
                    injection_site = site,
                    notes = null
                )
                repository.insertInjection(injection)
                _selectedInjectionSite.value = site
            }
        }
        onSiteDialogDismiss()
    }


}