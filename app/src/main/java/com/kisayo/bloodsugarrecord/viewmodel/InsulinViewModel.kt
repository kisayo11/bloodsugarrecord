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
            Log.d("InsulinViewModel", "Starting Flow chain setup")
            currentDate.collect { date ->
                Log.d("InsulinViewModel", "1. Date change detected: $date")
                repository.getInjectionsByDate(date).collect { records ->
                    Log.d("InsulinViewModel", "2. Fetched records for date: $date")
                    Log.d("InsulinViewModel", "3. Found ${records.size} injection records")
                    _dailyInjectionRecord.value = records
                    if (records.isNotEmpty()) {
                        Log.d("InsulinViewModel", "4. Records exist - updating UI values")
                        records.firstOrNull()?.let { record ->
                            _todayTotalAmount.value = record.injection_amount
                            _selectedInjectionSite.value = record.injection_site
                            Log.d("InsulinViewModel", "5. Updated - Amount: ${record.injection_amount}u, Site: ${record.injection_site}")
                        }
                    } else {
                        Log.d("InsulinViewModel", "4. No records found - resetting values")
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
        Log.d("InsulinViewModel", "updateDate called: $newDate")
        viewModelScope.launch {
            _currentDate.value = newDate
            // Flow가 새로 collect되는지 확인하기 위해 로그 추가
            repository.getInjectionsByDate(newDate).collect { records ->
                Log.d("InsulinViewModel", "New date records fetched: ${records.size} for date $newDate")
                _dailyInjectionRecord.value = records
                if (records.isNotEmpty()) {
                    _todayTotalAmount.value = records.first().injection_amount
                    _selectedInjectionSite.value = records.first().injection_site
                    Log.d("InsulinViewModel", "Data updated - Amount: ${records.first().injection_amount}u, Site: ${records.first().injection_site}")
                } else {
                    _todayTotalAmount.value = 0
                    _selectedInjectionSite.value = null
                    Log.d("InsulinViewModel", "No records found for date: $newDate")
                }
            }
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

    fun updateInjectionAmount(amount: Int) = viewModelScope.launch {
        currentStock.value?.let { stock ->
            val selectedDate = _currentDate.value
            val currentRecord = repository.getInjectionsByDate(selectedDate).first().firstOrNull()

            val injection = InsulinInjection(
                stock_id = stock.stock_id,
                date = selectedDate,
                injection_time = getCurrentTime(),
                injection_amount = amount,
                injection_site = currentRecord?.injection_site ?: "",
                notes = currentRecord?.notes
            )
            repository.insertInjection(injection)
            loadInjectionData(selectedDate)  // 데이터 다시 로드
        }
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

    fun updateInjectionSite(site: String) = viewModelScope.launch {
        currentStock.value?.let { stock ->
            val selectedDate = _currentDate.value
            val currentRecord = repository.getInjectionsByDate(selectedDate).first().firstOrNull()

            val injection = InsulinInjection(
                stock_id = stock.stock_id,
                date = selectedDate,
                injection_time = getCurrentTime(),
                injection_amount = currentRecord?.injection_amount ?: 0,
                injection_site = site,
                notes = currentRecord?.notes
            )
            repository.insertInjection(injection)

            // 데이터 다시 로드
            loadInjectionData(selectedDate)
        }
    }
    private fun loadInjectionData(date: String) = viewModelScope.launch {
        try {
            repository.getInjectionsByDate(date).first().let { records ->  // collect 대신 first() 사용
                _dailyInjectionRecord.value = records
                if (records.isNotEmpty()) {
                    _todayTotalAmount.value = records.first().injection_amount
                    _selectedInjectionSite.value = records.first().injection_site
                }
            }
        } catch (e: Exception) {
            Log.e("InsulinViewModel", "Error loading injection data", e)
        }
    }

}