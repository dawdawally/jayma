package com.jayma.pos.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.jayma.pos.data.repository.SaleRepository
import com.jayma.pos.databinding.FragmentCartBinding
import com.jayma.pos.ui.adapter.CartAdapter
import com.jayma.pos.ui.viewmodel.CartViewModel
import com.jayma.pos.util.printer.PrinterService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CartViewModel by viewModels({ requireActivity() })
    private lateinit var cartAdapter: CartAdapter
    
    @Inject
    lateinit var saleRepository: SaleRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtons()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChange = { productId, quantity ->
                viewModel.updateQuantity(productId, quantity)
            },
            onRemoveItem = { productId ->
                viewModel.removeFromCart(productId)
            }
        )

        binding.cartItemsRecyclerView.apply {
            adapter = cartAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }
    }

    private fun setupButtons() {
        binding.checkoutButton.setOnClickListener {
            navigateToCheckout()
        }

        binding.clearCartButton.setOnClickListener {
            viewModel.clearCart()
        }
    }

    private fun navigateToCheckout() {
        CheckoutDialogFragment.newInstance().show(
            childFragmentManager,
            "CheckoutDialog"
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                cartAdapter.submitList(state.cartItems)

                // Update totals
                binding.subtotalValue.text = String.format("$%.2f", state.subtotal)
                binding.taxValue.text = String.format("$%.2f", state.tax)
                binding.discountValue.text = String.format("$%.2f", state.discount)
                binding.shippingValue.text = String.format("$%.2f", state.shipping)
                binding.totalValue.text = String.format("$%.2f", state.total)

                // Show/hide empty state
                if (state.cartItems.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.cartItemsRecyclerView.visibility = View.GONE
                    binding.checkoutButton.isEnabled = false
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.cartItemsRecyclerView.visibility = View.VISIBLE
                    binding.checkoutButton.isEnabled = true
                }

                // Handle loading state
                binding.checkoutButton.isEnabled = !state.isLoading && state.cartItems.isNotEmpty()

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
}
