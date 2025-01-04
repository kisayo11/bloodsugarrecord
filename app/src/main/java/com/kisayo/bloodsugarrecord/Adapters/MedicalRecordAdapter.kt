package com.kisayo.bloodsugarrecord.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kisayo.bloodsugarrecord.data.model.MedicalRecord
import com.kisayo.bloodsugarrecord.databinding.ItemMediaddcardBinding

class MedicalRecordAdapter : ListAdapter<MedicalRecord, MedicalRecordAdapter.MedicalRecordViewHolder>(
    DiffCallback
) {
    private var onItemClickListener: ((MedicalRecord) -> Unit)? = null

    fun setOnItemClickListener(listener: (MedicalRecord) -> Unit) {
        onItemClickListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalRecordViewHolder {
        val binding = ItemMediaddcardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MedicalRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicalRecordViewHolder, position: Int) {
        val record = getItem(position)
        holder.bind(record)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(record)
        }
    }

    class MedicalRecordViewHolder(private val binding: ItemMediaddcardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: MedicalRecord) {
            binding.apply {
                tvHospital.text = record.hospitalName
                tvDate.text = record.visitDate
                tvNextAppointment.text = record.nextAppointment ?: "미정"
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<MedicalRecord>() {
            override fun areItemsTheSame(oldItem: MedicalRecord, newItem: MedicalRecord): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MedicalRecord, newItem: MedicalRecord): Boolean {
                return oldItem == newItem
            }
        }
    }
}