package com.example.shoes.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoes.Adapter.OrderAdapter
import com.example.shoes.Model.OrderModel
import com.example.shoes.databinding.ActivityOrderManagementBinding
import com.google.firebase.database.*

class OrderManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderManagementBinding
    private lateinit var database: DatabaseReference
    private val allOrders = mutableListOf<Pair<String, String>>() // Pair(userId, orderId)
    private val orderList = mutableListOf<OrderModel>()
    private val usernameList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference
        loadAllOrders()
    }

    private fun loadAllOrders() {
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allOrders.clear()
                orderList.clear()
                usernameList.clear()
                for (userSnap in snapshot.children) {
                    val userId = userSnap.key ?: continue
                    val ordersSnap = userSnap.child("listOrders")
                    val username = userSnap.child("username").getValue(String::class.java) ?: "Không rõ"
                    for (orderSnap in ordersSnap.children) {
                        val order = orderSnap.getValue(OrderModel::class.java)
                        if (order != null) {
                            allOrders.add(Pair(userId, orderSnap.key ?: ""))
                            orderList.add(order)
                            usernameList.add(username)
                        }
                    }
                }

                binding.recyclerViewManageOrders.layoutManager = LinearLayoutManager(this@OrderManagementActivity)
                binding.recyclerViewManageOrders.adapter = OrderAdapter(
                    orders = orderList,
                    usernames = usernameList,
                    isAdminView = true,
                    onStatusUpdate = { order ->
                        val index = orderList.indexOf(order)
                        if (index != -1) {
                            val (userId, orderId) = allOrders[index]
                            database.child("users").child(userId).child("listOrders").child(orderId)
                                .child("status").setValue("Đã giao")
                                .addOnSuccessListener {
                                    Toast.makeText(this@OrderManagementActivity, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show()
                                    val updatedOrder = order.copy(status = "Đã giao")
                                    orderList[index] = updatedOrder
                                    binding.recyclerViewManageOrders.adapter?.notifyItemChanged(index)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@OrderManagementActivity, "Lỗi cập nhật đơn", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderManagement", "Lỗi: ${error.message}")
            }
        })
    }
}
