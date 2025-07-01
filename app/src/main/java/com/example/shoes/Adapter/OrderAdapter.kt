
package com.example.shoes.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoes.Model.OrderModel
import com.example.shoes.databinding.ItemOrderBinding

class OrderAdapter(
    private val orders: List<OrderModel>,
    private val usernames: List<String> = emptyList(),
    private val isAdminView: Boolean = false,
    private val onStatusUpdate: ((OrderModel) -> Unit)? = null
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.apply {
            txtOrderTime.text = order.timestamp
            txtOrderStatus.text = order.status
            txtTotal.text = "Tổng cộng: $${order.total}"
            txtTax.text = "Thuế : $${order.tax}"
            txtDelivery.text = "Phí giao hàng $${order.delivery}"
            recyclerOrderDetail.layoutManager = LinearLayoutManager(holder.itemView.context)
            recyclerOrderDetail.adapter = OrderDetailAdapter(order.items)

            if (usernames.isNotEmpty()) {
                txtUserName.visibility = View.VISIBLE
                txtUserName.text = "Người đặt: ${usernames[position]}"
            } else {
                txtUserName.visibility = View.GONE
            }

            if (isAdminView) {
                btnMarkDelivered.visibility = View.VISIBLE
                if (order.status == "Đã giao") {
                    btnMarkDelivered.text = "Đã giao"
                    btnMarkDelivered.isEnabled = false
                } else {
                    btnMarkDelivered.text = "Đánh dấu đã giao"
                    btnMarkDelivered.isEnabled = true
                    btnMarkDelivered.setOnClickListener {
                        onStatusUpdate?.invoke(order)
                    }
                }
            } else {
                btnMarkDelivered.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = orders.size
}
