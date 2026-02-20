package com.jayma.pos.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jayma.pos.data.local.entities.ProductEntity
import com.jayma.pos.data.model.CartItem
import com.jayma.pos.data.remote.ApiConfig
import com.jayma.pos.databinding.ItemProductBinding
import com.jayma.pos.util.StockAlertHelper

class ProductAdapter(
    private val onItemClick: (ProductEntity) -> Unit,
    private val onAddToCart: (ProductEntity) -> Unit,
    private val onIncreaseQuantity: (Int) -> Unit,
    private val onDecreaseQuantity: (Int) -> Unit,
    private val cartItems: List<CartItem> = emptyList()
) : ListAdapter<ProductEntity, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding, onItemClick, onAddToCart, onIncreaseQuantity, onDecreaseQuantity)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position), cartItems)
    }

    class ProductViewHolder(
        private val binding: ItemProductBinding,
        private val onItemClick: (ProductEntity) -> Unit,
        private val onAddToCart: (ProductEntity) -> Unit,
        private val onIncreaseQuantity: (Int) -> Unit,
        private val onDecreaseQuantity: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductEntity, cartItems: List<CartItem>) {
            binding.apply {
                productName.text = product.name
                productCode.text = product.code
                productPrice.text = String.format("$%.2f", product.netPrice)
                
                // Show stock with alert if low
                val stockText = if (StockAlertHelper.isLowStock(product)) {
                    "⚠️ Low: ${product.qteSale.toInt()}"
                } else {
                    "Stock: ${product.qteSale.toInt()}"
                }
                productStock.text = stockText
                productStock.setTextColor(
                    if (StockAlertHelper.isLowStock(product)) {
                        root.context.getColor(android.R.color.holo_red_dark)
                    } else {
                        root.context.getColor(android.R.color.darker_gray)
                    }
                )

                // Load product image
                val imageUrl = if (product.image != null) {
                    "${ApiConfig.BASE_URL}/${product.image}"
                } else {
                    null
                }

                Glide.with(root.context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(productImage)

                // Check if product is in cart
                val cartItem = cartItems.find { it.product.id == product.id }
                
                if (cartItem != null) {
                    // Show quantity controls
                    addToCartButton.visibility = ViewGroup.GONE
                    quantityControls.visibility = ViewGroup.VISIBLE
                    quantityText.text = cartItem.quantity.toInt().toString()
                    
                    decreaseButton.setOnClickListener {
                        onDecreaseQuantity(product.id)
                    }
                    
                    increaseButton.setOnClickListener {
                        if (cartItem.quantity < product.qteSale) {
                            onIncreaseQuantity(product.id)
                        }
                    }
                } else {
                    // Show add to cart button
                    addToCartButton.visibility = ViewGroup.VISIBLE
                    quantityControls.visibility = ViewGroup.GONE
                    
                    addToCartButton.setOnClickListener {
                        onAddToCart(product)
                    }
                }

                root.setOnClickListener {
                    onItemClick(product)
                }
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem == newItem
        }
    }
}
