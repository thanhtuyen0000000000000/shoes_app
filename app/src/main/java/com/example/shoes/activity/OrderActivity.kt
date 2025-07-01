package com.example.shoes.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoes.Adapter.OrderAdapter
import com.example.shoes.Model.ItemsModel
import com.example.shoes.Model.OrderModel

import com.example.shoes.databinding.ActivityOrderBinding
import com.google.firebase.database.*

class OrderActivity : BaseActivity() {
    private lateinit var binding: ActivityOrderBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference
        saveNewOrderIfAny()
        loadOrderHistory()
    }

    private fun saveNewOrderIfAny() {
        val cartList = intent.getParcelableArrayListExtra<ItemsModel>("cartList")
        val totalStr = intent.getStringExtra("total")
        val taxStr = intent.getStringExtra("tax")
        val deliveryStr = intent.getStringExtra("delivery")

        if (cartList != null && totalStr != null && taxStr != null && deliveryStr != null) {
            val userId = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .getString("username", null) ?: return

            val orderRef = database.child("users").child(userId).child("listOrders").push()

            val newOrder = OrderModel(
                items = cartList,
                total = totalStr.replace("$", "").toDoubleOrNull() ?: 0.0,
                tax = taxStr.replace("$", "").toDoubleOrNull() ?: 0.0,
                delivery = deliveryStr.replace("$", "").toDoubleOrNull() ?: 0.0,
                status = "Đang xử lý",
                timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(java.util.Date())
            )
            orderRef.setValue(newOrder)
        }
    }

    private fun loadOrderHistory() {
        val userId = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            .getString("username", null) ?: return

        val ordersRef = database.child("users").child(userId).child("listOrders")
        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<OrderModel>()
                for (orderSnap in snapshot.children) {
                    val order = orderSnap.getValue(OrderModel::class.java)
                    if (order != null) {
                        orderList.add(order)
                    }
                }
                if (orderList.isEmpty()) {
                    Toast.makeText(this@OrderActivity, "Chưa có đơn hàng nào", Toast.LENGTH_SHORT).show()
                    return
                }
                binding.recyclerViewOrders.layoutManager = LinearLayoutManager(this@OrderActivity)
                binding.recyclerViewOrders.adapter = OrderAdapter(orderList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderActivity", "Lỗi tải đơn hàng: ${error.message}")
            }
        })
    }
}