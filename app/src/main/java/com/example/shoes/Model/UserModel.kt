package com.example.shoes.Model

data class UserModel(
    val username: String = "",
    val password: String = "",
    val phonenumber: String = "",
    val role: String = "user", // "admin" hoặc "user"
    val listCart: List<ItemsModel> = emptyList(),
    val listFav: List<ItemsModel> = emptyList(),
)
