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
    private fun getUsername(): String? {
        val sharedPref = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    fun insertShoe(item: ItemsModel, onDone: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = getUsername() ?: return@launch
                val listShoe = getListCart().toMutableList()
                val existAlready = listShoe.any { it.title == item.title }
                val index = listShoe.indexOfFirst { it.title == item.title }

                if (existAlready) {
                    listShoe[index].numberInCart = item.numberInCart
                } else {
                    listShoe.add(item)
                }
                database.child("users").child(userId).child("listCart").setValueAwait(listShoe)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
                    onDone?.invoke()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error adding to cart: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun getListCart(): ArrayList<ItemsModel> {
        return suspendCancellableCoroutine { continuation ->
            val userId = getUsername() ?: run {
                continuation.resume(arrayListOf())
                return@suspendCancellableCoroutine
            }
            database.child("users").child(userId).child("listCart").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listShoe = ArrayList<ItemsModel>()
                    for (childSnapshot in snapshot.children) {
                        val item = childSnapshot.getValue(ItemsModel::class.java)
                        item?.let { listShoe.add(it) }
                    }
                    continuation.resume(listShoe)
                }

                override fun onCancelled(error: DatabaseError) {
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
                if (listShoe[position].numberInCart == 1) {
                    listShoe.removeAt(position)
                } else {
                    listShoe[position].numberInCart--
                }
                database.child("users").child(userId).child("listCart").setValueAwait(listShoe)
                withContext(Dispatchers.Main) {
                    listener.onChanged()
                }
            } catch (e: Exception) {
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
                listShoe[position].numberInCart++
                database.child("users").child(userId).child("listCart").setValueAwait(listShoe)
                withContext(Dispatchers.Main) {
                    listener.onChanged()
                }
            } catch (e: Exception) {
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
        return fee
    }
}

// Extension function để hỗ trợ await cho Realtime Database
