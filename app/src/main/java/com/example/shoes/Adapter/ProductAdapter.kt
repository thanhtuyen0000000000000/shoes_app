package com.example.shoes.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.shoes.Model.ItemsModel
import com.example.shoes.R

class ProductAdapter(
    private val onEditClick: (ItemsModel, String) -> Unit,
    private val onDeleteClick: (String, String) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var products: List<Pair<String, ItemsModel>> = emptyList()

    fun submitList(newProducts: List<Pair<String, ItemsModel>>) {
        products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val (key, product) = products[position]
        holder.bind(product, key)
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.productTitleTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.productPriceTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.productImageView)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(product: ItemsModel, key: String) {
            titleTextView.text = product.title
            priceTextView.text = "$${product.price}"
            if (product.picUrl.isNotEmpty() && !product.picUrl[0].isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(product.picUrl[0])
                    .apply(RequestOptions().transform(CenterCrop()))
                    .into(imageView)
            }
            editButton.setOnClickListener { onEditClick(product, key) }
            deleteButton.setOnClickListener { onDeleteClick(key, product.picUrl[0]) }
        }
    }
}