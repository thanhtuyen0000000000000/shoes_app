package com.example.shoes.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.edit
import com.example.shoes.Model.ItemsModel
import com.example.shoes.Model.UserModel
import com.example.shoes.R
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
            Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
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

        // Change password (using settings button)
        binding.settingsBtn.setOnClickListener {
            showChangePasswordDialog()
        }

        // Logout
        binding.logoutBtn.setOnClickListener {
            logout()
        }

        // Cart info - click on layout instead of textview
        binding.cartInfoLayout.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        // Favorite info - click on layout instead of textview  
        binding.favInfoLayout.setOnClickListener {
            val intent = Intent(this, FavActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserInfo(username: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(username)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val usernameValue = snapshot.child("username").getValue(String::class.java) ?: ""
                val phoneValue = snapshot.child("phonenumber").getValue(String::class.java) ?: ""
                val favSnapshot = snapshot.child("listFav")
                val cartSnapshot = snapshot.child("listCart")

                val favMap = mutableMapOf<String, ItemsModel>()
                val cartMap = mutableMapOf<String, ItemsModel>()

                favSnapshot.children.forEach {
                    val item = it.getValue(ItemsModel::class.java)
                    if (item != null) {
                        favMap[it.key ?: item.title] = item
                    }
                }

                cartSnapshot.children.forEach {
                    val item = it.getValue(ItemsModel::class.java)
                    if (item != null) {
                        cartMap[it.key ?: item.title] = item
                    }
                }

                val user = UserModel(
                    username = usernameValue,
                    phonenumber = phoneValue,
                    listFav = favMap,
                    listCart = cartMap
                )

                binding.usernameTextView.text = user.username
                binding.usernameDisplayTextView.text = user.username
                binding.phoneTextView.text = if (user.phonenumber.isNotEmpty()) {
                    user.phonenumber
                } else {
                    getString(R.string.not_updated)
                }

                binding.cartInfoTextView.text = getString(R.string.items_in_cart, user.listCart?.size ?: 0)
                binding.favInfoTextView.text = getString(R.string.items_in_favorites, user.listFav?.size ?: 0)
            } else {
                Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, getString(R.string.error_occurred, it.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChangePasswordDialog() {
        val username = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("username", null)
        if (username == null) {
            Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Password")

        // Create layout for dialog
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        // Current password input
        val currentPasswordInput = EditText(this)
        currentPasswordInput.hint = "Current Password"
        currentPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(currentPasswordInput)

        // New password input
        val newPasswordInput = EditText(this)
        newPasswordInput.hint = "New Password"
        newPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(newPasswordInput)

        // Confirm password input
        val confirmPasswordInput = EditText(this)
        confirmPasswordInput.hint = "Confirm New Password"
        confirmPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(confirmPasswordInput)

        builder.setView(layout)

        builder.setPositiveButton("Change Password") { dialog, _ ->
            val currentPassword = currentPasswordInput.text.toString().trim()
            val newPassword = newPasswordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newPassword.length < 6) {
                Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            changePassword(username, currentPassword, newPassword)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun changePassword(username: String, currentPassword: String, newPassword: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(username)

        // Check current password
        userRef.child("password").get().addOnSuccessListener { snapshot ->
            val storedPassword = snapshot.getValue(String::class.java)
            
            if (storedPassword == currentPassword) {
                // Current password is correct, update with new password
                userRef.child("password").setValue(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error checking password: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        // Clear shared preferences using KTX extension
        getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit {
            clear()
        }
        
        Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()

        // Navigate to intro activity
        val intent = Intent(this, IntroActivity::class.java)
        startActivity(intent)
        finish()
    }
}