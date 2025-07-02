package com.example.shoes.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoes.Adapter.OrderAdapter
import com.example.shoes.Model.OrderModel
import com.example.shoes.R
import com.example.shoes.databinding.ActivityOrderManagementBinding
import com.google.firebase.database.*

class OrderManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderManagementBinding
    private lateinit var database: DatabaseReference
    private val allOrders = mutableListOf<Pair<String, String>>() // Pair(userId, orderId)
    private val orderList = mutableListOf<OrderModel>()
    private val usernameList = mutableListOf<String>()
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference
        setupUI()
        loadAllOrders()
    }

    private fun setupUI() {
        // Back button functionality
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun loadAllOrders() {
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allOrders.clear()
                orderList.clear()
                usernameList.clear()
                
                var totalOrders = 0
                var pendingOrders = 0
                
                for (userSnap in snapshot.children) {
                    val userId = userSnap.key ?: continue
                    val ordersSnap = userSnap.child("listOrders")
                    val username = userSnap.child("username").getValue(String::class.java) ?: getString(R.string.unknown_user)
                    for (orderSnap in ordersSnap.children) {
                        val order = orderSnap.getValue(OrderModel::class.java)
                        if (order != null) {
                            allOrders.add(Pair(userId, orderSnap.key ?: ""))
                            orderList.add(order)
                            usernameList.add(username)
                            totalOrders++
                            
                            // Count pending orders (not delivered)
                            if (order.status != "Delivered") {
                                pendingOrders++
                            }
                        }
                    }
                }

                // Update statistics display
                binding.totalOrdersText.text = totalOrders.toString()
                binding.pendingOrdersText.text = pendingOrders.toString()

                binding.recyclerViewManageOrders.layoutManager = LinearLayoutManager(this@OrderManagementActivity)
                orderAdapter = OrderAdapter(
                    orders = orderList,
                    usernames = usernameList,
                    isAdminView = true,
                    onStatusUpdate = { order ->
                        markOrderAsDeliveredAndRemove(order)
                    }
                )
                binding.recyclerViewManageOrders.adapter = orderAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderManagement", "Error: ${error.message}")
                Toast.makeText(this@OrderManagementActivity, "Failed to load orders", Toast.LENGTH_SHORT).show()
                binding.totalOrdersText.text = "Error"
                binding.pendingOrdersText.text = "Error"
            }
        })
    }

    private fun markOrderAsDeliveredAndRemove(order: OrderModel) {
        val index = orderList.indexOf(order)
        if (index == -1) return

        val (userId, orderId) = allOrders[index]
        
        // Delete the order from Firebase
        database.child("users").child(userId).child("listOrders").child(orderId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.order_delivered_and_removed), Toast.LENGTH_SHORT).show()
                
                // Remove from local lists
                allOrders.removeAt(index)
                
                // Update adapter (this will also remove from orderList and usernameList)
                orderAdapter.removeItem(index)
                
                // Update statistics
                val currentTotal = binding.totalOrdersText.text.toString().toIntOrNull() ?: 0
                val currentPending = binding.pendingOrdersText.text.toString().toIntOrNull() ?: 0
                
                binding.totalOrdersText.text = (currentTotal - 1).toString()
                if (order.status != "Delivered") {
                    binding.pendingOrdersText.text = (currentPending - 1).toString()
                }
                
                Log.d("OrderManagement", "Order successfully removed. Remaining orders: ${orderList.size}")
            }
            .addOnFailureListener { exception ->
                Log.e("OrderManagement", "Failed to remove order: ${exception.message}")
                Toast.makeText(this, getString(R.string.failed_to_mark_delivered, exception.message ?: "Unknown error"), Toast.LENGTH_SHORT).show()
            }
    }
}
