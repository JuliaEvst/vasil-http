package ru.ac.uniyar.web.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.storage.ParentCategory

data class ParentCategoryListVM(
    val parentCategory: List<ParentCategory>,
    val path: String,
    val page: Int,
    val lastPage: Int
) : ViewModel
