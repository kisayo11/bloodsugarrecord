package com.kisayo.bloodsugarrecord.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kisayo.bloodsugarrecord.data.model.MedicalRecord
import com.kisayo.bloodsugarrecord.databinding.FragmentMediaddBinding
import com.kisayo.bloodsugarrecord.viewmodel.MedicalRecordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MediAddFragment : Fragment() {
    private var _binding: FragmentMediaddBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MedicalRecordViewModel

    private var isEditMode = false
    private var visitDate: String? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // bundle에서 데이터 받기
            isEditMode = arguments?.getBoolean("isEdit") ?: false
            visitDate = arguments?.getString("visitDate")
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this)[MedicalRecordViewModel::class.java]

        // 수정 모드일 경우 기존 데이터 불러오기
        if (isEditMode && visitDate != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getRecordByDate(visitDate!!)?.let { record ->
                    // 기존 데이터를 입력 필드에 설정
                    binding.apply {
                        etHospitalName.setText(record.hospitalName)
                        etDate.setText(record.visitDate)
                        etDoctorName.setText(record.doctorName)
                        etPrescription.setText(record.prescription)
                        etNotes.setText(record.notes)
                        etNextAppointment.setText(record.nextAppointment)
                    }
                }
            }
        }
        // DatePicker 설정
        setupDatePickers()

        // 저장 버튼 클릭 리스너
        binding.btnSave.setOnClickListener {
            try {
                saveRecord()
            } catch (e: Exception) {
                Log.e("MediAddFragment", "Error saving record", e)
                Toast.makeText(context, "저장 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveRecord() {
        val hospitalName = binding.etHospitalName.text.toString()
        val visitDate = binding.etDate.text.toString()
        val doctorName = binding.etDoctorName.text.toString()
        val prescription = binding.etPrescription.text?.toString()
        val notes = binding.etNotes.text?.toString()
        val nextAppointment = binding.etNextAppointment.text?.toString()

        if (hospitalName.isEmpty()) {
            binding.etHospitalName.error = "병원명을 입력해주세요"
            return
        }
        if (visitDate.isEmpty()) {
            binding.etDate.error = "진료일을 선택해주세요"
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val record = MedicalRecord(
                    hospitalName = hospitalName,
                    visitDate = visitDate,
                    doctorName = doctorName,
                    prescription = prescription,
                    notes = notes,
                    nextAppointment = nextAppointment
                )

                // 수정 모드에 따라 insert 또는 update 실행
                if (isEditMode) {
                    viewModel.update(record)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "수정되었습니다", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    viewModel.insert(record)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "저장되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }

                withContext(Dispatchers.Main) {
                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("MediAddFragment", "Error saving record", e)
                    Toast.makeText(context, "저장 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupDatePickers() {
        // Date EditText 클릭 시 DatePicker 표시
        val dateClickListener = View.OnClickListener { view ->
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                (view as EditText).setText(selectedDate)
            }, year, month, day).show()
        }

        // 각 날짜 필드에 클릭 리스너 설정
        binding.etDate.setOnClickListener(dateClickListener)
        binding.etNextAppointment.setOnClickListener(dateClickListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}