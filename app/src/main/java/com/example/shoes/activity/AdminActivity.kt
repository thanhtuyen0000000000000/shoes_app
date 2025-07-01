package com.example.shoes.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        // Hiển thị thông tin admin
        val username = intent.getStringExtra("username") ?: ""
        binding.adminNameText.text = "Xin chào Admin: $username"
    }

    private fun setupClickListeners() {
        // Quản lý sản phẩm
        binding.manageProductsCard.setOnClickListener {
//            Toast.makeText(this, "Tính năng quản lý sản phẩm sẽ được phát triển", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ProductManagementActivity::class.java))
        }

        // Quản lý đơn hàng
        binding.manageOrdersCard.setOnClickListener {
//            Toast.makeText(this, "Tính năng quản lý đơn hàng sẽ được phát triển", Toast.LENGTH_SHORT).show()
            // TODO: Chuyển đến OrderManagementActivity
            startActivity(Intent(this, OrderManagementActivity::class.java))
        }

        // Thống kê
        binding.statisticsCard.setOnClickListener {
//            Toast.makeText(this, "Tính năng thống kê sẽ được phát triển", Toast.LENGTH_SHORT).show()
            // TODO: Chuyển đến StatisticsActivity

            startActivity(Intent(this, StatisticActivity::class.java))
        }

        // Quản lý người dùng
        binding.manageUsersCard.setOnClickListener {
//            Toast.makeText(this, "Tính năng quản lý người dùng sẽ được phát triển", Toast.LENGTH_SHORT).show()
            // TODO: Chuyển đến UserManagementActivity
            startActivity(Intent(this, UserManagementActivity::class.java))

        }

        // Cài đặt hệ thống (mới)
        binding.settingsCard.setOnClickListener {
            Toast.makeText(this, "Tính năng cài đặt hệ thống sẽ được phát triển", Toast.LENGTH_SHORT).show()
            // TODO: Chuyển đến SettingsActivity
        }

        // Đăng xuất (cập nhật từ button thành card)
        binding.logoutCard.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Xóa thông tin đăng nhập
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        
        // Chuyển về màn hình đăng nhập
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 