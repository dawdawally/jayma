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

    class CheckoutCartViewHolder(
        private val binding: ItemCheckoutCartBinding,
        private val onIncreaseQuantity: (Int) -> Unit,
        private val onDecreaseQuantity: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.apply {
                productName.text = cartItem.product.name
                productPrice.text = String.format("$%.2f each", cartItem.unitPrice)
                quantityText.text = cartItem.quantity.toInt().toString()
                subtotal.text = String.format("$%.2f", cartItem.subtotal)

                increaseButton.setOnClickListener {
                    if (cartItem.quantity < cartItem.product.qteSale) {
                        onIncreaseQuantity(cartItem.product.id)
                    }
                }

                decreaseButton.setOnClickListener {
                    if (cartItem.quantity > 1) {
                        onDecreaseQuantity(cartItem.product.id)
                    } else {
                        onDecreaseQuantity(cartItem.product.id) // Will remove if quantity becomes 0
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
            return oldItem == newItem
        }
    }
}
