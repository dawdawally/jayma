package com.jayma.pos.ui.cart

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jayma.pos.databinding.DialogCheckoutBinding
import com.jayma.pos.data.repository.SaleRepository
import com.jayma.pos.ui.viewmodel.CartViewModel
import com.jayma.pos.util.printer.PrinterService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CheckoutDialogFragment : DialogFragment() {

    private var _binding: DialogCheckoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CartViewModel by viewModels({ requireActivity() })
    
    @Inject
    lateinit var saleRepository: SaleRepository

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Checkout")
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPaymentMethod()
        setupButtons()
        observeViewModel()
    }

    private fun setupPaymentMethod() {
        // TODO: Load payment methods from repository
        // For now, use default payment method (Cash = 1)
        binding.paymentMethodSpinner.setSelection(0)
    }

    private fun setupButtons() {
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.confirmButton.setOnClickListener {
            val paymentMethodId = 1 // Cash - TODO: Get from spinner
            val paymentAmount = binding.paymentAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
            val notes = binding.notesEditText.text.toString().takeIf { it.isNotBlank() }

            if (paymentAmount <= 0) {
                Toast.makeText(context, "Please enter payment amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.checkout(paymentMethodId, paymentAmount, notes)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.totalAmount.text = String.format("Total: $%.2f", state.total)
                
                // Pre-fill payment amount with total
                if (binding.paymentAmountEditText.text.isNullOrBlank()) {
                    binding.paymentAmountEditText.setText(String.format("%.2f", state.total))
                }

                // Handle checkout result
                if (!state.isLoading && state.cartItems.isEmpty() && state.error == null) {
                    // Checkout successful - print receipt
                    printReceiptAfterCheckout()
                    Toast.makeText(context, "Sale completed successfully!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }

                // Handle errors
                state.error?.let { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun printReceiptAfterCheckout() {
        // Get the last created sale and print receipt
        lifecycleScope.launch {
            try {
                // Get unsynced sales (the one we just created)
                val unsyncedSales = saleRepository.getUnsyncedSales()
                if (unsyncedSales.isNotEmpty()) {
                    val latestSale = unsyncedSales.maxByOrNull { it.createdAt }
                    latestSale?.let { sale ->
                        val saleDetails = saleRepository.getSaleDetails(sale.localId)
                        val payments = saleRepository.getPayments(sale.localId)
                        
                        val printerService = PrinterService(requireContext())
                        val result = printerService.printReceipt(
                            sale = sale,
                            saleDetails = saleDetails,
                            payments = payments
                        )
                        
                        if (!result.success) {
                            Toast.makeText(
                                context,
                                "Receipt print failed: ${result.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Failed to print receipt: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    companion object {
        fun newInstance() = CheckoutDialogFragment()
    }
}
