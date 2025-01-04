package com.kisayo.bloodsugarrecord.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kisayo.bloodsugarrecord.Adapters.RecordsAdapter
import com.kisayo.bloodsugarrecord.R
import com.kisayo.bloodsugarrecord.data.model.DailyRecord
import com.kisayo.bloodsugarrecord.databinding.FragmentRecordsBinding
import com.kisayo.bloodsugarrecord.viewmodel.RecordsViewModel

class RecordsFragment : Fragment() {
    private lateinit var binding: FragmentRecordsBinding
    private lateinit var recordsAdapter: RecordsAdapter
    private val viewModel: RecordsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvDailyRecords.layoutManager = LinearLayoutManager(requireContext())

        viewModel.getRecentRecords(5).observe(viewLifecycleOwner) { records ->
            if (records.isEmpty()) {
                binding.tvNoRecords.visibility = View.VISIBLE
                binding.rvDailyRecords.visibility = View.GONE
            } else {
                binding.tvNoRecords.visibility = View.GONE
                binding.rvDailyRecords.visibility = View.VISIBLE
                recordsAdapter = RecordsAdapter(
                    records = records,
                    onNotesClickListener = { record -> showNotesDialog(record) },
                    onDeleteClickListener = { record -> viewModel.deleteRecord(record.date) }
                )
                binding.rvDailyRecords.adapter = recordsAdapter
            }
        }
    }

    private fun showNotesDialog(record: DailyRecord) {
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_notes, null)
        val editText = dialogView.findViewById<EditText>(R.id.etNotes)

        dialog.setView(dialogView)
        dialog.setTitle("특이사항")

        editText.setText(record.notes ?: "")

        dialog.setPositiveButton("저장") { _, _ ->
            val notes = editText.text.toString().trim()
            if (notes.length <= 30) {
                viewModel.updateNotes(record.date, notes)
            } else {
                Toast.makeText(context, "30자 이내로 작성해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setNegativeButton("취소", null)
        dialog.show()
    }
}