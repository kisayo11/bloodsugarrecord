package com.kisayo.bloodsugarrecord.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kisayo.bloodsugarrecord.R
import com.kisayo.bloodsugarrecord.data.model.MedicalRecord
import com.kisayo.bloodsugarrecord.databinding.FragmentMedidetailBinding
import com.kisayo.bloodsugarrecord.viewmodel.MedicalRecordViewModel
import kotlinx.coroutines.launch

class MediDetailFragment : Fragment() {
    private var _binding: FragmentMedidetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MedicalRecordViewModel
    private var visitDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitDate = arguments?.getString("visitDate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedidetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MedicalRecordViewModel::class.java]

        // 데이터 로드 및 표시
        visitDate?.let { date ->  // null 체크
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getRecordByDate(date)?.let { record ->  // getRecordById -> getRecordByDate로 변경
                    displayRecord(record)
                }
            }
        }

        // 수정 버튼 클릭
        binding.btnEdit.setOnClickListener {
            visitDate?.let{ date ->
                val bundle = Bundle().apply {
                putString("visitDate", date)
                putBoolean("isEdit", true)
                }
                findNavController().navigate(
                    R.id.action_mediDetailFragment_to_mediAddFragment,
                    bundle
                )
            }
        }

        // 삭제 버튼 클릭
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun displayRecord(record: MedicalRecord) {
        binding.apply {
            tvDetailHospitalName.text = record.hospitalName
            tvDetailDate.text = record.visitDate
            tvDetailDoctorName.text = record.doctorName
            tvDetailPrescription.text = record.prescription ?: "처방 내용 없음"
            tvDetailNotes.text = record.notes ?: "노트 없음"
            tvDetailNextAppointment.text = record.nextAppointment ?: "예약 없음"
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("기록 삭제")
            .setMessage("이 진료 기록을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    visitDate?.let { date ->  // visitDate 사용
                        viewModel.deleteRecordByDate(date)  // date로 삭제
                        findNavController().navigateUp()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show().also { dialog ->
                // "삭제" 버튼 색상 변경
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.strong_blue)
                )
                // "취소" 버튼 색상 변경
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.strong_blue)
                )
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}