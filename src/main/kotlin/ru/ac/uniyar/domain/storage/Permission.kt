package ru.ac.uniyar.domain.storage

data class Permission(
    val role: String,
    val canAddProductInCart: Boolean = false,
    val canDeleteProductFromCart: Boolean = false,
    val canSeeCart: Boolean = false,
    val canOrder: Boolean = false,
    val canAddNewProduct: Boolean = false,
    val canEditProduct: Boolean = false,
    val canDeleteProduct: Boolean = false,
    val canEditCategories: Boolean = false,
    val canSeeProducer: Boolean = false,
    val canAddNewProducer: Boolean = false
)
