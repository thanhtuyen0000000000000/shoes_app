package com.example.shoes.activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoes.Adapter.CartAdapter
import com.example.shoes.Helper.ChangeNumberItemsListener
import com.example.shoes.Helper.ManagmentCart
import com.example.shoes.databinding.ActivityCartBinding
import kotlinx.coroutines.launch

class CartActivity : BaseActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var managmentCart: ManagmentCart
    private var tax: Double = 0.0
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            Log.d("CartActivity", "onCreate started")
            super.onCreate(savedInstanceState)
            
            Log.d("CartActivity", "Inflating binding")
            binding = ActivityCartBinding.inflate(layoutInflater)
            setContentView(binding.root)

            Log.d("CartActivity", "Initializing ManagmentCart")
            managmentCart = ManagmentCart(this)
            
            Log.d("CartActivity", "Setting up RecyclerView")
            setupRecyclerView()
            
            Log.d("CartActivity", "Setting up variables")
            setVariable()
            
            Log.d("CartActivity", "Loading cart data")
            // Load cart data
            loadCartData()
            
            Log.d("CartActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e("CartActivity", "Error in onCreate: ${e.message}", e)
            // Hiển thị empty state nếu có lỗi
            try {
                binding?.let { showEmptyState(true) }
            } catch (bindingError: Exception) {
                Log.e("CartActivity", "Error accessing binding: ${bindingError.message}")
                finish() // Đóng activity nếu không thể init
            }
        }
    }

    private fun setupRecyclerView() {
        try {
            Log.d("CartActivity", "Setting up RecyclerView layout manager")
            binding.viewCart.layoutManager = LinearLayoutManager(this)
        } catch (e: Exception) {
            Log.e("CartActivity", "Error setting up RecyclerView: ${e.message}")
        }
    }

    private fun loadCartData() {
        lifecycleScope.launch {
            try {
                Log.d("CartActivity", "Starting to load cart data")
                showLoading(true)
                val cartList = managmentCart.getListCart()
                Log.d("CartActivity", "Loaded cart items: ${cartList.size}")
                
                // In ra thông tin từng item để debug
                cartList.forEachIndexed { index, item ->
                    Log.d("CartActivity", "Item $index: ${item.title}, quantity: ${item.numberInCart}")
                }
                
                initCartList(cartList)
            } catch (e: Exception) {
                Log.e("CartActivity", "Error loading cart: ${e.message}", e)
                showEmptyState(true)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun initCartList(cartList: ArrayList<com.example.shoes.Model.ItemsModel>) {
        try {
            Log.d("CartActivity", "Initializing cart list with ${cartList.size} items")
            cartAdapter = CartAdapter(cartList, this@CartActivity, object : ChangeNumberItemsListener {
                override fun onChanged() {
                    lifecycleScope.launch {
                        calculateCart()
                    }
                }
            })
            
            binding.viewCart.adapter = cartAdapter
            Log.d("CartActivity", "CartAdapter set successfully")
            
            // Hiển thị empty state hoặc content dựa trên kích thước list
            val isEmpty = cartList.isEmpty()
            showEmptyState(isEmpty)
            
            if (!isEmpty) {
                calculateCart()
            }
        } catch (e: Exception) {
            Log.e("CartActivity", "Error initializing cart list: ${e.message}", e)
            showEmptyState(true)
        }
    }

    private fun showEmptyState(show: Boolean) {
        try {
            Log.d("CartActivity", "Show empty state: $show")
            binding.emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
            binding.scrollView2.visibility = if (show) View.GONE else View.VISIBLE
            binding.checkoutSection.visibility = if (show) View.GONE else View.VISIBLE
        } catch (e: Exception) {
            Log.e("CartActivity", "Error updating empty state: ${e.message}")
        }
    }

    private fun showLoading(show: Boolean) {
        // Có thể thêm ProgressBar nếu cần
        Log.d("CartActivity", "Loading: $show")
    }

    private fun calculateCart() {
        lifecycleScope.launch {
            try {
                Log.d("CartActivity", "Calculating cart totals")
                val percentTax = 0.2
                val delivery = 10.0
                val totalFee = managmentCart.getTotalFee()
                
                tax = Math.round((totalFee * percentTax) * 100) / 100.0
                val total = Math.round((totalFee + tax + delivery) * 100) / 100
                val itemTotal = Math.round(totalFee * 100) / 100

                Log.d("CartActivity", "Calculated - itemTotal: $itemTotal, tax: $tax, total: $total")

                with(binding) {
                    totalFeeTxt.text = "$$itemTotal"
                    taxTxt.text = "$$tax"
                    deliveryTxt.text = "$$delivery"
                    totalTxt.text = "$$total"
                }
            } catch (e: Exception) {
                Log.e("CartActivity", "Error calculating cart: ${e.message}")
            }
        }
    }

    private fun setVariable() {

        try {
            Log.d("CartActivity", "Setting up click listeners")
            binding.backBtn.setOnClickListener {
                Log.d("CartActivity", "Back button clicked")
                finish()
            }
            binding.shopNowBtn?.setOnClickListener {
                Log.d("CartActivity", "Shop now button clicked")
                finish() // Quay về màn hình chính để mua sắm
            }
            binding.checkoutBtn.setOnClickListener {
                lifecycleScope.launch {
                Log.d("CartActivity", "Checkout button clicked")

                val cartList = managmentCart.getListCart()

                // Chuyển sang OrderActivity và truyền dữ liệu
                val intent = Intent(this@CartActivity, OrderActivity::class.java)

                intent.putParcelableArrayListExtra("cartList", cartList)
                intent.putExtra("total", binding.totalTxt.text.toString())
                intent.putExtra("tax", binding.taxTxt.text.toString())
                intent.putExtra("delivery", binding.deliveryTxt.text.toString())

                startActivity(intent)

                // Xóa giỏ hàng sau khi thanh toán
                managmentCart.clearCart()
                finish()
            }
            }
        } catch (e: Exception) {
            Log.e("CartActivity", "Error setting up variables: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("CartActivity", "onResume called - refreshing cart")
        // Refresh cart khi quay lại activity
        try {
            loadCartData()
        } catch (e: Exception) {
            Log.e("CartActivity", "Error in onResume: ${e.message}")
        }
    }
}