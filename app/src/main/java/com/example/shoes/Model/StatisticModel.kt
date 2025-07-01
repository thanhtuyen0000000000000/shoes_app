package com.example.shoes.Model


data class StatisticModel(
    val productId: String = "",
    val productName: String = "",
    var quantitySold: Int = 0,
    var revenue: Double = 0.0,
)
