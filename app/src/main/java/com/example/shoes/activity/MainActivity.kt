package com.example.shoes.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.shoes.Adapter.BrandAdapter
import com.example.shoes.Adapter.PopularAdapter
import com.example.shoes.Model.SliderModel
import com.example.shoes.Adapter.SliderAdapter
import com.example.shoes.Model.ItemsModel
import com.example.shoes.ViewModel.MainViewModel
import com.example.shoes.databinding.ActivityMainBinding
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private val viewModel=MainViewModel()
    private lateinit var binding: ActivityMainBinding
    private val allPopularItems = mutableListOf<ItemsModel>()
    private var selectedBrand: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val username = intent.getStringExtra("username")
        Log.d("MainActivity", "Received username: $username")
        
        if (username != null) {
            // Lưu username vào SharedPreferences để các activity khác sử dụng
            saveUserSession(username)
            getUserInfo(username)
        } else {
            Log.w("MainActivity", "No username provided")
            // Kiểm tra xem có session được lưu trước đó không
            checkExistingSession()
        }
        
        initBanner()
        initBrand()
        initPopular()
        initBottomMenu()
    }
    
    private fun saveUserSession(username: String) {
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("username", username)
            apply()
        }
        Log.d("MainActivity", "Saved username to SharedPrefs: $username")
    }
    
    private fun checkExistingSession() {
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", null)
        Log.d("MainActivity", "Checking existing session: $savedUsername")
        
        if (savedUsername != null) {
            getUserInfo(savedUsername)
        } else {
            Log.w("MainActivity", "No existing session found")
            binding.usernameTextView.text = "Guest"
        }
    }

    private fun getUserInfo(username: String) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        usersRef.child(username).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val name = snapshot.child("username").value.toString()
                    binding.usernameTextView.text = name
                    Log.d("MainActivity", "Successfully loaded user info: $name")
                } else {
                    Log.w("MainActivity", "User not found in database: $username")
                    binding.usernameTextView.text = "User"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Failed to load user info: ${exception.message}")
                binding.usernameTextView.text = "User"
            }
    }

    private fun initBottomMenu() {
        binding.cartBtn.setOnClickListener {
            try {
                Log.d("MainActivity", "Cart button clicked")
                
                // Kiểm tra xem user đã login chưa
                val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                val username = sharedPref.getString("username", null)
                
                if (username != null) {
                    Log.d("MainActivity", "Opening CartActivity for user: $username")
                    val intent = Intent(this@MainActivity, CartActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.w("MainActivity", "User not logged in, redirecting to login")
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error opening CartActivity: ${e.message}")
            }
        }
        
        binding.favBtn.setOnClickListener {
            try {
                Log.d("MainActivity", "Favorites button clicked")
                
                val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                val username = sharedPref.getString("username", null)
                
                if (username != null) {
                    Log.d("MainActivity", "Opening FavActivity for user: $username")
                    val intent = Intent(this@MainActivity, FavActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.w("MainActivity", "User not logged in, redirecting to login")
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error opening FavActivity: ${e.message}")
            }
        }
        
        binding.profileBtn.setOnClickListener {
            try {
                Log.d("MainActivity", "Profile button clicked")
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error opening ProfileActivity: ${e.message}")
            }
        }
        binding.orderBtn.setOnClickListener{
            try {
                Log.d("MainActivity", "Order button clicked")
                val intent = Intent(this, OrderActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error opening OrderActivity: ${e.message}")
            }
        }
    }

    private fun initBanner(){
        binding.progressBarBanner.visibility= View.VISIBLE
        viewModel.banners.observe(this, Observer { items ->
            banners(items)
            binding.progressBarBanner.visibility = View.GONE
        })
        viewModel.loadBanner()
    }
    
    private fun banners(images:List<SliderModel>){
        Log.d("MainActivity", "Loaded banner size: ${images.size}")
        binding.viewpagerSlider.adapter= SliderAdapter(images,binding.viewpagerSlider)
        binding.viewpagerSlider.clipToPadding=false
        binding.viewpagerSlider.clipChildren=false
        binding.viewpagerSlider.offscreenPageLimit=3
        binding.viewpagerSlider.getChildAt(0).overScrollMode=RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer=CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
        }

        binding.viewpagerSlider.setPageTransformer(compositePageTransformer)
        if(images.size>1)
        {
            binding.dotIndicator.visibility=View.VISIBLE
            binding.dotIndicator.attachTo(binding.viewpagerSlider)
        }
    }
    
//    private fun initBrand(){
//        binding.progressBarBrand.visibility= View.VISIBLE
//        viewModel.brands.observe(this, Observer { items ->
//            binding.viewBrand.layoutManager=
//                LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
//            binding.viewBrand.adapter=BrandAdapter(items)
//            binding.progressBarBrand.visibility = View.GONE
//        })
//        viewModel.loadBrand()
//    }

    private fun initBrand() {
        binding.progressBarBrand.visibility = View.VISIBLE

        viewModel.brands.observe(this) { items ->
            binding.viewBrand.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)

            // ✅ Truyền callback khi chọn brand
            binding.viewBrand.adapter = BrandAdapter(items.toMutableList()) { selectedBrand ->
                filterPopularByBrand(selectedBrand)
            }

            binding.progressBarBrand.visibility = View.GONE
        }

        viewModel.loadBrand()
    }

    private fun filterPopularByBrand(brand: String?) {
        selectedBrand = brand
        val filteredItems = if (brand == null) {
            allPopularItems
        } else {
            allPopularItems.filter { it.brand.equals(brand, ignoreCase = true) }
        }
        binding.viewPopular.adapter = PopularAdapter(filteredItems.toMutableList())
    }

    private fun initPopular() {
        binding.progressBarPopular.visibility = View.VISIBLE
        viewModel.populars.observe(this) { items ->
            allPopularItems.clear()
            allPopularItems.addAll(items)

            binding.viewPopular.layoutManager = GridLayoutManager(this@MainActivity, 2)
            binding.viewPopular.adapter = PopularAdapter(items)

            binding.progressBarPopular.visibility = View.GONE
        }

        viewModel.loadPopular()
    }

//    private fun initPopular(){
//        binding.progressBarPopular.visibility= View.VISIBLE
//        viewModel.populars.observe(this, Observer { items ->
//            binding.viewPopular.layoutManager = GridLayoutManager(this@MainActivity,2)
//            binding.viewPopular.adapter= PopularAdapter(items)
//            binding.progressBarPopular.visibility = View.GONE
//        })
//        viewModel.loadPopular()
//    }
}