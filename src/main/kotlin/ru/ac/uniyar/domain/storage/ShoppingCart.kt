package ru.ac.uniyar.domain.storage

data class ShoppingCart(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val productName: String,
    val productPrice: Int,
    val productCount: Int,
    val time: String
)
