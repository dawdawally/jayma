package com.jayma.pos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jayma.pos.data.model.CartItem
import com.jayma.pos.databinding.ItemCheckoutCartBinding

class CheckoutCartAdapter(
    private val onIncreaseQuantity: (Int) -> Unit,
    private val onDecreaseQuantity: (Int) -> Unit
) : ListAdapter<CartItem, CheckoutCartAdapter.CheckoutCartViewHolder>(CheckoutCartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutCartViewHolder {
        val binding = ItemCheckoutCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CheckoutCartViewHolder(binding, onIncreaseQuantity, onDecreaseQuantity)
    }

    override fun onBindViewHolder(holder: CheckoutCartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    override fun onBindViewHolder(
        holder: CheckoutCartViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            // Handle partial updates
            val cartItem = getItem(position)
            
            @Suppress("UNCHECKED_CAST")
            val changes = payloads.firstOrNull() as? List<String>
            if (changes != null) {
                holder.updatePartial(cartItem, changes)
            } else {
                // If payload format is unexpected, do full bind
                holder.bind(cartItem)
            }
        } else {
            // Full bind
            holder.bind(getItem(position))
        }
    }

    class CheckoutCartViewHolder(
        private val binding: ItemCheckoutCartBinding,
        private val onIncreaseQuantity: (Int) -> Unit,
        private val onDecreaseQuantity: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.apply {
                productName.text = cartItem.product.name
                productPrice.text = String.format("$%.2f each", cartItem.unitPrice)
                
                // Always update quantity text to ensure it reflects current state
                val quantity = cartItem.quantity.toInt()
                quantityText.text = quantity.toString()
                subtotal.text = String.format("$%.2f", cartItem.subtotal)

                // Clear previous listeners to avoid multiple callbacks
                increaseButton.setOnClickListener(null)
                decreaseButton.setOnClickListener(null)

                // Set new listeners with current product ID
                val productId = cartItem.product.id
                increaseButton.setOnClickListener {
                    onIncreaseQuantity(productId)
                }

                decreaseButton.setOnClickListener {
                    onDecreaseQuantity(productId)
                }
            }
        }
        
        fun updatePartial(cartItem: CartItem, changes: List<String>) {
            binding.apply {
                changes.forEach { change ->
                    when (change) {
                        "quantity" -> {
                            quantityText.text = cartItem.quantity.toInt().toString()
                        }
                        "subtotal" -> {
                            subtotal.text = String.format("$%.2f", cartItem.subtotal)
                        }
                    }
                }
            }
        }
    }

    class CheckoutCartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
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
        
        override fun getChangePayload(oldItem: CartItem, newItem: CartItem): Any? {
            // Return a payload to indicate what changed for partial updates
            val payload = mutableListOf<String>()
            if (oldItem.quantity != newItem.quantity) {
                payload.add("quantity")
            }
            if (oldItem.subtotal != newItem.subtotal) {
                payload.add("subtotal")
            }
            return if (payload.isEmpty()) null else payload
        }
    }
}
