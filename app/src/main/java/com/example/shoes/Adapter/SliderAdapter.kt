package com.example.shoes.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import com.example.shoes.Model.SliderModel
import com.example.shoes.R

class SliderAdapter(
    private var sliderItems:List<SliderModel>,
    private val viewPager2:ViewPager2
):RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {
    private  lateinit var context:Context
//    private val runnable = kotlinx.coroutines.Runnable {
//        sliderItems=sliderItems
//        notifyDataSetChanged()
//    }
    private val runnable = Runnable {
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SliderViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.slider_item_container,parent,false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
      holder.setImage(sliderItems[position],context)
        if (position==sliderItems.lastIndex-1){
            viewPager2.post(runnable)
        }
    }

    override fun getItemCount(): Int=sliderItems.size
    class SliderViewHolder(iemView: View):RecyclerView.ViewHolder(iemView){
        private val imageView:ImageView=itemView.findViewById(R.id.imageSlide)
        fun setImage(sliderItems:SliderModel,context: Context){
            val requestOptions = RequestOptions().transform(CenterInside())

            Glide.with(context)
                .load(sliderItems.url)
                .apply(requestOptions)
                .into(imageView)
        }
    }
}