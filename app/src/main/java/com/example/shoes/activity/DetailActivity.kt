package com.example.shoes.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoes.Adapter.ColorAdapter
import com.example.shoes.Adapter.SizeAdapter
import com.example.shoes.Adapter.SliderAdapter
import com.example.shoes.Helper.ManagmentCart
import com.example.shoes.Model.ItemsModel
import com.example.shoes.Model.SliderModel
import com.example.shoes.R
import com.example.shoes.databinding.ActivityDetailBinding
import java.util.ResourceBundle.getBundle

class DetailActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var item:ItemsModel
    private var numberOrder=1
    private lateinit var managmentCart: ManagmentCart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart=ManagmentCart(this)
        getBundle()
        banners()
        initLists()
    }

    private fun initLists() {
        val sizeList=ArrayList<String>()
        for (size in item.size){
            sizeList.add(size.toString())
        }
        binding.sizeList.adapter=SizeAdapter(sizeList)
        binding.sizeList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false )
        val colorList = ArrayList<String>()
        for (imageUrl in item.picUrl ){
            colorList.add(imageUrl)
        }

        binding.colorList.adapter=ColorAdapter(colorList)
        binding.colorList.layoutManager= LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
    }

    private fun banners() {
        val sliderItems=ArrayList<SliderModel>()
        for (imageUrl in item.picUrl){
            sliderItems.add(SliderModel(imageUrl))
        }
        binding.slider.adapter=SliderAdapter(sliderItems,binding.slider)
        binding.slider.clipToPadding=true
        binding.slider.clipChildren=true

        binding.slider.offscreenPageLimit=1


        if(sliderItems.size>1){
            binding.dotIndicator.visibility=View.VISIBLE
            binding.dotIndicator.attachTo(binding.slider)
        }

    }


    private fun getBundle(){
        item=intent.getParcelableExtra("object")!!

        binding.titleTxt.text=item.title
        binding.descriptionTxt.text=item.description
        binding.priceTxt.text="$"+item.price
        binding.ratingTxt.text="${item.rating} Rating"
        binding.addToCartBtn.setOnClickListener{
            item.numberInCart=numberOrder
            managmentCart.insertFood(item)
        }
        binding.backBtn.setOnClickListener{finish()}
        binding.cartBtn.setOnClickListener{
            startActivity(Intent(this@DetailActivity,CartActivity::class.java))
        }
    }
}