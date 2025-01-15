package com.kisayo.bloodsugarrecord.ui.insulin.components

import android.app.Application
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.unit.dp
import com.kisayo.bloodsugarrecord.ui.theme.AppColor
import com.kisayo.bloodsugarrecord.viewmodel.InsulinViewModel




@Composable
fun InsulinCard(
    modifier: Modifier = Modifier,
    viewModel: InsulinViewModel
) {
    val isExpanded by viewModel.isExpanded.collectAsState(false)
    val currentStock by viewModel.currentStock.collectAsState()
    val todayTotalAmount by viewModel.todayTotalAmount.collectAsState(0)
    Log.d("InsulinCard", "Composable recomposition - todayTotalAmount: $todayTotalAmount")

    val showInsulinInfoComponent by viewModel.showInsulinInfoComponent.collectAsState(false)
    val showInjectionDialog by viewModel.showInjectionDialog.collectAsState()
    val showSiteDialog by viewModel.showSiteDialog.collectAsState(false)
    val showNotesDialog by viewModel.showNotesDialog.collectAsState(false)
    val currentDate by viewModel.currentDate.collectAsState()
    val stocks by viewModel.stocks.collectAsState()
    val selectedInjectionSite by viewModel.selectedInjectionSite.collectAsState()
    Log.d("InsulinCard", "Composable recomposition - selectedInjectionSite: $selectedInjectionSite")

    val dailyinjectionRecords by viewModel.dailyInjectionRecord.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        color = AppColor.InsulinCardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 헤더 섹션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "인슐린 관리 💉",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
                Text(  // 여기에 currentDate 표시 추가
                    text = currentDate,
                    color = AppColor.InsulinCardText
                )
                // 확장/축소 아이콘 버튼
                val rotationState by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "rotation"
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.onExpandClick() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isExpanded) "닫기" else "펼치기",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColor.InsulinCardText
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Details",
                        modifier = Modifier.padding(start = 8.dp),
                        tint = AppColor.InsulinCardText
                    )
                }
            }

            // 확장 가능한 상세 정보 섹션
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // 투여 정보 섹션
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.onInjectionAmountClick() },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "오늘 투여용량",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${todayTotalAmount}u",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = AppColor.InsulinCardText,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                                Text(
                                    text = "터치하여 기록",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.LightGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.onInjectionSiteClick() },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "투여 부위",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Black
                                )
                                Text(
                                    text = selectedInjectionSite ?: "--",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = AppColor.InsulinCardText,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Text(
                                    text = "터치하여 선택",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 처방 및 재고 정보
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // 처방일과 투약 시작일
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "처방일",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Black
                                )
                                Text(
                                    text = currentStock?.prescription_date ?: "--",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "투약 시작일",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Black
                                )
                                Text(
                                    text = currentStock?.start_date ?: "--",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                        }

                        Divider(
                            color = AppColor.InsulinCardText,
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 하단: 남은 용량과 총 용량
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "남은 용량",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${currentStock?.remaining_amount ?: 0}u",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColor.InsulinCardText
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "총 용량",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${currentStock?.total_amount ?: 0}u",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColor.InsulinCardText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ProgressBar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColor.InsulinCardBar.copy(alpha = 0.3f))
                        ) {
                            // 프로그래스 바
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(
                                        (currentStock?.remaining_amount?.toFloat()
                                            ?: 0f) / (currentStock?.total_amount?.toFloat() ?: 1f)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColor.InsulinCardBar)
                            )
                            // 남은 용량 텍스트
                            Text(
                                text = "${currentStock?.remaining_amount ?: 0}u",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }



                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AssistChip(
                                onClick = { viewModel.onInsulinInfoComponent() },
                                label = {
                                    Text(
                                        text = "인슐린 정보 입력",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = AppColor.InsulinCardBtn,
                                    labelColor = Color.White
                                ),
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            AssistChip(
                                onClick = { viewModel.onNotesClick() },
                                label = {
                                    Text(
                                        text = "특이사항",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = AppColor.InsulinCardBtn,
                                    labelColor = Color.White
                                ),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    // 다이얼로그들
    if (showInsulinInfoComponent) {
        InsulinInfoComponent(
            show = showInsulinInfoComponent,
            onDismiss = { viewModel.onInsulinInfoComponentDismiss() },
            viewModel = viewModel,  // viewModel 파라미터 추가
            onSave = { type, amount, prescriptionDate, startDate ->
                viewModel.insertStock(type, amount, prescriptionDate, startDate)
            }
        )
    }

    if (showInjectionDialog) {
        InjectionDialog(
            onDismiss = { viewModel.onInjectionDialogDismiss() },
            onConfirm = { amount, site, notes ->
                viewModel.recordInjection(amount, site.toString(), notes.toString())
            }
        )
    }
    if (showSiteDialog) {
        InjectionSiteDialog(
            onDismiss = { viewModel.onSiteDialogDismiss() },
            onSelect = { site ->
                viewModel.updateInjectionSite(site)

            }
        )
    }

    if (showNotesDialog) {
        NotesDialog(
            onDismiss = { viewModel.onNotesDialogDismiss() },
            onConfirm = { notes ->
                // 특이사항 처리
                viewModel.onNotesDialogDismiss()
            }
        )
    }
}

// 투여량 입력 다이얼로그
@Composable
fun InjectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Int, Any?, Any?) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("투여량 기록", color = Color.Black) },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        amount = it
                    }
                },
                label = { Text("투여량 (u)", color = Color.Black) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val injectionAmount = amount.toIntOrNull() ?: 0
                    if (injectionAmount > 0) {
                        onConfirm(injectionAmount, null, null)
                    }
                },
                enabled = amount.isNotEmpty()
            ) {
                Text("저장", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color.Black)
            }
        }
    )
}

// 투여 부위 선택 다이얼로그
@Composable
fun InjectionSiteDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val injectionSites = listOf(
        "복부 좌측", "복부 우측",
        "허벅지 좌측", "허벅지 우측",
        "팔 좌측", "팔 우측"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("투여 부위 선택", color = Color.Black) },
        text = {
            Column {
                injectionSites.forEach { site ->
                    TextButton(
                        onClick = { onSelect(site) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(site, color = Color.Black)
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color.Black)
            }
        }
    )
}

// 특이사항 입력 다이얼로그
@Composable
fun NotesDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("특이사항", color = Color.Black) },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("내용", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(notes) }
            ) {
                Text("저장", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color.Black)
            }
        }
    )
}
