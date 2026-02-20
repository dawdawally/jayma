package com.jayma.pos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jayma.pos.data.model.CartItem
import com.jayma.pos.databinding.ItemCartBinding

class CartAdapter(
    private val onQuantityChange: (Int, Double) -> Unit,
    private val onRemoveItem: (Int) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding, onQuantityChange, onRemoveItem)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CartViewHolder(
        private val binding: ItemCartBinding,
        private val onQuantityChange: (Int, Double) -> Unit,
        private val onRemoveItem: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.apply {
                productName.text = cartItem.product.name
                productCode.text = cartItem.product.code
                unitPrice.text = String.format("$%.2f each", cartItem.unitPrice)
                
                // Always update quantity text to ensure it reflects current state
                val currentQuantity = cartItem.quantity.toInt()
                quantityText.text = currentQuantity.toString()
                subtotal.text = String.format("Subtotal: $%.2f", cartItem.subtotal)

                // Clear previous listeners
                decreaseButton.setOnClickListener(null)
                increaseButton.setOnClickListener(null)
                removeButton.setOnClickListener(null)

                // Set new listeners
                decreaseButton.setOnClickListener {
                    val newQuantity = (cartItem.quantity - 1).coerceAtLeast(0.0)
                    onQuantityChange(cartItem.product.id, newQuantity)
                }

                increaseButton.setOnClickListener {
                    val newQuantity = cartItem.quantity + 1
                    if (newQuantity <= cartItem.product.qteSale) {
                        onQuantityChange(cartItem.product.id, newQuantity)
                    }
                }

                removeButton.setOnClickListener {
                    onRemoveItem(cartItem.product.id)
                }
            }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            // Compare all relevant fields to detect changes
            return oldItem.quantity == newItem.quantity &&
                   oldItem.unitPrice == newItem.unitPrice &&
                   oldItem.subtotal == newItem.subtotal &&
                   oldItem.product.name == newItem.product.name
        }
    }
}
