package com.example.shoes.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoes.Model.ItemsModel
import com.example.shoes.R
import com.example.shoes.databinding.ItemOrderDetailBinding

class OrderDetailAdapter(private val items: List<ItemsModel>) :
    RecyclerView.Adapter<OrderDetailAdapter.DetailViewHolder>() {

    inner class DetailViewHolder(val binding: ItemOrderDetailBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemOrderDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        
        holder.binding.apply {
            // Product name
            txtItemName.text = item.title
            
            // Size
            val sizeText = if (item.size.isNotEmpty()) {
                context.getString(R.string.size_format, item.size[0])
            } else {
                context.getString(R.string.size_format, "N/A")
            }
            txtItemSize.text = sizeText
            
            // Quantity
            txtItemQuantity.text = context.getString(R.string.qty_format, item.numberInCart)
            
            // Price per item
            txtItemPrice.text = context.getString(R.string.order_total, item.price)
            
            // Subtotal (price * quantity)
            val subtotal = item.price * item.numberInCart
            txtItemSubtotal.text = context.getString(R.string.subtotal_format, subtotal)
            
            // Product image
            if (item.picUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(item.picUrl[0])
                    .placeholder(R.drawable.shoes)
                    .error(R.drawable.shoes)
                    .centerCrop()
                    .into(imgProduct)
            } else {
                imgProduct.setImageResource(R.drawable.shoes)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
