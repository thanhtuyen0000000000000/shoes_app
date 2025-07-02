package com.example.shoes.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoes.Model.ItemsModel
import com.example.shoes.databinding.ActivityProductManagementBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProductManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductManagementBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        setupUI()
        setupRecyclerView()
        setupClickListeners()
        loadProducts()
    }

    private fun setupUI() {
        // Back button functionality
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onEditClick = { item, key ->
                val intent = Intent(this, ProductEditActivity::class.java)
                intent.putExtra("PRODUCT_KEY", key)
                intent.putExtra("PRODUCT_ITEM", item)
                startActivity(intent)
            },
            onDeleteClick = { key, imageUrl ->
                deleteProduct(key, imageUrl)
            }
        )
        binding.productRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProductManagementActivity)
            this.adapter = this@ProductManagementActivity.adapter
        }
    }

    private fun setupClickListeners() {
        // Updated to use the new card design
        binding.addProductCard.setOnClickListener {
            startActivity(Intent(this, ProductEditActivity::class.java))
        }
        
        // Also keep the button click for backwards compatibility
        binding.addProductButton.setOnClickListener {
            startActivity(Intent(this, ProductEditActivity::class.java))
        }
    }

    private fun loadProducts() {
        database.getReference("Items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<Pair<String, ItemsModel>>()
                for (data in snapshot.children) {
                    val item = data.getValue(ItemsModel::class.java)
                    item?.let {
                        products.add(Pair(data.key!!, it))
                    }
                }
                adapter.submitList(products)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProductManagementActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteProduct(key: String, imageUrl: String) {
        storage.getReferenceFromUrl(imageUrl).delete()
            .addOnSuccessListener {
                database.getReference("Items").child(key).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete product", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show()
            }
    }
}