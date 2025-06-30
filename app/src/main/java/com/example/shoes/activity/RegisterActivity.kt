package com.example.shoes.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shoes.Model.UserModel
import com.example.shoes.databinding.ActivityRegisterBinding
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val rePassword = binding.repasswordEditText.text.toString().trim()
            val phonenumber = binding.phonenumberEditText.text.toString().trim()

            // Kiểm tra rỗng
            if (username.isEmpty() || password.isEmpty() || rePassword.isEmpty() || phonenumber.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra khớp mật khẩu
            if (password != rePassword) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("users")

            // Kiểm tra username có tồn tại chưa
            usersRef.child(username).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        Toast.makeText(this, "Tên người dùng đã tồn tại", Toast.LENGTH_SHORT).show()
                    } else {
                        val user = UserModel(username = username, password = password, phonenumber = phonenumber)

                        usersRef.child(username).setValue(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Đăng ký thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lỗi kiểm tra tài khoản: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Chuyển về trang đăng nhập
        binding.loginLinkText.setOnClickListener {
            finish() // Quay lại LoginActivity
        }
    }
}


