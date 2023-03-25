package ru.ac.uniyar.domain.storage

data class Product(
    var productId: Int,
    var category: String,
    var name: String,
    var description: String,
    var price: Int,
    var count: Int,
    var producer: String,
    var country: String,
    var time: String
)
