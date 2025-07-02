package com.example.shoes.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shoes.Adapter.PopularAdapter
import com.example.shoes.Model.ItemsModel
import com.example.shoes.R
import com.example.shoes.ViewModel.MainViewModel
import com.example.shoes.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val viewModel = MainViewModel()
    private val allProducts = mutableListOf<ItemsModel>()
    private var searchAdapter: PopularAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        loadProducts()
        setupSearch()
    }

    private fun setupViews() {
        // Setup RecyclerView
        binding.recyclerViewSearchResults.layoutManager = GridLayoutManager(this, 2)
        
        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }
        
        // Clear search button
        binding.clearSearchButton.setOnClickListener {
            binding.searchEditText.setText("")
            binding.searchEditText.requestFocus()
        }
    }

    private fun loadProducts() {
        binding.progressBarSearch.visibility = View.VISIBLE
        
        viewModel.populars.observe(this, Observer { items ->
            allProducts.clear()
            allProducts.addAll(items)
            
            // Hiển thị tất cả sản phẩm ban đầu
            displayProducts(allProducts)
            binding.progressBarSearch.visibility = View.GONE
            
            Log.d("SearchActivity", "Loaded ${allProducts.size} products")
        })
        
        viewModel.loadPopular()
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                performSearch(query)
                
                // Hiển thị/ẩn nút clear
                binding.clearSearchButton.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }
        })

        // Focus vào search EditText khi mở activity
        binding.searchEditText.requestFocus()
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            // Hiển thị tất cả sản phẩm khi không có từ khóa
            displayProducts(allProducts)
            return
        }

        // Tìm kiếm theo title, brand, description
        val filteredProducts = allProducts.filter { product ->
            product.title.contains(query, ignoreCase = true) ||
            product.brand.contains(query, ignoreCase = true) ||
            (product.description?.contains(query, ignoreCase = true) == true)
        }

        displayProducts(filteredProducts)
        
        Log.d("SearchActivity", "Search '$query' found ${filteredProducts.size} results")
    }

    private fun displayProducts(products: List<ItemsModel>) {
        if (products.isEmpty()) {
            // Hiển thị thông báo không tìm thấy
            binding.recyclerViewSearchResults.visibility = View.GONE
            binding.emptySearchTextView.visibility = View.VISIBLE
            binding.resultCountTextView.text = getString(R.string.no_results_found)
        } else {
            // Hiển thị danh sách sản phẩm
            binding.recyclerViewSearchResults.visibility = View.VISIBLE
            binding.emptySearchTextView.visibility = View.GONE
            binding.resultCountTextView.text = getString(R.string.found_products, products.size)
            
            searchAdapter = PopularAdapter(products.toMutableList())
            binding.recyclerViewSearchResults.adapter = searchAdapter
        }
    }
} 