package com.jayma.pos.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jayma.pos.databinding.FragmentPrinterSettingsBinding
import com.jayma.pos.util.printer.PrinterService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrinterSettingsFragment : Fragment() {
    
    private var _binding: FragmentPrinterSettingsBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrinterSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupButtons()
        checkPrinterStatus()
    }
    
    private fun setupButtons() {
        binding.testPrintButton.setOnClickListener {
            printTestPage()
        }
        
        binding.checkStatusButton.setOnClickListener {
            checkPrinterStatus()
        }
    }
    
    private fun checkPrinterStatus() {
        val printerService = PrinterService(requireContext())
        val status = printerService.checkPrinterStatus()
        
        binding.statusText.text = "Status: $status"
        
        when (status) {
            com.jayma.pos.util.printer.PrinterStatus.READY -> {
                binding.statusText.setTextColor(
                    requireContext().getColor(android.R.color.holo_green_dark)
                )
            }
            com.jayma.pos.util.printer.PrinterStatus.OUT_OF_PAPER -> {
                binding.statusText.setTextColor(
                    requireContext().getColor(android.R.color.holo_red_dark)
                )
                Toast.makeText(context, "Printer is out of paper", Toast.LENGTH_SHORT).show()
            }
            com.jayma.pos.util.printer.PrinterStatus.OVERHEATED -> {
                binding.statusText.setTextColor(
                    requireContext().getColor(android.R.color.holo_orange_dark)
                )
                Toast.makeText(context, "Printer is overheated", Toast.LENGTH_SHORT).show()
            }
            com.jayma.pos.util.printer.PrinterStatus.ERROR -> {
                binding.statusText.setTextColor(
                    requireContext().getColor(android.R.color.holo_red_dark)
                )
                Toast.makeText(context, "Printer error", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun printTestPage() {
        val printerService = PrinterService(requireContext())
        val result = printerService.printTestPage()
        
        if (result.success) {
            Toast.makeText(context, "Test page printed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Print failed: ${result.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
