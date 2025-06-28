package com.example.shoes.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoes.Adapter.FavAdapter
import com.example.shoes.Helper.ManagmentFav
import com.example.shoes.databinding.ActivityFavBinding
import kotlinx.coroutines.launch

class FavActivity : BaseActivity() {

    private lateinit var binding: ActivityFavBinding
    private lateinit var managmentFav: ManagmentFav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentFav = ManagmentFav(this)

        setVariable()
        initFavList()
    }

    private fun setVariable() {
        binding.backBtn.setOnClickListener { finish() }
        binding.viewCart.layoutManager = LinearLayoutManager(this)
    }

    private fun initFavList() {
        lifecycleScope.launch {
            val favList = managmentFav.getListFav()
            binding.viewCart.adapter = FavAdapter(favList, this@FavActivity) {
                refreshFavorites()
            }

            updateEmptyState(favList.isEmpty())
        }
    }

    private fun refreshFavorites() {
        initFavList() // Load lại danh sách khi có thay đổi
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyTxt.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.scrollView2.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    override fun onResume() {
        super.onResume()
        initFavList()
    }






}
