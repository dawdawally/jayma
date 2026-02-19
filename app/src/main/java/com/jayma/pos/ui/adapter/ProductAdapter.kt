package com.jayma.pos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jayma.pos.data.local.entities.ProductEntity
import com.jayma.pos.data.remote.ApiConfig
import com.jayma.pos.databinding.ItemProductBinding
import com.jayma.pos.util.StockAlertHelper

class ProductAdapter(
    private val onItemClick: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(
        private val binding: ItemProductBinding,
        private val onItemClick: (ProductEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductEntity) {
            binding.apply {
                productName.text = product.name
                productCode.text = product.code
                productPrice.text = String.format("$%.2f", product.netPrice)
                
                // Show stock with alert if low
                val stockText = if (StockAlertHelper.isLowStock(product)) {
                    "⚠️ Low Stock: ${product.qteSale.toInt()}"
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
                productUnit.text = product.unitSale

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

                root.setOnClickListener {
                    onItemClick(product)
                }
                
                // Add to cart button (will be fully implemented in Phase 5)
                addToCartButton.setOnClickListener {
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
