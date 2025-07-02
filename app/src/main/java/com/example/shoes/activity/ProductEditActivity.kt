package com.example.shoes.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.shoes.Model.ItemsModel
import com.example.shoes.Model.SliderModel
import com.example.shoes.ViewModel.MainViewModel
import com.example.shoes.databinding.ActivityProductEditBinding
import com.example.shoes.Adapter.SliderAdapter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ProductEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductEditBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var productKey: String? = null
    private var existingItem: ItemsModel? = null
    private val mainViewModel: MainViewModel by viewModels()
    private var imageUris: List<Uri> = emptyList()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                imageUris = uris
                showImagesInSlider(uris) // ✅ Hiển thị slide ảnh mới chọn
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        productKey = intent.getStringExtra("PRODUCT_KEY")
        existingItem = intent.getParcelableExtra("PRODUCT_ITEM")

        setupUI()
        mainViewModel.loadBrand()
        observeBrandData()
        setupClickListeners()
    }

    private fun observeBrandData() {
        mainViewModel.brands.observe(this) { brandList ->
            val brandTitles = brandList.map { it.title }
            val brandAdapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, brandTitles)
            binding.productBrandDropdown.setAdapter(brandAdapter)

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
                showImagesInSlider(item.picUrl)
            }

            binding.addProductButton.text = "Update Product"
        } ?: run {
            binding.addProductButton.text = "Add Product"
        }
    }

    private fun setupClickListeners() {
        // Back button listener
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.selectImageButton.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.addProductButton.setOnClickListener {
            val title = binding.productTitleEditText.text.toString()
            val description = binding.productDescriptionEditText.text.toString()
            val price = binding.productPriceEditText.text.toString().toDoubleOrNull() ?: 0.0
            val rating = binding.productRatingEditText.text.toString().toDoubleOrNull() ?: 0.0
            val sizes =
                binding.productSizesEditText.text.toString().split(",").map { it.trim() }
            val brand = binding.productBrandDropdown.text.toString()

            val hasExistingImages = existingItem?.picUrl?.isNotEmpty() == true
            val hasNewImages = imageUris.isNotEmpty()

            if (title.isNotEmpty() && brand.isNotEmpty() && (hasNewImages || hasExistingImages)) {
                uploadImageAndSaveProduct(title, description, price, rating, sizes, brand)
            } else {
                Toast.makeText(
                    this,
                    "Please fill all fields and select at least one image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ✅ Convert Any (Uri or String) to SliderModel
    private fun convertToSliderModels(imageList: List<Any>): List<SliderModel> {
        return imageList.map {
            SliderModel(
                url = when (it) {
                    is Uri -> it.toString()
                    is String -> it
                    else -> ""
                }
            )
        }
    }

    // ✅ Show Slider using SliderAdapter
    private fun showImagesInSlider(imageList: List<Any>) {
        val sliderItems = convertToSliderModels(imageList)
        val adapter = SliderAdapter(sliderItems, binding.imageSlider)
        binding.imageSlider.adapter = adapter
    }

    private fun uploadImageAndSaveProduct(
        title: String,
        description: String,
        price: Double,
        rating: Double,
        sizes: List<String>,
        brand: String
    ) {
        if (imageUris.isNotEmpty()) {
            val uploadedUrls = mutableListOf<String>()
            var uploadedCount = 0

            for (uri in imageUris) {
                val fileName = UUID.randomUUID().toString()
                val refStorage = storage.reference.child("products/$fileName")

                refStorage.putFile(uri).addOnSuccessListener {
                    refStorage.downloadUrl.addOnSuccessListener { downloadUrl ->
                        uploadedUrls.add(downloadUrl.toString())
                        uploadedCount++

                        if (uploadedCount == imageUris.size) {
                            saveProduct(title, description, price, rating, sizes, brand, uploadedUrls)
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val urls = existingItem?.picUrl ?: arrayListOf()
            saveProduct(title, description, price, rating, sizes, brand, urls)
        }
    }

    private fun saveProduct(
        title: String,
        description: String,
        price: Double,
        rating: Double,
        sizes: List<String>,
        brand: String,
        imageUrls: List<String>
    ) {
        val itemRef = database.getReference("Items")

        if (productKey != null) {
            val ref = itemRef.child(productKey!!)
            val item = ItemsModel(
                title = title,
                description = description,
                picUrl = ArrayList(imageUrls),
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
            itemRef.get().addOnSuccessListener { snapshot ->
                val maxId = snapshot.children.mapNotNull { it.key?.toIntOrNull() }.maxOrNull() ?: -1
                val newId = (maxId + 1).toString()

                val item = ItemsModel(
                    title = title,
                    description = description,
                    picUrl = ArrayList(imageUrls),
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
