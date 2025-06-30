package com.example.shoes.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.shoes.Model.UserModel
import com.example.shoes.databinding.ActivityProfileBinding
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity: BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("username", null)

        if (username == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserInfo(username)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Back button
        binding.backBtn.setOnClickListener {
            finish()
        }

        // Change password
        binding.changePasswordBtn.setOnClickListener {
            Toast.makeText(this, "Chức năng đổi mật khẩu sẽ được phát triển", Toast.LENGTH_SHORT).show()
            // TODO: Implement change password functionality
        }

        // Logout
        binding.logoutBtn.setOnClickListener {
            logout()
        }

        // Cart info - click vào layout thay vì textview
        binding.cartInfoLayout.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        // Favorite info - click vào layout thay vì textview  
        binding.favInfoLayout.setOnClickListener {
            val intent = Intent(this, FavActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserInfo(username: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(username)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(UserModel::class.java)
                user?.let {
                    // Update header username
                    binding.usernameTextView.text = it.username
                    
                    // Update info card
                    binding.usernameDisplayTextView.text = it.username
                    binding.phoneTextView.text = if (it.phonenumber.isNotEmpty()) {
                        it.phonenumber
                    } else {
                        "Chưa cập nhật"
                    }
                    
                    // Update activity counts
                    binding.cartInfoTextView.text = "${it.listCart.size} sản phẩm"
                    binding.favInfoTextView.text = "${it.listFav.size} yêu thích"

                    // TODO: Nếu có avatar URL thì load bằng Glide
                    // Glide.with(this).load(it.avatarUrl).into(binding.avatarImageView)
                }
            } else {
                Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        // Clear shared preferences
        getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit().clear().apply()
        
        Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()

        // Navigate to intro activity
        val intent = Intent(this, IntroActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}