package com.example.shoes.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.shoes.Model.ItemsModel
import com.example.shoes.databinding.ActivityProductEditBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import androidx.activity.viewModels
import com.example.shoes.ViewModel.MainViewModel

class ProductEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductEditBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null
    private var productKey: String? = null
    private var existingItem: ItemsModel? = null
    private val mainViewModel: MainViewModel by viewModels()

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.productImageView.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        productKey = intent.getStringExtra("PRODUCT_KEY")
        existingItem = intent.getParcelableExtra<ItemsModel>("PRODUCT_ITEM")

        setupUI()
        mainViewModel.loadBrand()
        observeBrandData()
        setupClickListeners()
    }
    private fun observeBrandData() {
        mainViewModel.brands.observe(this) { brandList ->
            val brandTitles = brandList.map { it.title }
            val brandAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, brandTitles)
            binding.productBrandDropdown.setAdapter(brandAdapter)

            // Gán lại brand nếu đang sửa
            existingItem?.let {
                binding.productBrandDropdown.setText(it.brand, false)
            }
        }
    }

    private fun setupUI() {
        existingItem?.let { item ->
            binding.productTitleEditText.setText(item.title)
            binding.productDescriptionEditText.setText(item.description)
            binding.productPriceEditText.setText(item.price.toString())
            binding.productRatingEditText.setText(item.rating.toString())
            binding.productSizesEditText.setText(item.size.joinToString(", "))
            binding.productBrandDropdown.setText(item.brand, false)

            if (item.picUrl.isNotEmpty()) {
                Glide.with(this).load(item.picUrl[0]).into(binding.productImageView)
            }
            binding.addProductButton.text = "Update Product"
        } ?: run {
            binding.addProductButton.text = "Add Product"
        }
    }

    private fun setupClickListeners() {
        binding.selectImageButton.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.addProductButton.setOnClickListener {
            val title = binding.productTitleEditText.text.toString()
            val description = binding.productDescriptionEditText.text.toString()
            val price = binding.productPriceEditText.text.toString().toDoubleOrNull() ?: 0.0
            val rating = binding.productRatingEditText.text.toString().toDoubleOrNull() ?: 0.0
            val sizes = binding.productSizesEditText.text.toString().split(",").map { it.trim() }
            val brand = binding.productBrandDropdown.text.toString()

            if (title.isNotEmpty() && brand.isNotEmpty() && (imageUri != null || existingItem?.picUrl?.isNotEmpty() == true)) {
                uploadImageAndSaveProduct(title, description, price, rating, sizes, brand)
            } else {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageAndSaveProduct(title: String, description: String, price: Double, rating: Double, sizes: List<String>, brand: String) {
        if (imageUri != null) {
            val fileName = UUID.randomUUID().toString()
            val refStorage = storage.reference.child("products/$fileName")
            val uploadTask = refStorage.putFile(imageUri!!)

            uploadTask.addOnSuccessListener {
                refStorage.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveProduct(title, description, price, rating, sizes, brand, downloadUrl.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            saveProduct(title, description, price, rating, sizes, brand, existingItem?.picUrl?.get(0) ?: "")
        }
    }

    private fun saveProduct(
        title: String,
        description: String,
        price: Double,
        rating: Double,
        sizes: List<String>,
        brand: String,
        imageUrl: String
    ) {
        val itemRef = database.getReference("Items")

        if (productKey != null) {
            // 🔁 Cập nhật sản phẩm
            val ref = itemRef.child(productKey!!)
            val item = ItemsModel(
                title = title,
                description = description,
                picUrl = arrayListOf(imageUrl),
                size = ArrayList(sizes),
                price = price,
                rating = rating,
                brand = brand
            )
            ref.setValue(item)
                .addOnSuccessListener {
                    Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show()
                }
        } else {
            // ➕ Thêm mới sản phẩm
            itemRef.get().addOnSuccessListener { snapshot ->
                // Tìm max id hiện tại (chỉ lấy các key là số)
                val maxId = snapshot.children.mapNotNull {
                    it.key?.toIntOrNull()
                }.maxOrNull() ?: -1
                val newId = (maxId + 1).toString()

                val item = ItemsModel(
                    title = title,
                    description = description,
                    picUrl = arrayListOf(imageUrl),
                    size = ArrayList(sizes),
                    price = price,
                    rating = rating,
                    brand = brand
                )

                itemRef.child(newId).setValue(item)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

}
