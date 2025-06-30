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
import com.example.shoes.databinding.ViewholderColorBinding
import com.example.shoes.databinding.ViewholderSizeBinding

// Interface để callback khi size được chọn
interface SizeSelectionListener {
    fun onSizeSelected(size: String, position: Int)
}

class SizeAdapter(
    val items: MutableList<String>,
    private val sizeSelectionListener: SizeSelectionListener? = null
) : RecyclerView.Adapter<SizeAdapter.Viewholder>() {
    
    private var selectedPosition = -1
    private var lastSelectedPosition = -1
    private lateinit var context: Context
    
    class Viewholder(val binding: ViewholderSizeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeAdapter.Viewholder {
        context = parent.context
        val binding = ViewholderSizeBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: SizeAdapter.Viewholder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.sizeTxt.text = items[position]

        holder.binding.root.setOnClickListener {
            lastSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)
            
            // Callback về DetailActivity khi size được chọn
            sizeSelectionListener?.onSizeSelected(items[position], position)
        }

        if (selectedPosition == position) {
            holder.binding.colorLayout.setBackgroundResource(R.drawable.grey_bg_selected)
            holder.binding.sizeTxt.setTextColor(context.resources.getColor(R.color.purple))
        } else {
            holder.binding.colorLayout.setBackgroundResource(R.drawable.grey_bg)
            holder.binding.sizeTxt.setTextColor(context.resources.getColor(R.color.black))
        }
    }

    override fun getItemCount(): Int = items.size
    
    // Phương thức để lấy size đã chọn
    fun getSelectedSize(): String? {
        return if (selectedPosition != -1 && selectedPosition < items.size) {
            items[selectedPosition]
        } else {
            null
        }
    }
    
    // Phương thức để lấy position đã chọn
    fun getSelectedPosition(): Int {
        return selectedPosition
    }
    
    // Phương thức để kiểm tra đã chọn size chưa
    fun isSizeSelected(): Boolean {
        return selectedPosition != -1
    }
    
    // Phương thức để reset selection
    fun clearSelection() {
        val oldSelected = selectedPosition
        selectedPosition = -1
        if (oldSelected != -1) {
            notifyItemChanged(oldSelected)
        }
    }
}