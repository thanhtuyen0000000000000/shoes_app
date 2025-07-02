package com.example.shoes.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoes.Adapter.UserAdapter
import com.example.shoes.Model.UserModel
import com.example.shoes.databinding.ActivityUserManagementBinding
import com.google.firebase.database.*

class UserManagementActivity : BaseActivity() {
    private lateinit var binding: ActivityUserManagementBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private val userList = mutableListOf<UserModel>()
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users") // Path to user list

        setupUI()
        setupRecyclerView()
        loadUsers()
    }

    private fun setupUI() {
        // Back button functionality
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(userList)
        binding.userRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.userRecyclerView.adapter = adapter
    }

    private fun loadUsers() {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                var userCount = 0
                for (userSnapshot in snapshot.children) {
                    val username = userSnapshot.child("username").getValue(String::class.java) ?: continue
                    if (username == "admin") continue

                    val password = userSnapshot.child("password").getValue(String::class.java) ?: ""
                    val phone = userSnapshot.child("phonenumber").getValue(String::class.java) ?: ""

                    // Don't read listCart & listFav to avoid deserialize errors
                    val user = UserModel(
                        username = username,
                        password = password,
                        phonenumber = phone,
                        listCart = emptyMap(),  // keep empty as not needed for display
                        listFav = emptyMap()
                    )
                    userList.add(user)
                    userCount++
                }
                
                // Update user count display
                binding.userCountText.text = "Total Users: $userCount"
                
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserManagementActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
                binding.userCountText.text = "Total Users: Error loading"
            }
        })
    }
}
