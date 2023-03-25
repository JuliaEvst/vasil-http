package ru.ac.uniyar.domain.storage

data class Category(
    val category: String,
    val categoryParent: String,
    var countProducts: Int = 0
)
