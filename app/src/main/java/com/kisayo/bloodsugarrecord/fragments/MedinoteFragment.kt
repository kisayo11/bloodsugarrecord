package com.kisayo.bloodsugarrecord.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kisayo.bloodsugarrecord.Adapters.MedicalRecordAdapter
import com.kisayo.bloodsugarrecord.R
import com.kisayo.bloodsugarrecord.databinding.FragmentMedinoteBinding
import com.kisayo.bloodsugarrecord.viewmodel.MedicalRecordViewModel
import kotlinx.coroutines.launch

class MedinoteFragment : Fragment() {
    private var _binding: FragmentMedinoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MedicalRecordViewModel
    private lateinit var adapter: MedicalRecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedinoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MedicalRecordViewModel::class.java]
        setupRecyclerView()
        setupObservers()

        binding.addRecordButton.setOnClickListener {
            findNavController().navigate(R.id.action_medinoteFragment_to_mediAddFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = MedicalRecordAdapter().apply {
            setOnItemClickListener { record ->
                val bundle = Bundle().apply {
                    putString("visitDate", record.visitDate)
                }
                findNavController().navigate(R.id.action_medinoteFragment_to_mediDetailFragment, bundle)
            }
        }
        binding.mediaddcardview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MedinoteFragment.adapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentRecords.collect { records ->
                adapter.submitList(records)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}