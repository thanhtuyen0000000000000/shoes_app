package com.example.shoes.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.shoes.Helper.ChangeNumberItemsListener
import com.example.shoes.Helper.ManagmentCart
import com.example.shoes.Model.ItemsModel
import com.example.shoes.activity.DetailActivity
import com.example.shoes.databinding.ActivityDetailBinding
import com.example.shoes.databinding.ViewholderCartBinding
import java.util.ArrayList

class CartAdapter (private val listItemSelected:ArrayList<ItemsModel>,
    context: Context,
    var changeNumberItemsListener: ChangeNumberItemsListener?=null

) :RecyclerView.Adapter<CartAdapter.ViewHolder>() {
    class ViewHolder(val binding: ViewholderCartBinding):RecyclerView.ViewHolder(binding.root) {

    }
    private  val managementCart = ManagmentCart(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartAdapter.ViewHolder {
        val binding=ViewholderCartBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartAdapter.ViewHolder, position: Int) {
        val item=listItemSelected[position]

        holder.binding.titleTxt.text=item.title
        holder.binding.brandTxt.text="Brand: ${item.brand}"
        
        // Debug logging cho size
        Log.d("CartAdapter", "=== Cart Item Debug ===")
        Log.d("CartAdapter", "Item: ${item.title}")
        Log.d("CartAdapter", "Size array: ${item.size}")
        Log.d("CartAdapter", "Size array size: ${item.size.size}")
        Log.d("CartAdapter", "Selected size (first): ${if (item.size.isNotEmpty()) item.size[0] else "N/A"}")
        Log.d("CartAdapter", "Quantity: ${item.numberInCart}")
        Log.d("CartAdapter", "======================")
        
        // Hiển thị size đã chọn (luôn là phần tử đầu tiên trong array)
        val selectedSize = if (item.size.isNotEmpty()) item.size[0] else "N/A"
        holder.binding.sizeTxt.text="Size: $selectedSize"
        
        holder.binding.feeEachItem.text="$${item.price}"
        holder.binding.totalEachItem.text="$${Math.round(item.numberInCart*item.price)}"
        holder.binding.numberItemTxt.text = item.numberInCart.toString()

        Glide.with(holder.itemView.context)
            .load(item.picUrl[0])
            .apply(RequestOptions().transform(CenterCrop()))
            .into(holder.binding.pic)

        holder.binding.plusCartBtn.setOnClickListener{
            managementCart.plusItem(listItemSelected,position,object : ChangeNumberItemsListener{
                override fun onChanged() {
                    notifyDataSetChanged()
                    changeNumberItemsListener?.onChanged()
                }

            })
        }

        holder.binding.minusCartBtn.setOnClickListener{
            managementCart.minusItem(listItemSelected,position,object : ChangeNumberItemsListener{
                override fun onChanged() {
                    notifyDataSetChanged()
                    changeNumberItemsListener?.onChanged()
                }
            })
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("object", item)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int =listItemSelected.size
}