package com.example.shoes.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shoes.Model.ItemsModel
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
        holder.binding.apply {
            txtItemName.text = item.title
            txtItemSize.text = "Size: ${if (item.size.isNotEmpty()) item.size[0] else "N/A"}"
            txtItemQuantity.text = "Số lượng: ${item.numberInCart}"
            txtItemPrice.text = "Giá: $${item.price}"
        }
    }

    override fun getItemCount(): Int = items.size
}
