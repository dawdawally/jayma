package com.jayma.pos.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.jayma.pos.data.remote.ApiConfig
import com.jayma.pos.databinding.FragmentProductDetailBinding
import com.jayma.pos.ui.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()
    private val args: ProductDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = args.productId
        loadProductDetails(productId)
    }

    private fun loadProductDetails(productId: Int) {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                val product = state.products.find { it.id == productId }
                product?.let { displayProduct(it) }
            }
        }
    }

    private fun displayProduct(product: com.jayma.pos.data.local.entities.ProductEntity) {
        binding.apply {
            productName.text = product.name
            productCode.text = "Code: ${product.code}"
            productBarcode.text = product.barcode?.let { "Barcode: $it" } ?: ""
            productPrice.text = String.format("$%.2f", product.netPrice)
            productStock.text = "Stock: ${product.qteSale.toInt()} ${product.unitSale}"
            productType.text = "Type: ${product.productType}"
            
            // Load product image
            val imageUrl = if (product.image != null) {
                "${ApiConfig.BASE_URL}/${product.image}"
            } else {
                null
            }

            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(productImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
