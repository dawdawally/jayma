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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jayma.pos.databinding.DialogCheckoutBinding
import com.jayma.pos.data.repository.SaleRepository
import com.jayma.pos.ui.adapter.CheckoutCartAdapter
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
    
    private lateinit var checkoutCartAdapter: CheckoutCartAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCheckoutBinding.inflate(layoutInflater)
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Checkout")
            .setView(binding.root)
            .create()
        
        return dialog
    }

    override fun onStart() {
        super.onStart()
        // Configure dialog window after it's shown
        dialog?.window?.let { window ->
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            // Ensure dialog appears on top with proper dimming
            window.setDimAmount(0.5f)
            // Ensure dialog is above everything
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // View is already created in onCreateDialog
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupPaymentMethod()
        setupButtons()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        checkoutCartAdapter = CheckoutCartAdapter(
            onIncreaseQuantity = { productId ->
                val currentQuantity = viewModel.uiState.value.cartItems
                    .find { it.product.id == productId }?.quantity ?: 0.0
                viewModel.updateQuantity(productId, currentQuantity + 1.0)
            },
            onDecreaseQuantity = { productId ->
                val currentQuantity = viewModel.uiState.value.cartItems
                    .find { it.product.id == productId }?.quantity ?: 0.0
                if (currentQuantity > 1) {
                    viewModel.updateQuantity(productId, currentQuantity - 1.0)
                } else {
                    viewModel.removeFromCart(productId)
                }
            }
        )
        
        binding.cartItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = checkoutCartAdapter
        }
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
            
            if (viewModel.uiState.value.cartItems.isEmpty()) {
                Toast.makeText(context, "Cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkoutInProgress = true
            binding.confirmButton.isEnabled = false
            binding.confirmButton.text = "Processing..."
            viewModel.checkout(paymentMethodId, paymentAmount, notes)
        }
    }

    private var checkoutInProgress = false
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Update cart items list
                checkoutCartAdapter.submitList(state.cartItems)
                
                // Show/hide empty cart message
                if (state.cartItems.isEmpty()) {
                    binding.emptyCartMessage.visibility = View.VISIBLE
                    binding.cartItemsRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyCartMessage.visibility = View.GONE
                    binding.cartItemsRecyclerView.visibility = View.VISIBLE
                }
                
                binding.totalAmount.text = String.format("Total: $%.2f", state.total)
                
                // Pre-fill payment amount with total
                if (binding.paymentAmountEditText.text.isNullOrBlank()) {
                    binding.paymentAmountEditText.setText(String.format("%.2f", state.total))
                }

                // Handle checkout result - check if checkout was in progress and now cart is empty
                if (checkoutInProgress && !state.isLoading && state.cartItems.isEmpty() && state.error == null) {
                    checkoutInProgress = false
                    // Checkout successful - print receipt
                    printReceiptAfterCheckout()
                    Toast.makeText(context, "Sale completed successfully!", Toast.LENGTH_SHORT).show()
                    
                    // Refresh reports by triggering a reload
                    // The reports fragment will auto-update when it becomes visible
                    dismiss()
                }

                // Handle errors
                state.error?.let { error ->
                    checkoutInProgress = false
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
