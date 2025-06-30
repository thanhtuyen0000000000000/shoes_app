package com.example.shoes.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoes.Adapter.FavAdapter
import com.example.shoes.Helper.ManagmentFav
import com.example.shoes.Model.ItemsModel
import com.example.shoes.databinding.ActivityFavBinding
import kotlinx.coroutines.launch

class FavActivity : BaseActivity() {

    private lateinit var binding: ActivityFavBinding
    private lateinit var managmentFav: ManagmentFav
    private lateinit var favAdapter: FavAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentFav = ManagmentFav(this)
        setupRecyclerView()
        setVariable()

        loadFavoritesData()
    }

    private fun setupRecyclerView() {
        binding.viewCart.layoutManager = LinearLayoutManager(this)
    }

    private fun setVariable() {
        binding.backBtn.setOnClickListener { finish() }
        binding.exploreBtn?.setOnClickListener {
            finish()
        }
    }

    private fun loadFavoritesData() {
        lifecycleScope.launch {
            try {
                showLoading(true)

                // ✅ Nhận cả list sản phẩm và list key
                val (favList, favKeyList) = managmentFav.getListFav()
                Log.d("FavActivity", "Loaded favorite items: ${favList.size}")

                favList.forEachIndexed { index, item ->
                    Log.d("FavActivity", "Item $index: ${item.title}")
                }

                initFavList(favList, favKeyList)
            } catch (e: Exception) {
                Log.e("FavActivity", "Error loading favorites: ${e.message}")
                showEmptyState(true)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun initFavList(
        favList: ArrayList<ItemsModel>,
        favKeyList: ArrayList<String>
    ) {
        favAdapter = FavAdapter(favList, favKeyList, this@FavActivity) {
            refreshFavorites()
        }

        binding.viewCart.adapter = favAdapter
        showEmptyState(favList.isEmpty())
    }

    private fun refreshFavorites() {
        Log.d("FavActivity", "Refreshing favorites list...")
        loadFavoritesData()
    }

    private fun showEmptyState(show: Boolean) {
        Log.d("FavActivity", "Show empty state: $show")
        binding.emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        binding.scrollView2.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showLoading(show: Boolean) {
        Log.d("FavActivity", "Loading: $show")
        // Optionally handle progress UI
    }

    override fun onResume() {
        super.onResume()
        loadFavoritesData()
    }
}
