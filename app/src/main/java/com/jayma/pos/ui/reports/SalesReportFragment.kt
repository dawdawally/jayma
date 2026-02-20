package com.jayma.pos.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.jayma.pos.databinding.FragmentSalesReportBinding
import com.jayma.pos.ui.viewmodel.SalesReportViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SalesReportFragment : Fragment() {
    
    private var _binding: FragmentSalesReportBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SalesReportViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesReportBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        observeViewModel()
        setupButtons()
        
        // Load today's report by default
        viewModel.loadTodayReport()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload report when fragment becomes visible (e.g., after checkout)
        viewModel.loadTodayReport()
    }
    
    private fun setupButtons() {
        binding.todayButton.setOnClickListener {
            viewModel.loadTodayReport()
        }
        
        binding.weekButton.setOnClickListener {
            viewModel.loadWeekReport()
        }
        
        binding.monthButton.setOnClickListener {
            viewModel.loadMonthReport()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.reportState.collect { state ->
                binding.totalSales.text = String.format("$%.2f", state.totalSales)
                binding.totalTransactions.text = state.totalTransactions.toString()
                binding.averageSale.text = String.format("$%.2f", state.averageSale)
                binding.topProduct.text = state.topProduct ?: "N/A"
                
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
