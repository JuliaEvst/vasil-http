package ru.ac.uniyar.web.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.storage.Product

data class ProductsListVM(
    val products: List<Product>,
    val path: String,
    val page: Int,
    val lastPage: Int,
    val categoryFilter: String,
    val sortFilter: String,
    val parentCategoryFilter: String?
) : ViewModel
