//package com.example.shoes.Helper
//
//import android.content.Context
//import android.widget.Toast
//import com.example.shoes.Model.ItemsModel
//import com.google.firebase.database.*
//import com.google.firebase.auth.FirebaseAuth
//import kotlinx.coroutines.*
//import kotlinx.coroutines.tasks.await
//import kotlin.coroutines.resume
//import kotlin.coroutines.resumeWithException
//import kotlin.coroutines.suspendCoroutine
//import com.example.shoes.Helper.setValueAwait
//class ManagmentFav(private val context: Context) {
//    private val database = FirebaseDatabase.getInstance().reference
//
//    private fun getUsername(): String? {
//        val sharedPref = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
//        return sharedPref.getString("username", null)
//    }
//
//    fun insertShoe(item: ItemsModel, onDone: (() -> Unit)? = null) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val userId = getUsername() ?: return@launch
//                val favList = getListFav().toMutableList()
//                val alreadyExists = favList.any { it.title == item.title }
//
//                if (!alreadyExists) {
//                    favList.add(item)
//                    database.child("users").child(userId).child("listFav").setValueAwait(favList)
//                }
//
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
//                    onDone?.invoke()
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Error adding to favorites: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    suspend fun getListFav(): ArrayList<ItemsModel> {
//        return suspendCancellableCoroutine { continuation ->
//            val userId = getUsername() ?: run {
//                continuation.resume(arrayListOf())
//                return@suspendCancellableCoroutine
//            }
//
//            database.child("users").child(userId).child("listFav")
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val favList = ArrayList<ItemsModel>()
//                        for (childSnapshot in snapshot.children) {
//                            val item = childSnapshot.getValue(ItemsModel::class.java)
//                            item?.let { favList.add(it) }
//                        }
//                        continuation.resume(favList)
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        continuation.resumeWithException(error.toException())
//                    }
//                })
//        }
//    }
//
//    fun removeFav(title: String, onDone: (() -> Unit)? = null) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val userId = getUsername() ?: return@launch
//                val favList = getListFav().filterNot { it.title == title }
//
//                database.child("users").child(userId).child("listFav").setValueAwait(favList)
//
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
//                    onDone?.invoke()
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Error removing from favorites: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    suspend fun isFavorite(title: String): Boolean {
//        val favList = getListFav()
//        return favList.any { it.title == title }
//    }
//}

package com.example.shoes.Helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.shoes.Model.ItemsModel

import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ManagmentFav(private val context: Context) {
    private val database = FirebaseDatabase.getInstance().reference

    private fun getUsername(): String? {
        val sharedPref = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    fun insertShoe(item: ItemsModel, onDone: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = getUsername() ?: return@launch
                val itemRef = database.child("users").child(userId).child("listFav").child(item.title)

                itemRef.setValue(item).await()
                delay(100) // Đảm bảo Firebase sync ổn định

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
                    onDone?.invoke()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error adding to favorites: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun getListFav(): ArrayList<ItemsModel> {
        return suspendCancellableCoroutine { continuation ->
            val userId = getUsername() ?: run {
                continuation.resume(arrayListOf())
                return@suspendCancellableCoroutine
            }

            database.child("users").child(userId).child("listFav")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val favList = ArrayList<ItemsModel>()
                        for (childSnapshot in snapshot.children) {
                            val item = childSnapshot.getValue(ItemsModel::class.java)
                            item?.let { favList.add(it) }
                        }
                        continuation.resume(favList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

    fun removeFav(title: String, onDone: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = getUsername() ?: return@launch
                val itemRef = database.child("users").child(userId).child("listFav").child(title)

                itemRef.removeValue().await()
                delay(100)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                    onDone?.invoke()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error removing from favorites: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun isFavorite(title: String): Boolean {
        val userId = getUsername() ?: return false

        return suspendCoroutine { continuation ->
            database.child("users").child(userId).child("listFav").child(title)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot.exists())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(false)
                    }
                })
        }
    }
}

