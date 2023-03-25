package ru.ac.uniyar.web.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.storage.Category

data class CategoryPageVM(
    val categories: List<Category>,
    val path: String,
    val page: Int,
    val lastPage: Int,
    val parentCategoryFilter: String
) : ViewModel
