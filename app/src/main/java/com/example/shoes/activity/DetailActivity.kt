package com.example.shoes.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoes.Adapter.ColorAdapter
import com.example.shoes.Adapter.SizeAdapter
import com.example.shoes.Adapter.SizeSelectionListener
import com.example.shoes.Adapter.SliderAdapter
import com.example.shoes.Helper.ManagmentCart
import com.example.shoes.Helper.ManagmentFav
import com.example.shoes.Model.ItemsModel
import com.example.shoes.Model.SliderModel
import com.example.shoes.R
import com.example.shoes.databinding.ActivityDetailBinding
import com.example.shoes.databinding.DialogAddToCartSuccessBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ResourceBundle.getBundle
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions

class DetailActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel
    private var numberOrder = 1
    private lateinit var managmentCart: ManagmentCart
    private lateinit var managmentFav: ManagmentFav
    private lateinit var sizeAdapter: SizeAdapter
    private var selectedSize: String? = null
    private var productId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart = ManagmentCart(this)
        managmentFav = ManagmentFav(this)
        getBundle()
        banners()
        initLists()
    }

    private fun initLists() {
        // Setup Size List với callback
        val sizeList = ArrayList<String>()
        for (size in item.size) {
            sizeList.add(size.toString())
        }
        
        sizeAdapter = SizeAdapter(sizeList, object : SizeSelectionListener {
            override fun onSizeSelected(size: String, position: Int) {
                selectedSize = size
                Log.d("DetailActivity", "=== Size Selection Debug ===")
                Log.d("DetailActivity", "Product: ${item.title}")
                Log.d("DetailActivity", "Available sizes: ${item.size}")
                Log.d("DetailActivity", "Size selected: $size at position: $position")
                Log.d("DetailActivity", "Previous selectedSize: $selectedSize")
                Log.d("DetailActivity", "============================")
                
                // Hiển thị thông báo size đã chọn
                Toast.makeText(this@DetailActivity, getString(R.string.size_selected, size), Toast.LENGTH_SHORT).show()
            }
        })
        
        binding.sizeList.adapter = sizeAdapter
        binding.sizeList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        
        // Setup Color List
        val colorList = ArrayList<String>()
        for (imageUrl in item.picUrl) {
            colorList.add(imageUrl)
        }

        binding.colorList.adapter = ColorAdapter(colorList) { selectedPosition ->
            binding.slider.setCurrentItem(selectedPosition, true)
        }
        binding.colorList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun banners() {
        val sliderItems = ArrayList<SliderModel>()
        for (imageUrl in item.picUrl) {
            sliderItems.add(SliderModel(imageUrl))
        }
        binding.slider.adapter = SliderAdapter(sliderItems, binding.slider)
        binding.slider.clipToPadding = true
        binding.slider.clipChildren = true

        binding.slider.offscreenPageLimit = 1

        if (sliderItems.size > 1) {
            binding.dotIndicator.visibility = View.VISIBLE
            binding.dotIndicator.attachTo(binding.slider)
        }
    }
    
    private fun updateFavIcon() {
        CoroutineScope(Dispatchers.Main).launch {
            val isFav = managmentFav.isFavorite(productId)
            if (isFav) {
                binding.favBtn.setImageResource(R.drawable.fav_icon_filled)
            } else {
                binding.favBtn.setImageResource(R.drawable.fav_icon)
            }
        }
    }

    private fun validateAndAddToCart() {
        // Kiểm tra xem đã chọn size chưa
        if (!sizeAdapter.isSizeSelected() || selectedSize == null) {
            // Hiển thị dialog yêu cầu chọn size
            showSizeSelectionDialog()
            return
        }
        
        // Nếu đã chọn size, thêm vào cart
        addToCart()
    }
    
    private fun showSizeSelectionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.size_required))
        builder.setMessage(getString(R.string.size_required_message, item.size.joinToString(", ")))
        
        // Sử dụng icon warning thay vì shoes
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        
        builder.setPositiveButton(getString(R.string.select_now)) { dialog, _ ->
            dialog.dismiss()
            // Scroll đến phần size selection để user dễ thấy
            binding.sizeList.smoothScrollToPosition(0)
            
            // Highlight size selection area
            highlightSizeSelection()
        }
        
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        
        // Không cho phép dismiss dialog bằng cách bấm ngoài
        builder.setCancelable(true)
        
        val dialog = builder.create()
        dialog.show()
        
        // Tùy chỉnh màu button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.purple))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.grey))
    }
    
    private fun highlightSizeSelection() {
        // Focus vào size selection area
        binding.sizeList.requestFocus()
        
        // Animation để thu hút sự chú ý với hiệu ứng bounce
        binding.sizeList.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .withEndAction {
                binding.sizeList.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .withEndAction {
                        // Lặp lại hiệu ứng một lần nữa để thu hút chú ý
                        binding.sizeList.animate()
                            .scaleX(1.05f)
                            .scaleY(1.05f)
                            .setDuration(150)
                            .withEndAction {
                                binding.sizeList.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(150)
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
            
        // Hiển thị toast hướng dẫn với emoji và vibration nếu có
        Toast.makeText(this, getString(R.string.please_select_size), Toast.LENGTH_LONG).show()
        
        // Thêm haptic feedback nếu có
        try {
            @Suppress("DEPRECATION")
            binding.sizeList.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        } catch (e: Exception) {
            // Ignore nếu không support haptic feedback
        }
    }
    
    private fun addToCart() {
        try {
            Log.d("DetailActivity", "Adding to cart - Selected size: $selectedSize")
            
            // Validation bổ sung
            if (selectedSize.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.error_size_selection), Toast.LENGTH_SHORT).show()
                return
            }
            
            // Clone item để không ảnh hưởng đến object gốc
            val cartItem = item.copy()
            cartItem.numberInCart = numberOrder
            
            // Đảm bảo size được chọn được lưu vào item
            selectedSize?.let { chosenSize ->
                Log.d("DetailActivity", "Original sizes: ${item.size}")
                Log.d("DetailActivity", "Chosen size: $chosenSize")
                
                // Cập nhật size đã chọn vào đầu danh sách size của item
                val updatedSizes = ArrayList<String>()
                updatedSizes.add(chosenSize)
                
                // Thêm các size khác (trừ size đã chọn) để giữ nguyên dữ liệu
                item.size.forEach { size ->
                    if (size != chosenSize) {
                        updatedSizes.add(size)
                    }
                }
                cartItem.size = updatedSizes
                
                Log.d("DetailActivity", "Updated sizes: ${cartItem.size}")
                Log.d("DetailActivity", "First size (should be selected): ${cartItem.size[0]}")
            }
            
            managmentCart.insertShoe(cartItem) {
                Log.d("DetailActivity", "Successfully added to cart")
                // Hiển thị thông báo thành công với size đã chọn
                showSuccessMessage()
            }
            
        } catch (e: Exception) {
            Log.e("DetailActivity", "Error adding to cart: ${e.message}")
            Toast.makeText(this, getString(R.string.error_add_to_cart, e.message), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showSuccessMessage() {
        // Tạo custom dialog đẹp với layout riêng
        val dialog = Dialog(this)
        val dialogBinding = DialogAddToCartSuccessBinding.inflate(layoutInflater)
        
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        
        // Populate data vào dialog
        setupDialogContent(dialogBinding)
        
        // Setup click listeners
        setupDialogClickListeners(dialog, dialogBinding)
        
        // Hiển thị dialog với animation
        showDialogWithAnimation(dialog, dialogBinding)
    }
    
    private fun setupDialogContent(dialogBinding: DialogAddToCartSuccessBinding) {
        // Set product info
        dialogBinding.productTitle.text = item.title
        dialogBinding.productSize.text = selectedSize ?: "N/A"
        dialogBinding.productPrice.text = "$${item.price}"
        
        // Load product image với Glide
        if (item.picUrl.isNotEmpty()) {
            Glide.with(this)
                .load(item.picUrl[0])
                .apply(RequestOptions().transform(CenterCrop()))
                .placeholder(R.drawable.shoes)
                .error(R.drawable.shoes)
                .into(dialogBinding.productImage)
        } else {
            dialogBinding.productImage.setImageResource(R.drawable.shoes)
        }
    }
    
    private fun setupDialogClickListeners(dialog: Dialog, dialogBinding: DialogAddToCartSuccessBinding) {
        // Close button
        dialogBinding.closeBtn.setOnClickListener {
            // Haptic feedback
            try {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (e: Exception) { /* Ignore */ }
            
            dismissDialogWithAnimation(dialog, dialogBinding)
        }
        
        // Continue shopping button với animation effect
        dialogBinding.continueShoppingBtn.setOnClickListener {
            // Haptic feedback
            try {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (e: Exception) { /* Ignore */ }
            
            // Scale animation khi click
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction {
                            dismissDialogWithAnimation(dialog, dialogBinding)
                        }
                        .start()
                }
                .start()
                
            Log.d("DetailActivity", "Continue shopping clicked")
        }
        
        // View cart button với animation effect
        dialogBinding.viewCartBtn.setOnClickListener {
            // Haptic feedback
            try {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (e: Exception) { /* Ignore */ }
            
            // Scale animation khi click
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction {
                            dismissDialogWithAnimation(dialog, dialogBinding)
                            
                            // Navigate to CartActivity
                            val intent = Intent(this@DetailActivity, CartActivity::class.java)
                            startActivity(intent)
                        }
                        .start()
                }
                .start()
                
            Log.d("DetailActivity", "View cart clicked")
        }
        
        // Dismiss when clicking outside
        dialog.setOnCancelListener {
            dismissDialogWithAnimation(dialog, dialogBinding)
        }
    }
    
    private fun showDialogWithAnimation(dialog: Dialog, dialogBinding: DialogAddToCartSuccessBinding) {
        dialog.show()
        
        // Set dialog size với margin đẹp hơn
        val window = dialog.window
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.92).toInt()
        
        window?.setLayout(
            width,
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        
        // Animation vào: Scale + Fade cho toàn bộ dialog
        dialogBinding.root.scaleX = 0.8f
        dialogBinding.root.scaleY = 0.8f
        dialogBinding.root.alpha = 0f
        
        dialogBinding.root.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(350)
            .setInterpolator(android.view.animation.OvershootInterpolator(1.1f))
            .start()
            
        // Animation icon bounce với delay
        dialogBinding.iconCard.scaleX = 0f
        dialogBinding.iconCard.scaleY = 0f
        dialogBinding.iconCard.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .setStartDelay(200)
            .setInterpolator(android.view.animation.BounceInterpolator())
            .start()
            
        // Animation success title fade in
        dialogBinding.successTitle.alpha = 0f
        dialogBinding.successTitle.translationY = -30f
        dialogBinding.successTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
            
        // Animation product card slide up
        dialogBinding.productInfoCard.translationY = 120f
        dialogBinding.productInfoCard.alpha = 0f
        dialogBinding.productInfoCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(450)
            .setStartDelay(350)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
            
        // Animation buttons slide up từ bottom
        val buttonsContainer = dialogBinding.root.getChildAt(4) // LinearLayout với buttons
        buttonsContainer.translationY = 80f
        buttonsContainer.alpha = 0f
        buttonsContainer.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(450)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
            
        // Animation close button fade in
        dialogBinding.closeBtn.alpha = 0f
        dialogBinding.closeBtn.scaleX = 0.5f
        dialogBinding.closeBtn.scaleY = 0.5f
        dialogBinding.closeBtn.animate()
            .alpha(0.7f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setStartDelay(500)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }
    
    private fun dismissDialogWithAnimation(dialog: Dialog, dialogBinding: DialogAddToCartSuccessBinding) {
        // Animation ra: Scale + Fade
        dialogBinding.root.animate()
            .scaleX(0.7f)
            .scaleY(0.7f)
            .alpha(0f)
            .setDuration(200)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                dialog.dismiss()
            }
            .start()
    }

    private fun getBundle() {
        item = intent.getParcelableExtra("object")!!
        productId = intent.getStringExtra("productId") ?: item.title
        binding.titleTxt.text = item.title
        binding.descriptionTxt.text = item.description
        binding.priceTxt.text = "$" + item.price
        binding.ratingTxt.text = "${item.rating} Rating"
        binding.brandTxt.text = item.brand

        updateFavIcon()

        // Cập nhật click listener cho Add to Cart button
        binding.addToCartBtn.setOnClickListener {
            Log.d("DetailActivity", "Add to Cart button clicked")
            validateAndAddToCart()
        }

        binding.favBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val isFav = managmentFav.isFavorite(productId)
                if (isFav) {
                    managmentFav.removeFav(productId) {
                        binding.favBtn.setImageResource(R.drawable.fav_icon)
                    }
                } else {
                    managmentFav.insertShoe(item,productId) {
                        binding.favBtn.setImageResource(R.drawable.fav_icon_filled)
                    }
                }
            }
        }

        binding.backBtn.setOnClickListener { finish() }
        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this@DetailActivity, CartActivity::class.java))
        }
    }
}