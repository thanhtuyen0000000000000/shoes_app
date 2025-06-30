//package com.example.shoes.Helper
//
//import android.content.Context
//import android.widget.Toast
//import com.example.shoes.Model.ItemsModel
//
//
//class ManagmentCart(val context: Context) {
//
//    private val tinyDB = TinyDB(context)
//
//    fun insertShoe(item: ItemsModel) {
//        var listShoe = getListCart()
//        val existAlready = listShoe.any { it.title == item.title }
//        val index = listShoe.indexOfFirst { it.title == item.title }
//
//        if (existAlready) {
//            listShoe[index].numberInCart = item.numberInCart
//        } else {
//            listShoe.add(item)
//        }
//        tinyDB.putListObject("CartList", listShoe)
//        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
//    }
//
//    fun getListCart(): ArrayList<ItemsModel> {
//        return tinyDB.getListObject("CartList") ?: arrayListOf()
//    }
//
//    fun minusItem(listShoe: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
//        if (listShoe[position].numberInCart == 1) {
//            listShoe.removeAt(position)
//        } else {
//            listShoe[position].numberInCart--
//        }
//        tinyDB.putListObject("CartList", listShoe)
//        listener.onChanged()
//    }
//
//    fun plusItem(listShoe: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
//        listShoe[position].numberInCart++
//        tinyDB.putListObject("CartList", listShoe)
//        listener.onChanged()
//    }
//
//    fun getTotalFee(): Double {
//        val listShoe = getListCart()
//        var fee = 0.0
//        for (item in listShoe) {
//            fee += item.price * item.numberInCart
//        }
//        return fee
//    }
//}

package com.example.shoes.Helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.shoes.Model.ItemsModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.shoes.Helper.setValueAwait

class ManagmentCart(val context: Context) {
    private val database = FirebaseDatabase.getInstance().reference
    
    // Helper method để tạo unique identifier cho cart item
    private fun getItemKey(item: ItemsModel): String {
        val size = if (item.size.isNotEmpty()) item.size[0] else "NO_SIZE"
        return "${item.title}_${size}"
    }
    
    // Helper method để debug item info
    private fun debugItemInfo(item: ItemsModel, prefix: String = "") {
        val key = getItemKey(item)
        Log.d("ManagmentCart", "$prefix Item Key: $key")
        Log.d("ManagmentCart", "$prefix Title: ${item.title}")
        Log.d("ManagmentCart", "$prefix Size: ${if (item.size.isNotEmpty()) item.size[0] else "N/A"}")
        Log.d("ManagmentCart", "$prefix Size Array: ${item.size}")
        Log.d("ManagmentCart", "$prefix Quantity: ${item.numberInCart}")
    }
    
    private fun getUsername(): String? {
        val sharedPref = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)
        Log.d("ManagmentCart", "Username from SharedPrefs: $username")
        return username
    }

    fun insertShoe(item: ItemsModel, onDone: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = getUsername() ?: run {
                    Log.e("ManagmentCart", "Username is null, cannot insert shoe")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                
                Log.d("ManagmentCart", "=== INSERT SHOE DEBUG ===")
                debugItemInfo(item, "[NEW] ")
                
                val listShoe = getListCart().toMutableList()
                Log.d("ManagmentCart", "Current cart has ${listShoe.size} items")
                
                // Log existing items in cart
                listShoe.forEachIndexed { index, existingItem ->
                    debugItemInfo(existingItem, "[EXISTING $index] ")
                }
                
                // Check both title AND size để xác định unique item  
                val newItemKey = getItemKey(item)
                val existingIndex = listShoe.indexOfFirst { existingItem ->
                    getItemKey(existingItem) == newItemKey
                }

                if (existingIndex != -1) {
                    Log.d("ManagmentCart", "Found duplicate item at index $existingIndex")
                    debugItemInfo(listShoe[existingIndex], "[BEFORE UPDATE] ")
                    
                    listShoe[existingIndex].numberInCart += item.numberInCart // Cộng dồn quantity
                    
                    debugItemInfo(listShoe[existingIndex], "[AFTER UPDATE] ")
                } else {
                    Log.d("ManagmentCart", "Adding new unique item to cart")
                    listShoe.add(item)
                }
                
                Log.d("ManagmentCart", "Final cart size: ${listShoe.size}")
                Log.d("ManagmentCart", "=========================")
                
                database.child("users").child(userId).child("listCart").setValueAwait(listShoe)
                Log.d("ManagmentCart", "Successfully saved cart to Firebase")
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
                    onDone?.invoke()
                }
            } catch (e: Exception) {
                Log.e("ManagmentCart", "Error inserting shoe: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error adding to cart: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun getListCart(): ArrayList<ItemsModel> {
        return suspendCancellableCoroutine { continuation ->
            val userId = getUsername() ?: run {
                Log.e("ManagmentCart", "Username is null, returning empty cart")
                continuation.resume(arrayListOf())
                return@suspendCancellableCoroutine
            }
            
            Log.d("ManagmentCart", "Loading cart for user: $userId")
            database.child("users").child(userId).child("listCart").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listShoe = ArrayList<ItemsModel>()
                    Log.d("ManagmentCart", "Firebase snapshot exists: ${snapshot.exists()}, children count: ${snapshot.childrenCount}")
                    
                    for (childSnapshot in snapshot.children) {
                        try {
                            val item = childSnapshot.getValue(ItemsModel::class.java)
                            if (item != null) {
                                listShoe.add(item)
                                debugItemInfo(item, "[LOADED] ")
                            } else {
                                Log.w("ManagmentCart", "Failed to parse item from snapshot: ${childSnapshot.key}")
                            }
                        } catch (e: Exception) {
                            Log.e("ManagmentCart", "Error parsing item: ${e.message}")
                        }
                    }
                    Log.d("ManagmentCart", "Total items loaded: ${listShoe.size}")
                    continuation.resume(listShoe)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ManagmentCart", "Firebase error: ${error.message}")
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }

    fun minusItem(listShoe: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = getUsername() ?: return@launch
                if (position < 0 || position >= listShoe.size) return@launch
                
                Log.d("ManagmentCart", "Minus item at position: $position")
                if (listShoe[position].numberInCart == 1) {
                    listShoe.removeAt(position)
                    Log.d("ManagmentCart", "Removed item from cart")
                } else {
                    listShoe[position].numberInCart--
                    Log.d("ManagmentCart", "Decreased quantity to: ${listShoe[position].numberInCart}")
                }
                database.child("users").child(userId).child("listCart").setValueAwait(listShoe)
                withContext(Dispatchers.Main) {
                    listener.onChanged()
                }
            } catch (e: Exception) {
                Log.e("ManagmentCart", "Error minus item: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error updating cart: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun plusItem(listShoe: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = getUsername() ?: return@launch
                if (position < 0 || position >= listShoe.size) return@launch
                
                Log.d("ManagmentCart", "Plus item at position: $position")
                listShoe[position].numberInCart++
                Log.d("ManagmentCart", "Increased quantity to: ${listShoe[position].numberInCart}")
                
                database.child("users").child(userId).child("listCart").setValueAwait(listShoe)
                withContext(Dispatchers.Main) {
                    listener.onChanged()
                }
            } catch (e: Exception) {
                Log.e("ManagmentCart", "Error plus item: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error updating cart: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun getTotalFee(): Double {
        val listShoe = getListCart()
        var fee = 0.0
        for (item in listShoe) {
            fee += item.price * item.numberInCart
        }
        Log.d("ManagmentCart", "Total fee calculated: $fee")
        return fee
    }
}

// Extension function để hỗ trợ await cho Realtime Database
