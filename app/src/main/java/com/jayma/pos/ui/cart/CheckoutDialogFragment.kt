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
import com.jayma.pos.ui.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CheckoutDialogFragment : DialogFragment() {

    private var _binding: DialogCheckoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CartViewModel by viewModels({ requireActivity() })

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
                    // Checkout successful
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

    companion object {
        fun newInstance() = CheckoutDialogFragment()
    }
}
