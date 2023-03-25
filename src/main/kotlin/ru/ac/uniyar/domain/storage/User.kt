package ru.ac.uniyar.domain.storage

data class User(
    val id: Int,
    val name: String,
    val address: String,
    val userRole: String,
    val password: Int
)
