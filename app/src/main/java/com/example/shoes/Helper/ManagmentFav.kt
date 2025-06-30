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
        val username = sharedPref.getString("username", null)
        Log.d("ManagmentFav", "Username from SharedPrefs: $username")
        return username
    }

    // ✅ Dùng productId làm key khi thêm
    fun insertShoe(item: ItemsModel, productId: String, onDone: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = getUsername() ?: run {
                    Log.e("ManagmentFav", "Username is null, cannot insert shoe")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                Log.d("ManagmentFav", "Inserting favorite: ${item.title} with key $productId for user: $userId")
                val itemRef = database.child("users").child(userId).child("listFav").child(productId)

                itemRef.setValue(item).await()
                delay(100)
                Log.d("ManagmentFav", "Successfully saved favorite to Firebase")

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
                    onDone?.invoke()
                }
            } catch (e: Exception) {
                Log.e("ManagmentFav", "Error inserting favorite: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error adding to favorites: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ✅ Trả ra cả danh sách sản phẩm và danh sách key tương ứng
    suspend fun getListFav(): Pair<ArrayList<ItemsModel>, ArrayList<String>> {
        return suspendCancellableCoroutine { continuation ->
            val userId = getUsername() ?: run {
                Log.e("ManagmentFav", "Username is null, returning empty favorites")
                continuation.resume(Pair(arrayListOf(), arrayListOf()))
                return@suspendCancellableCoroutine
            }

            Log.d("ManagmentFav", "Loading favorites for user: $userId")
            database.child("users").child(userId).child("listFav")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val favList = ArrayList<ItemsModel>()
                        val keyList = ArrayList<String>()

                        Log.d("ManagmentFav", "Firebase snapshot exists: ${snapshot.exists()}, children count: ${snapshot.childrenCount}")

                        for (childSnapshot in snapshot.children) {
                            try {
                                val item = childSnapshot.getValue(ItemsModel::class.java)
                                if (item != null) {
                                    favList.add(item)
                                    keyList.add(childSnapshot.key ?: "")
                                    Log.d("ManagmentFav", "Loaded favorite: ${item.title} with key: ${childSnapshot.key}")
                                } else {
                                    Log.w("ManagmentFav", "Failed to parse favorite from snapshot: ${childSnapshot.key}")
                                }
                            } catch (e: Exception) {
                                Log.e("ManagmentFav", "Error parsing favorite: ${e.message}")
                            }
                        }
                        continuation.resume(Pair(favList, keyList))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ManagmentFav", "Firebase error: ${error.message}")
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

    // ✅ Xóa theo đúng Firebase key
    fun removeFav(productId: String, onDone: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = getUsername() ?: return@launch
                Log.d("ManagmentFav", "Removing favorite with key: $productId for user: $userId")

                val itemRef = database.child("users").child(userId).child("listFav").child(productId)
                itemRef.removeValue().await()
                delay(100)
                Log.d("ManagmentFav", "Successfully removed favorite from Firebase")

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                    onDone?.invoke()
                }
            } catch (e: Exception) {
                Log.e("ManagmentFav", "Error removing favorite: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error removing from favorites: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ✅ Kiểm tra yêu thích bằng Firebase key
    suspend fun isFavorite(productId: String): Boolean {
        val userId = getUsername() ?: return false

        return suspendCoroutine { continuation ->
            database.child("users").child(userId).child("listFav").child(productId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isFav = snapshot.exists()
                        Log.d("ManagmentFav", "Item with key '$productId' is favorite: $isFav")
                        continuation.resume(isFav)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ManagmentFav", "Error checking favorite: ${error.message}")
                        continuation.resume(false)
                    }
                })
        }
    }
}
