package com.kisayo.bloodsugarrecord.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.kisayo.bloodsugarrecord.R
import com.kisayo.bloodsugarrecord.data.dao.InsulinRecordDao
import com.kisayo.bloodsugarrecord.data.database.InsulinDatabase
import com.kisayo.bloodsugarrecord.data.model.DailyRecord
import com.kisayo.bloodsugarrecord.data.model.InsulinInjection
import com.kisayo.bloodsugarrecord.databinding.ItemDailyRecordBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RecordsAdapter(
    private val records: List<DailyRecord>,
    private val context: Context,
    private val onNotesClickListener: (DailyRecord) -> Unit,
    private val onDeleteClickListener: (DailyRecord, Int?) -> Unit
) : RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {

    private val insulinRecordDao: InsulinRecordDao = InsulinDatabase.getDatabase(context).insulinRecordDao()

    inner class RecordViewHolder(val binding: ItemDailyRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val lifecycleScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        var collectJob: Job? = null

        fun bind(record: DailyRecord, insulinInjection: InsulinInjection? ) {
            // 날짜 설정
            binding.tvDate.text = record.date

            // 공복 혈당 색상 설정
            binding.tvFastingValue.text = record.fasting?.let { "$it" } ?: " -- "
            binding.tvFastingValue.setTextColor(getFastingBloodSugarColor(record.fasting))

            // 아침 식전/식후
            binding.tvBreakfastBeforeValue.text = record.breakfastBefore?.let { "$it" } ?: " -- "
            binding.tvBreakfastBeforeValue.setTextColor(getBeforeBloodSugarColor(record.breakfastBefore))

            binding.tvBreakfastAfterValue.text = record.breakfastAfter?.let { "$it" } ?: " -- "
            binding.tvBreakfastAfterValue.setTextColor(getAfterBloodSugarColor(record.breakfastAfter))

            // 점심 식전/식후
            binding.tvLunchBeforeValue.text = record.lunchBefore?.let { "$it" } ?: " -- "
            binding.tvLunchBeforeValue.setTextColor(getBeforeBloodSugarColor(record.lunchBefore))

            binding.tvLunchAfterValue.text = record.lunchAfter?.let { "$it" } ?: " -- "
            binding.tvLunchAfterValue.setTextColor(getAfterBloodSugarColor(record.lunchAfter))

            // 저녁 식전/식후
            binding.tvDinnerBeforeValue.text = record.dinnerBefore?.let { "$it" } ?: " -- "
            binding.tvDinnerBeforeValue.setTextColor(getBeforeBloodSugarColor(record.dinnerBefore))

            binding.tvDinnerAfterValue.text = record.dinnerAfter?.let { "$it" } ?: " -- "
            binding.tvDinnerAfterValue.setTextColor(getAfterBloodSugarColor(record.dinnerAfter))

            // 체중
            binding.tvWeight.text = record.weight?.let { "$it kg" } ?: " -- "

            binding.tvInsulin.text = insulinInjection?.injection_amount?.let { "$it IU" } ?: "--"

            // 특이사항 칩
            binding.chipNotes.apply {
                if (record.notes.isNullOrBlank()) {
                    text = "특이사항 추가"
                    setChipBackgroundColorResource(R.color.chip_background_default)
                    setTextColor(ContextCompat.getColor(context, R.color.chip_text_default))
                } else {
                    text = "특이사항"
                    setChipBackgroundColorResource(R.color.chip_background_notes)
                    setTextColor(ContextCompat.getColor(context, R.color.chip_text_notes))
                }

                setOnClickListener {
                    onNotesClickListener(record)
                }

            }
            // 삭제 버튼 클릭 리스너
            binding.btnDelete.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("기록 삭제")
                    .setMessage("이 날짜의 기록을 삭제하시겠습니까?")
                    .setPositiveButton("네") { _, _ ->
                        onDeleteClickListener(record, insulinInjection?.injection_amount)
                        val context = itemView.context
                        Toast.makeText(context, "선택한 날짜의 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("아니요", null)
                    .show().also { dialog ->
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                            ContextCompat.getColor(itemView.context, R.color.strong_blue)
                        )
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                            ContextCompat.getColor(itemView.context, R.color.strong_blue)
                        )
                    }
            }
        }

        private fun getFastingBloodSugarColor(value: Int?): Int {
            val context = itemView.context
            return when {
                value == null -> ContextCompat.getColor(context, R.color.text_default)
                value < 70 -> ContextCompat.getColor(context, R.color.status_low)
                value in 70..125 -> ContextCompat.getColor(context, R.color.status_normal)
                value in 126..180 -> ContextCompat.getColor(context, R.color.status_warning)
                else -> ContextCompat.getColor(context, R.color.status_danger)
            }
        }

        private fun getBeforeBloodSugarColor(value: Int?): Int {
            val context = itemView.context
            return when {
                value == null -> ContextCompat.getColor(context, R.color.text_default)
                value < 70 -> ContextCompat.getColor(context, R.color.status_low)
                value in 70..125 -> ContextCompat.getColor(context, R.color.status_normal)
                value in 126..180 -> ContextCompat.getColor(context, R.color.status_warning)
                else -> ContextCompat.getColor(context, R.color.status_danger)
            }
        }

        private fun getAfterBloodSugarColor(value: Int?): Int {
            val context = itemView.context
            return when {
                value == null -> ContextCompat.getColor(context, R.color.text_default)
                value < 70 -> ContextCompat.getColor(context, R.color.status_low)
                value in 70..125 -> ContextCompat.getColor(context, R.color.status_normal)
                value in 126..180 -> ContextCompat.getColor(context, R.color.status_warning)
                else -> ContextCompat.getColor(context, R.color.status_danger)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemDailyRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        val date = record.date

        // 이전 collectJob이 있으면 취소
        holder.collectJob?.cancel()

        // 새로운 collectJob 생성
        holder.collectJob = holder.lifecycleScope.launch {
            insulinRecordDao.getInjectionsByDate(date).collect { injections ->
                val injection = injections.firstOrNull()
                holder.bind(record, injection)
            }
        }
    }

    override fun getItemCount() = records.size
}
