package com.example.shoes.Model

data class UserModel(
    val username: String = "",
    val password: String = "",
    val phonenumber: String = "",
    var listCart: Map<String, ItemsModel>? = emptyMap(),
    var listFav: Map<String, ItemsModel>? = emptyMap(),
    var listOrders: Map<String, OrderModel>? = emptyMap()
)
