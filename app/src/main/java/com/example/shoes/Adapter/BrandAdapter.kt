//package com.example.shoes.Adapter
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.res.ColorStateList
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.widget.ImageViewCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.shoes.Model.BrandModel
//import com.example.shoes.R
//import com.example.shoes.databinding.ViewholderBrandBinding
//
//class BrandAdapter (val items:MutableList<BrandModel>, private val onBrandClick: (String?) -> Unit):
//    RecyclerView.Adapter<BrandAdapter.Viewholder>(){
//        private var selectedPosition = -1
//        private var lastSelectedPosition = -1
//    private lateinit var context: Context
//    class Viewholder(val binding: ViewholderBrandBinding):
//            RecyclerView.ViewHolder(binding.root){
//
//            }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandAdapter.Viewholder {
//        context=parent.context
//        val binding=ViewholderBrandBinding.inflate(LayoutInflater.from(context),parent,false)
//        return Viewholder(binding)
//    }
//    override fun onBindViewHolder(holder: BrandAdapter.Viewholder, @SuppressLint("RecyclerView") position: Int) {
//        val item=items[position]
//        holder.binding.title.text=item.title
//
//        Glide.with(holder.itemView.context)
//            .load(item.picUrl)
//            .into(holder.binding.pic)
//        holder.binding.root.setOnClickListener{
//
//            lastSelectedPosition=selectedPosition
//            selectedPosition=position
//            notifyItemChanged(lastSelectedPosition)
//            notifyItemChanged(selectedPosition)
//        }
//        onBrandClick(item.title)
//        holder.binding.title.setTextColor(context.resources.getColor(R.color.white))
//        if(selectedPosition==position){
//            holder.binding.pic.setBackgroundResource(0)
//            holder.binding.mainLayout.setBackgroundResource(R.drawable.purple_bg)
//            ImageViewCompat.setImageTintList(holder.binding.pic, ColorStateList.valueOf(context.getColor(R.color.white)))
//
//            holder.binding.title.visibility= View.VISIBLE
//        }else{
//            holder.binding.pic.setBackgroundResource(R.drawable.grey_bg)
//            holder.binding.mainLayout.setBackgroundResource(0)
//            ImageViewCompat.setImageTintList(holder.binding.pic, ColorStateList.valueOf(context.getColor(R.color.black)))
//
//            holder.binding.title.visibility= View.GONE
//        }
//    }
//    override fun getItemCount(): Int=items.size
//
//
//}


package com.example.shoes.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoes.Model.BrandModel
import com.example.shoes.R
import com.example.shoes.databinding.ViewholderBrandBinding

class BrandAdapter(
    private val items: MutableList<BrandModel>,
    private val onBrandClick: (String?) -> Unit // String? để truyền null khi bỏ chọn
) : RecyclerView.Adapter<BrandAdapter.Viewholder>() {

    private var selectedPosition = -1
    private lateinit var context: Context

    class Viewholder(val binding: ViewholderBrandBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        context = parent.context
        val binding = ViewholderBrandBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]
        holder.binding.title.text = item.title

        Glide.with(holder.itemView.context)
            .load(item.picUrl)
            .into(holder.binding.pic)

        holder.binding.root.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                if (selectedPosition == pos) {
                    // Nhấp lại cùng brand → bỏ chọn
                    val prev = selectedPosition
                    selectedPosition = -1
                    notifyItemChanged(prev)
                    onBrandClick(null) // gửi null để hiển thị all sản phẩm
                } else {
                    // Chọn brand mới
                    val prev = selectedPosition
                    selectedPosition = pos
                    notifyItemChanged(prev)
                    notifyItemChanged(selectedPosition)
                    onBrandClick(items[pos].title)
                }
            }
        }

        // Highlight UI
        if (selectedPosition == position) {
            holder.binding.pic.setBackgroundResource(0)
            holder.binding.mainLayout.setBackgroundResource(R.drawable.purple_bg)
            ImageViewCompat.setImageTintList(
                holder.binding.pic,
                ColorStateList.valueOf(context.getColor(R.color.white))
            )
            holder.binding.title.visibility = View.VISIBLE
        } else {
            holder.binding.pic.setBackgroundResource(R.drawable.grey_bg)
            holder.binding.mainLayout.setBackgroundResource(0)
            ImageViewCompat.setImageTintList(
                holder.binding.pic,
                ColorStateList.valueOf(context.getColor(R.color.black))
            )
            holder.binding.title.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size
}
