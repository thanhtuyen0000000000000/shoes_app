package com.example.shoes.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
        
        // Setup back button
        binding.backButton.setOnClickListener {
            navigateToMain()
        }
        
        // Khởi tạo stats ban đầu
        updateOrderStats(0, 0.0)
        
        saveNewOrderIfAny()
        loadOrderHistory()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
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
                status = "Processing",
                timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(java.util.Date())
            )
            orderRef.setValue(newOrder)
        }
    }

    private fun loadOrderHistory() {
        showLoading(true)
        
        val userId = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            .getString("username", null) ?: return

        val ordersRef = database.child("users").child(userId).child("listOrders")
        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showLoading(false)
                
                val orderList = ArrayList<OrderModel>()
                var totalSpent = 0.0
                
                for (orderSnap in snapshot.children) {
                    val order = orderSnap.getValue(OrderModel::class.java)
                    if (order != null) {
                        orderList.add(order)
                        totalSpent += order.total
                    }
                }
                
                // Cập nhật thống kê
                updateOrderStats(orderList.size, totalSpent)
                
                if (orderList.isEmpty()) {
                    showEmptyState(true)
                } else {
                    showEmptyState(false)
                    binding.recyclerViewOrders.layoutManager = LinearLayoutManager(this@OrderActivity)
                    binding.recyclerViewOrders.adapter = OrderAdapter(orderList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Log.e("OrderActivity", "Error loading orders: ${error.message}")
                Toast.makeText(this@OrderActivity, "Error loading orders", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun showLoading(show: Boolean) {
        binding.loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewOrders.visibility = if (show) View.GONE else View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewOrders.visibility = if (show) View.GONE else View.VISIBLE
        binding.loadingLayout.visibility = View.GONE
    }

    private fun updateOrderStats(totalOrders: Int, totalSpent: Double) {
        binding.totalOrdersText.text = "$totalOrders Total Orders"
        binding.totalSpentText.text = "$%.2f Spent".format(totalSpent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToMain()
    }
}