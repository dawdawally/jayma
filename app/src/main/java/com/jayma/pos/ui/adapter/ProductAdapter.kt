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
    private val onDecreaseQuantity: (Int) -> Unit
) : ListAdapter<ProductEntity, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    private var cartItems: List<CartItem> = emptyList()

    fun updateCartItems(newCartItems: List<CartItem>) {
        val oldCartItems = cartItems.toList()
        cartItems = newCartItems
        
        // Find which product IDs changed quantity or were added/removed
        val changedProductIds = mutableSetOf<Int>()
        
        // Check all products in current list
        for (i in 0 until itemCount) {
            try {
                val product = getItem(i)
                val oldItem = oldCartItems.find { it.product.id == product.id }
                val newItem = newCartItems.find { it.product.id == product.id }
                
                // If quantity changed or item was added/removed from cart
                if (oldItem?.quantity != newItem?.quantity || (oldItem == null) != (newItem == null)) {
                    changedProductIds.add(product.id)
                }
            } catch (e: Exception) {
                // Continue if item not available
            }
        }
        
        // Also check for products that were in cart but might not be in current product list
        val allProductIds = (oldCartItems.map { it.product.id } + newCartItems.map { it.product.id }).toSet()
        for (productId in allProductIds) {
            val oldItem = oldCartItems.find { it.product.id == productId }
            val newItem = newCartItems.find { it.product.id == productId }
            if (oldItem?.quantity != newItem?.quantity || (oldItem == null) != (newItem == null)) {
                changedProductIds.add(productId)
            }
        }
        
        // Notify all changed positions immediately
        if (changedProductIds.isNotEmpty()) {
            for (i in 0 until itemCount) {
                try {
                    val product = getItem(i)
                    if (changedProductIds.contains(product.id)) {
                        notifyItemChanged(i)
                    }
                } catch (e: Exception) {
                    // Continue if item not available
                }
            }
        }
    }

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
                
                // Show stock status - check for out of stock first
                val isOutOfStock = product.qteSale <= 0
                val isLowStock = !isOutOfStock && StockAlertHelper.isLowStock(product)
                
                val stockText = when {
                    isOutOfStock -> "Out of Stock"
                    isLowStock -> "⚠️ Low: ${product.qteSale.toInt()}"
                    else -> "Stock: ${product.qteSale.toInt()}"
                }
                productStock.text = stockText
                productStock.setTextColor(
                    when {
                        isOutOfStock -> root.context.getColor(android.R.color.holo_red_dark)
                        isLowStock -> root.context.getColor(android.R.color.holo_red_dark)
                        else -> root.context.getColor(android.R.color.darker_gray)
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
                    
                    // Always update quantity text to ensure it reflects current state
                    val currentQuantity = cartItem.quantity.toInt()
                    quantityText.text = currentQuantity.toString()
                    
                    // Clear previous listeners to avoid duplicates
                    decreaseButton.setOnClickListener(null)
                    increaseButton.setOnClickListener(null)
                    
                    // Store product ID for listeners
                    val productId = product.id
                    
                    // Set new listeners
                    decreaseButton.setOnClickListener {
                        onDecreaseQuantity(productId)
                    }
                    
                    increaseButton.setOnClickListener {
                        if (cartItem.quantity < product.qteSale) {
                            onIncreaseQuantity(productId)
                        }
                    }
                    
                    // Disable increase button if out of stock or at max
                    increaseButton.isEnabled = !isOutOfStock && cartItem.quantity < product.qteSale
                } else {
                    // Show add to cart button
                    addToCartButton.visibility = ViewGroup.VISIBLE
                    quantityControls.visibility = ViewGroup.GONE
                    
                    // Disable add button if out of stock
                    addToCartButton.isEnabled = !isOutOfStock
                    if (isOutOfStock) {
                        addToCartButton.text = "Out of Stock"
                    } else {
                        addToCartButton.text = "Add"
                    }
                    
                    addToCartButton.setOnClickListener {
                        if (!isOutOfStock) {
                            onAddToCart(product)
                        }
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
