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

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.changePasswordBtn.setOnClickListener {
            Toast.makeText(this, "Chức năng đổi mật khẩu chưa hỗ trợ", Toast.LENGTH_SHORT).show()
        }

        binding.logoutBtn.setOnClickListener {
            getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit().clear().apply()
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, IntroActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.cartInfoTextView.setOnClickListener{
            val intent = Intent(this, CartActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.favInfoTextView.setOnClickListener{
            val intent = Intent(this,FavActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadUserInfo(username: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(username)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(UserModel::class.java)
                user?.let {
                    binding.usernameTextView.text = "👤 ${it.username}"
                    binding.phoneTextView.text = "📞 ${it.phonenumber}"
                    binding.cartInfoTextView.text = "🛒 Giỏ hàng: ${it.listCart.size} sản phẩm"
                    binding.favInfoTextView.text = "❤️ Yêu thích: ${it.listFav.size} sản phẩm"

                    // Nếu có avatar URL thì load bằng Glide
                    // Glide.with(this).load(it.avatarUrl).into(binding.avatarImageView)
                }
            } else {
                Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}