package com.kisayo.bloodsugarrecord.ui.insulin.components

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kisayo.bloodsugarrecord.viewmodel.InsulinViewModel
import java.util.Date


@Composable
fun InsulinInfoComponent(
    show: Boolean,
    onDismiss: () -> Unit,
    viewModel: InsulinViewModel,
    onSave: (type: String, amount: Int, prescriptionDate: String, startDate: String) -> Unit
) {
    if (show) {
        Dialog(
            onDismissRequest = onDismiss, properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
            ) {
                InsulinManagementScreen(
                    viewModel = viewModel, onDismiss = onDismiss, onSave = onSave
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InsulinManagementScreen(
    viewModel: InsulinViewModel,
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Int, prescriptionDate: String, startDate: String) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("인슐린 관리", "새 인슐린 입력")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(title = { Text("인슐린 관리") }, navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "닫기")
            }
        })

        // Tabs
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) })
            }
        }

        // Content
        when (selectedTabIndex) {
            0 -> InsulinListContent(viewModel)
            1 -> InsulinInputContent(viewModel = viewModel,
                onSave = { type, amount, prescriptionDate, startDate ->
                    onSave(type, amount, prescriptionDate, startDate)
                    onDismiss()
                })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsulinInputContent(
    viewModel: InsulinViewModel,
    onSave: (type: String, amount: Int, prescriptionDate: String, startDate: String) -> Unit
) {
    var insulinType by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var prescriptionDate by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }


    // DatePicker 상태
    var showPrescriptionDatePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 인슐린 종류 선택
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(value = insulinType,
                onValueChange = { },
                label = { Text("인슐린 종류") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                InsulinViewModel.INSULIN_TYPES.forEach { type ->
                    DropdownMenuItem(text = { Text(type) }, onClick = {
                        insulinType = type
                        expanded = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = totalAmount,
            onValueChange = { if (it.all { char -> char.isDigit() }) totalAmount = it },
            label = { Text("총 용량 (u)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 처방일 선택
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { showPrescriptionDatePicker = true }  // 클릭시 날짜 선택기 열기
            .border(1.dp, Color.Black) // 테두리 추가
            .padding(16.dp)  // 여백 추가
        ) {
            Text(
                text = prescriptionDate.ifEmpty { "처방일 선택" },  // 날짜가 없을 경우 기본 텍스트
                color = if (prescriptionDate.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.6f
                ) else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 시작일 선택
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { showStartDatePicker = true }
            .border(1.dp, Color.Black)
            .padding(16.dp)) {
            Text(
                text = startDate.ifEmpty { "시작일 선택" },  // 날짜가 없을 경우 기본 텍스트
                color = if (startDate.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.6f
                ) else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val amount = totalAmount.toIntOrNull() ?: 0
                onSave(insulinType, amount, prescriptionDate, startDate)
                Toast.makeText(context, "인슐린 정보가 저장 되었습니다.", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.fillMaxWidth()
        ) {
            Text("저장하기")
        }
    }

    // DatePicker Dialogs
    if (showPrescriptionDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showPrescriptionDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        prescriptionDate = dateFormat.format(Date(millis))
                    }
                    showPrescriptionDatePicker = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrescriptionDatePicker = false }) {
                    Text("취소")
                }
            }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showStartDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    startDate = dateFormat.format(Date(millis))
                }
                showStartDatePicker = false
            }) {
                Text("확인")
            }
        }, dismissButton = {
            TextButton(onClick = { showStartDatePicker = false }) {
                Text("취소")
            }
        }) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun InsulinListContent(viewModel: InsulinViewModel) {
    val stocks = viewModel.stocks.collectAsState().value
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var selectedStockId by remember { mutableStateOf<String?>(null) }
    var deleteReason by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") }



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(stocks.size) { index ->
            val stock = stocks[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stock.insulin_type, style = MaterialTheme.typography.titleMedium
                        )
                        AssistChip(onClick = {
                            val newStatus = when (stock.status) {
                                "IN_USE" -> "COMPLETED"
                                "COMPLETED" -> "DISCARDED"
                                "DISCARDED" -> "UNUSED"
                                else -> "IN_USE"
                            }
                            selectedStatus = newStatus
                            viewModel.updateStatus(stock.stock_id, newStatus)
                        }, label = {
                            Text(
                                when (stock.status) {
                                    "IN_USE" -> "사용중"
                                    "COMPLETED" -> "완료"
                                    "DISCARDED" -> "폐기"
                                    else -> "미사용"
                                }
                            )
                        }, colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (stock.status) {
                                "IN_USE" -> MaterialTheme.colorScheme.primaryContainer
                                "COMPLETED" -> MaterialTheme.colorScheme.secondary
                                "DISCARDED" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("처방일: ${stock.prescription_date}")
                    Text("투약 시작일: ${stock.start_date}")
                    Text("총 용량: ${stock.total_amount}IU")
                    Text("남은 용량: ${stock.remaining_amount}IU")

                    // 삭제 버튼
                    IconButton(
                        onClick = {
                            selectedStockId = stock.stock_id.toString()
                            showDialog = true
                        }, modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제",
                            tint = MaterialTheme.colorScheme.error // 빨간색
                        )
                    }
                }
            }
        }
    }

    // 삭제 확인 다이얼로그
    if (showDialog) {
        AlertDialog(onDismissRequest = {}, title = { Text("삭제하시겠습니까?") }, icon = {
            Icon(
                imageVector = Icons.Default.Warning, contentDescription = "경고 아이콘"

            )
        }, text = {
            // 삭제 이유 입력 필드
            OutlinedTextField(value = deleteReason,
                onValueChange = { deleteReason = it },
                label = { Text("삭제 이유를 작성해주세요") },
                modifier = Modifier.fillMaxWidth()
            )
        }, confirmButton = {
            TextButton(onClick = {
                // selectedStockId를 Long 타입으로 변환
                val longStockId = selectedStockId?.toLongOrNull()
                if (longStockId != null) {
                    // 삭제 이유와 함께 삭제 실행
                    viewModel.deleteStockById(longStockId, deleteReason)
                } else {
                    // stockId가 Long으로 변환되지 않으면 처리할 로직
                    Log.e("Delete", "Invalid stock ID")
                }
                showDialog = false
            }) {
                Text("확인")
            }
        }, dismissButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("취소")
            }
        })
    }
}

@Preview
@Composable
fun PreviewInsulinInfoComponent() {
    var showDialog by remember { mutableStateOf(true) }
    val mockViewModel = remember { InsulinViewModel(Application()) }

    InsulinInfoComponent(show = showDialog,
        onDismiss = { showDialog = false },
        viewModel = mockViewModel,
        onSave = { _, _, _, _ -> })
}