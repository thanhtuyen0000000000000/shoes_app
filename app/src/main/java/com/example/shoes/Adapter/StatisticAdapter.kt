package com.example.shoes.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shoes.Model.StatisticModel
import com.example.shoes.R
import com.example.shoes.databinding.ItemStatisticBinding

class StatisticAdapter(private val stats: List<StatisticModel>) :
    RecyclerView.Adapter<StatisticAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemStatisticBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStatisticBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = stats[position]
        holder.binding.apply {
            txtProductName.text = item.productName
            txtQuantitySold.text = holder.itemView.context.getString(R.string.quantity_sold, item.quantitySold)
            txtRevenue.text = "Revenue: $${item.revenue}"
        }
    }

    override fun getItemCount() = stats.size
}
