package com.jayma.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayma.pos.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SalesReportState(
    val totalSales: Double = 0.0,
    val totalTransactions: Int = 0,
    val averageSale: Double = 0.0,
    val topProduct: String? = null,
    val isLoading: Boolean = false,
    val period: ReportPeriod = ReportPeriod.TODAY
)

enum class ReportPeriod {
    TODAY, WEEK, MONTH
}

@HiltViewModel
class SalesReportViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {
    
    private val _reportState = MutableStateFlow(SalesReportState())
    val reportState: StateFlow<SalesReportState> = _reportState.asStateFlow()
    
    fun loadTodayReport() {
        loadReport(ReportPeriod.TODAY)
    }
    
    fun loadWeekReport() {
        loadReport(ReportPeriod.WEEK)
    }
    
    fun loadMonthReport() {
        loadReport(ReportPeriod.MONTH)
    }
    
    private fun loadReport(period: ReportPeriod) {
        viewModelScope.launch {
            _reportState.value = _reportState.value.copy(isLoading = true, period = period)
            
            try {
                val startTime = getStartTimeForPeriod(period)
                val allSales = saleRepository.getAllSales()
                
                // Collect sales and filter by period
                val sales = allSales.first().filter { it.createdAt >= startTime }
                
                val totalSales = sales.sumOf { it.grandTotal }
                val totalTransactions = sales.size
                val averageSale = if (totalTransactions > 0) totalSales / totalTransactions else 0.0
                
                // Find top product (simplified - would need to aggregate from sale details)
                val topProduct = "N/A" // TODO: Implement product aggregation
                
                _reportState.value = _reportState.value.copy(
                    totalSales = totalSales,
                    totalTransactions = totalTransactions,
                    averageSale = averageSale,
                    topProduct = topProduct,
                    isLoading = false
                )
            } catch (e: Exception) {
                _reportState.value = _reportState.value.copy(isLoading = false)
            }
        }
    }
    
    private fun getStartTimeForPeriod(period: ReportPeriod): Long {
        val calendar = Calendar.getInstance()
        when (period) {
            ReportPeriod.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            ReportPeriod.WEEK -> {
                calendar.add(Calendar.DAY_OF_WEEK, -7)
            }
            ReportPeriod.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
            }
        }
        return calendar.timeInMillis
    }
}
