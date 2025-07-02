package com.example.shoes.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoes.Adapter.OrderDetailAdapter
import com.example.shoes.Model.OrderModel
import com.example.shoes.R
import com.example.shoes.databinding.ItemOrderBinding

class OrderAdapter(
    private val orders: MutableList<OrderModel>,
    private val usernames: MutableList<String> = mutableListOf(),
    private val isAdminView: Boolean = false,
    private val onStatusUpdate: ((OrderModel) -> Unit)? = null
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    // Track expanded state for each item
    private val expandedStates = mutableMapOf<Int, Boolean>()

    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context
        
        holder.binding.apply {
            txtOrderTime.text = order.timestamp
            txtOrderStatus.text = order.status
            txtTotal.text = context.getString(R.string.order_total, order.total)
            txtTax.text = context.getString(R.string.order_tax, order.tax)
            txtDelivery.text = context.getString(R.string.order_delivery, order.delivery)
            
            // Set up order details recycler
            recyclerOrderDetail.layoutManager = LinearLayoutManager(context)
            recyclerOrderDetail.adapter = OrderDetailAdapter(order.items)
            
            // Set initial visibility based on expanded state (default collapsed)
            val isExpanded = expandedStates[position] ?: false
            recyclerOrderDetail.visibility = if (isExpanded) View.VISIBLE else View.GONE
            txtExpandHint.text = if (isExpanded) "Tap to hide" else "Tap to view"

            // Handle username display for admin view
            if (usernames.isNotEmpty() && position < usernames.size) {
                txtUserName.visibility = View.VISIBLE
                txtUserName.text = context.getString(R.string.ordered_by, usernames[position])
            } else {
                txtUserName.visibility = View.GONE
            }

            // Handle admin button
            if (isAdminView) {
                btnMarkDeliveredCard.visibility = View.VISIBLE
                if (order.status == context.getString(R.string.delivered)) {
                    btnMarkDelivered.text = context.getString(R.string.delivered)
                    btnMarkDelivered.isEnabled = false
                    btnMarkDelivered.alpha = 0.5f
                } else {
                    btnMarkDelivered.text = context.getString(R.string.mark_as_delivered)
                    btnMarkDelivered.isEnabled = true
                    btnMarkDelivered.alpha = 1.0f
                    btnMarkDelivered.setOnClickListener {
                        onStatusUpdate?.invoke(order)
                    }
                }
            } else {
                btnMarkDeliveredCard.visibility = View.GONE
            }

            // Add click listener to expand/collapse order details
            root.setOnClickListener {
                val currentState = expandedStates[position] ?: false
                val newState = !currentState
                expandedStates[position] = newState
                
                // Update UI immediately
                recyclerOrderDetail.visibility = if (newState) View.VISIBLE else View.GONE
                txtExpandHint.text = if (newState) "Tap to hide" else "Tap to view"
            }
        }
    }

    override fun getItemCount(): Int = orders.size

    // Method to remove item from adapter
    fun removeItem(position: Int) {
        if (position >= 0 && position < orders.size) {
            orders.removeAt(position)
            if (position < usernames.size) {
                usernames.removeAt(position)
            }
            
            // Clear expanded state for removed item and adjust indices
            val newExpandedStates = mutableMapOf<Int, Boolean>()
            for ((index, isExpanded) in expandedStates) {
                when {
                    index < position -> newExpandedStates[index] = isExpanded
                    index > position -> newExpandedStates[index - 1] = isExpanded
                    // index == position -> remove this entry (don't add to new map)
                }
            }
            expandedStates.clear()
            expandedStates.putAll(newExpandedStates)
            
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, orders.size)
        }
    }

    // Method to update data
    fun updateData(newOrders: MutableList<OrderModel>, newUsernames: MutableList<String>) {
        orders.clear()
        orders.addAll(newOrders)
        usernames.clear()
        usernames.addAll(newUsernames)
        expandedStates.clear()
        notifyDataSetChanged()
    }
}
