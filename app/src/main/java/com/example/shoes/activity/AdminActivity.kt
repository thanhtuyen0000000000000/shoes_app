package com.example.shoes.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shoes.R
import com.example.shoes.databinding.ActivityAdminBinding
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : BaseActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Display admin information
        val username = intent.getStringExtra("username") ?: ""
        binding.adminNameText.text = getString(R.string.hello_admin, username)
    }

    private fun setupClickListeners() {
        // Product Management
        binding.manageProductsCard.setOnClickListener {
            startActivity(Intent(this, ProductManagementActivity::class.java))
        }

        // Order Management
        binding.manageOrdersCard.setOnClickListener {
            startActivity(Intent(this, OrderManagementActivity::class.java))
        }

        // Statistics
        binding.statisticsCard.setOnClickListener {
            startActivity(Intent(this, StatisticActivity::class.java))
        }

        // User Management
        binding.manageUsersCard.setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        // Logout
        binding.logoutCard.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Clear login information
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        
        // Return to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 