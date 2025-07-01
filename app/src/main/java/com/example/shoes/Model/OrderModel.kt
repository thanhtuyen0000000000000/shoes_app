package com.example.shoes.Model

data class OrderModel(
    val items: ArrayList<ItemsModel> = arrayListOf(),
    val total: Double = 0.0,
    val tax: Double = 0.0,
    val delivery: Double = 0.0,
    val status: String = "",
    val timestamp: String = ""
)