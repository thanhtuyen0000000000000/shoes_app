package com.example.shoes.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shoes.databinding.ActivityLoginBinding
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            // Kiểm tra rỗng
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Truy cập Firebase
            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("users")

            // Lấy dữ liệu người dùng từ Firebase
            usersRef.child(username).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val storedPassword = snapshot.child("password").value.toString()
                        if (password == storedPassword) {
                            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                            // Điều hướng sang trang chính
                            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            sharedPref.edit().putString("username", username).apply()
                            val intent = Intent(this, MainActivity::class.java) // Đổi MainActivity nếu cần
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.putExtra("username", username)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Sai mật khẩu", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Tên người dùng không tồn tại", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Bạn có thể xử lý quên mật khẩu ở đây nếu cần
        binding.forgotPasswordText.setOnClickListener {
            Toast.makeText(this, "Tính năng chưa hỗ trợ", Toast.LENGTH_SHORT).show()
        }
    }
}
