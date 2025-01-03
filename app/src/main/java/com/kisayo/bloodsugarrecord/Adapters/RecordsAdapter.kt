package com.kisayo.bloodsugarrecord.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kisayo.bloodsugarrecord.R
import com.kisayo.bloodsugarrecord.data.model.DailyRecord
import com.kisayo.bloodsugarrecord.databinding.ItemDailyRecordBinding

class RecordsAdapter(
    private val records: List<DailyRecord>,
    private val onNotesClickListener: (DailyRecord) -> Unit
) : RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {

    inner class RecordViewHolder(val binding: ItemDailyRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: DailyRecord) {
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
        holder.bind(records[position])
    }

    override fun getItemCount() = records.size
}
