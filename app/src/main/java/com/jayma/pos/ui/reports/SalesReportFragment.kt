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
                
                // Update button styles based on active period
                updateButtonStyles(state.period)
            }
        }
    }
    
    private fun updateButtonStyles(activePeriod: com.jayma.pos.ui.viewmodel.ReportPeriod) {
        // Get colorPrimary from theme
        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
        val colorPrimary = typedValue.data
        val whiteColor = androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.white)
        
        // Reset all buttons to outlined style (transparent background, primary text)
        binding.todayButton.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        binding.weekButton.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        binding.monthButton.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        
        binding.todayButton.setTextColor(colorPrimary)
        binding.weekButton.setTextColor(colorPrimary)
        binding.monthButton.setTextColor(colorPrimary)
        
        // Set active button to filled style (primary background, white text)
        when (activePeriod) {
            com.jayma.pos.ui.viewmodel.ReportPeriod.TODAY -> {
                binding.todayButton.backgroundTintList = android.content.res.ColorStateList.valueOf(colorPrimary)
                binding.todayButton.setTextColor(whiteColor)
            }
            com.jayma.pos.ui.viewmodel.ReportPeriod.WEEK -> {
                binding.weekButton.backgroundTintList = android.content.res.ColorStateList.valueOf(colorPrimary)
                binding.weekButton.setTextColor(whiteColor)
            }
            com.jayma.pos.ui.viewmodel.ReportPeriod.MONTH -> {
                binding.monthButton.backgroundTintList = android.content.res.ColorStateList.valueOf(colorPrimary)
                binding.monthButton.setTextColor(whiteColor)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
