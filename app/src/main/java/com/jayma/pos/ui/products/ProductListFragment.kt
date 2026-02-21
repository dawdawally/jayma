package com.jayma.pos.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.GridLayoutManager
import com.jayma.pos.data.local.entities.ProductEntity
import com.jayma.pos.databinding.FragmentProductListBinding
import com.jayma.pos.databinding.FragmentProductListSplitBinding
import com.jayma.pos.ui.adapter.ProductAdapter
import com.jayma.pos.ui.adapter.CartAdapter
import com.jayma.pos.ui.cart.CartFragment
import com.jayma.pos.ui.cart.CheckoutDialogFragment
import com.jayma.pos.ui.scanner.BarcodeScannerFragment
import com.jayma.pos.ui.viewmodel.BarcodeScannerViewModel
import com.jayma.pos.ui.viewmodel.CartViewModel
import com.jayma.pos.ui.viewmodel.ProductViewModel
import com.jayma.pos.util.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding not available")
    
    private var _splitBinding: FragmentProductListSplitBinding? = null
    private val splitBinding get() = _splitBinding ?: throw IllegalStateException("Split binding not available")

    private val productViewModel: ProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels({ requireActivity() })
    private val barcodeScannerViewModel: BarcodeScannerViewModel by viewModels()
    
    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper

    private lateinit var productAdapter: ProductAdapter
    private var cartAdapter: CartAdapter? = null
    private var isSplitScreen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Check if screen is large enough for split screen (600dp = tablet/POS size)
        val screenWidthDp = resources.configuration.screenWidthDp
        isSplitScreen = screenWidthDp >= 600
        
        return if (isSplitScreen) {
            _splitBinding = FragmentProductListSplitBinding.inflate(inflater, container, false)
            splitBinding.root
        } else {
            _binding = FragmentProductListBinding.inflate(inflater, container, false)
            binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupPullToRefresh()
        setupBarcodeScanner()
        if (isSplitScreen) {
            setupSplitScreenCart()
        }
        observeViewModel()
        observeBarcodeScanner()
    }
    
    private fun setupSplitScreenCart() {
        cartAdapter = CartAdapter(
            onQuantityChange = { productId, quantity ->
                cartViewModel.updateQuantity(productId, quantity)
            },
            onRemoveItem = { productId ->
                cartViewModel.removeFromCart(productId)
            }
        )
        
        splitBinding.cartItemsRecyclerView.apply {
            adapter = cartAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }
        
        splitBinding.checkoutButton.setOnClickListener {
            CheckoutDialogFragment.newInstance().show(
                childFragmentManager,
                "CheckoutDialog"
            )
        }
        
        splitBinding.clearCartButton.setOnClickListener {
            cartViewModel.clearCart()
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onItemClick = { product ->
                // Navigate to product detail (if needed)
            },
            onAddToCart = { product ->
                cartViewModel.addToCart(product, 1.0)
                Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
            },
            onIncreaseQuantity = { productId ->
                val currentQuantity = cartViewModel.uiState.value.cartItems
                    .find { it.product.id == productId }?.quantity ?: 0.0
                cartViewModel.updateQuantity(productId, currentQuantity + 1.0)
            },
            onDecreaseQuantity = { productId ->
                val currentQuantity = cartViewModel.uiState.value.cartItems
                    .find { it.product.id == productId }?.quantity ?: 0.0
                if (currentQuantity > 1) {
                    cartViewModel.updateQuantity(productId, currentQuantity - 1.0)
                } else {
                    cartViewModel.removeFromCart(productId)
                }
            }
        )

        // Responsive grid: 2 columns on phones, 3-4 on tablets/POS devices
        val spanCount = if (resources.configuration.screenWidthDp >= 600) {
            if (resources.configuration.screenWidthDp >= 840) 4 else 3
        } else {
            2
        }
        
        val productsRecyclerView = if (isSplitScreen) {
            splitBinding.productsRecyclerView
        } else {
            binding.productsRecyclerView
        }
        
        productsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = productAdapter
        }
    }

    private fun setupSearch() {
        val searchView = if (isSplitScreen) {
            splitBinding.searchView
        } else {
            binding.searchView
        }
        
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                productViewModel.searchProducts(newText ?: "")
                return true
            }
        })
    }

    private fun setupPullToRefresh() {
        val swipeRefreshLayout = if (isSplitScreen) {
            splitBinding.swipeRefreshLayout
        } else {
            binding.swipeRefreshLayout
        }
        
        swipeRefreshLayout.setOnRefreshListener {
            val warehouseId = sharedPreferences.getDefaultWarehouse()
            if (warehouseId != null) {
                productViewModel.syncProducts(warehouseId)
            } else {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "No warehouse selected", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupBarcodeScanner() {
        val scanButton = if (isSplitScreen) {
            splitBinding.scanButton
        } else {
            binding.scanButton
        }
        
        scanButton.setOnClickListener {
            // Open barcode scanner fragment
            val scannerFragment = BarcodeScannerFragment()
            // Replace in the fragment container (same container used by MainActivity)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(com.jayma.pos.R.id.fragmentContainer, scannerFragment)
                .addToBackStack("barcode_scanner")
                .commit()
        }
    }
    
    private fun observeBarcodeScanner() {
        lifecycleScope.launch {
            barcodeScannerViewModel.foundProduct.collect { product ->
                product?.let {
                    // Add product to cart when found via barcode scan
                    cartViewModel.addToCart(it, 1.0)
                    Toast.makeText(
                        context,
                        "${it.name} added to cart",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Reset after handling
                    barcodeScannerViewModel.reset()
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            productViewModel.uiState.collect { state ->
                productAdapter.submitList(state.products)

                // Handle loading state
                val swipeRefreshLayout = if (isSplitScreen) {
                    splitBinding.swipeRefreshLayout
                } else {
                    binding.swipeRefreshLayout
                }
                swipeRefreshLayout.isRefreshing = state.isLoading

                // Handle error state
                state.error?.let { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }

                // Show empty state
                if (isSplitScreen) {
                    if (state.products.isEmpty() && !state.isLoading) {
                        splitBinding.emptyState.visibility = View.VISIBLE
                        splitBinding.productsRecyclerView.visibility = View.GONE
                    } else {
                        splitBinding.emptyState.visibility = View.GONE
                        splitBinding.productsRecyclerView.visibility = View.VISIBLE
                    }
                } else {
                    if (state.products.isEmpty() && !state.isLoading) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.productsRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.productsRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
        
        // Observe cart to update product adapter with cart items
        lifecycleScope.launch {
            cartViewModel.uiState.collect { cartState ->
                // Update adapter with current cart items without recreating it
                productAdapter.updateCartItems(cartState.cartItems)
                
                // Update split screen cart if available
                if (isSplitScreen) {
                    cartAdapter?.submitList(cartState.cartItems)
                    
                    // Update totals
                    splitBinding.subtotalValue.text = String.format("$%.2f", cartState.subtotal)
                    splitBinding.taxValue.text = String.format("$%.2f", cartState.tax)
                    splitBinding.discountValue.text = String.format("$%.2f", cartState.discount)
                    splitBinding.shippingValue.text = String.format("$%.2f", cartState.shipping)
                    splitBinding.totalValue.text = String.format("$%.2f", cartState.total)
                    
                    // Show/hide empty state
                    if (cartState.cartItems.isEmpty()) {
                        splitBinding.emptyStateCart.visibility = View.VISIBLE
                        splitBinding.cartItemsRecyclerView.visibility = View.GONE
                        splitBinding.checkoutButton.isEnabled = false
                    } else {
                        splitBinding.emptyStateCart.visibility = View.GONE
                        splitBinding.cartItemsRecyclerView.visibility = View.VISIBLE
                        splitBinding.checkoutButton.isEnabled = true
                    }
                    
                    splitBinding.checkoutButton.isEnabled = !cartState.isLoading && cartState.cartItems.isNotEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _splitBinding = null
    }
}
