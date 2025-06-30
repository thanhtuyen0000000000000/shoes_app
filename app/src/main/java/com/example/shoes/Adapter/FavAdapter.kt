package com.example.shoes.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.shoes.Helper.ManagmentFav
import com.example.shoes.Model.ItemsModel
import com.example.shoes.activity.DetailActivity
import com.example.shoes.databinding.ViewholderFavBinding

class FavAdapter(
    private val favList: ArrayList<ItemsModel>,
    private val favKeyList: ArrayList<String>,  // 🔧 thêm danh sách key Firebase tương ứng
    private val context: Context,
    private val onFavChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<FavAdapter.ViewHolder>() {

    private val managmentFav = ManagmentFav(context)

    class ViewHolder(val binding: ViewholderFavBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderFavBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = favList[position]

        holder.binding.titleTxt.text = item.title
        holder.binding.brandTxt.text = "Brand: ${item.brand}"
        holder.binding.feeEachItem.text = "$${item.price}"

        Glide.with(holder.itemView.context)
            .load(item.picUrl[0])
            .apply(RequestOptions().transform(CenterCrop()))
            .into(holder.binding.pic)

        // 🗑 Nút xóa
        holder.binding.removeFavBtn.setOnClickListener {
            removeFromFavorites(position)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("object", item)
            intent.putExtra("productId", favKeyList[position])
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun removeFromFavorites(position: Int) {
        val key = favKeyList[position]  // 🔑 Lấy key từ danh sách key
        managmentFav.removeFav(key) {
            favList.removeAt(position)
            favKeyList.removeAt(position)  // 🧹 đồng bộ danh sách key
            notifyItemRemoved(position)
            onFavChanged?.invoke()
            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = favList.size
}
